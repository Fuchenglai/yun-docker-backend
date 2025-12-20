package com.lfc.yundocker.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lfc.yundocker.common.constant.CtrStatusConstant;
import com.lfc.yundocker.common.exception.BusinessException;
import com.lfc.yundocker.common.model.dto.BaseResponse;
import com.lfc.yundocker.common.model.dto.CtrRunResponse;
import com.lfc.yundocker.common.model.enums.ErrorCode;
import com.lfc.yundocker.common.util.ResultUtils;
import com.lfc.yundocker.mapper.YunContainerMapper;
import com.lfc.yundocker.mapper.YunImageMapper;
import com.lfc.yundocker.common.model.dto.CtrRunRequest;
import com.lfc.yundocker.common.model.entity.User;
import com.lfc.yundocker.common.model.entity.YunContainer;
import com.lfc.yundocker.common.model.entity.YunImage;
import com.lfc.yundocker.common.model.vo.ContainerVO;
import com.lfc.yundocker.monitor.MetricsCollector;
import com.lfc.yundocker.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

/**
 * 容器服务实现
 *
 * @author laifucheng
 */
@Service
@Slf4j
public class YunContainerServiceImpl extends ServiceImpl<YunContainerMapper, YunContainer> implements YunContainerService {

    @Resource
    private YunContainerMapper yunContainerMapper;

    @Resource
    private YunImageMapper yunImageMapper;

    @Resource
    private YunImageService yunImageService;

    @DubboReference(methods = {
            @Method(name = "runCtr", loadbalance = "firstcall"),
            @Method(name = "readCtrStats", loadbalance = "assignurl"),
            @Method(name = "closeStatsCmd", loadbalance = "assignurl"),
            @Method(name = "startCtr", loadbalance = "assignurl"),
            @Method(name = "stopCtr", loadbalance = "assignurl"),
            @Method(name = "logCtr", loadbalance = "assignurl"),
            @Method(name = "removeCtr", loadbalance = "assignurl"),
            @Method(name = "restartCtr", loadbalance = "assignurl"),})
    private RpcDockerService rpcDockerService;

    @Resource
    private UserService userService;

    @Resource
    private YunPortService yunPortService;

    @Resource
    private MetricsCollector metricsCollector;

    @Override
    public List<YunContainer> queryByUserId(String userId) {
        QueryWrapper<YunContainer> queryWrapper = new QueryWrapper<YunContainer>()
                .eq("user_id", userId);
        List<YunContainer> list = yunContainerMapper.selectList(queryWrapper);
        return list;
    }

    @Override
    public Page<ContainerVO> getContainerVOPage(Page<YunContainer> containerPage) {
        List<YunContainer> containers = containerPage.getRecords();
        Page<ContainerVO> containerVOPage = new Page<>(containerPage.getCurrent(), containerPage.getSize(), containerPage.getTotal());
        if (containers == null || containers.isEmpty()) {
            return containerVOPage;
        }

        Set<Long> imageIdSet = containers.stream().map(YunContainer::getImageId).collect(Collectors.toSet());
        Map<Long, List<YunImage>> imageId2YunImageListMap = yunImageService.listByIds(imageIdSet).stream().collect(Collectors.groupingBy(YunImage::getId));

        String ip = "192.168.254.128";
        // todo 这里得到的是云服务器的内网ip，没有用
/*        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ip = localHost.getHostAddress();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DOCKER_ERROR, e.getMessage());
        }*/

        //填充信息
        String finalIp = ip;
        List<ContainerVO> containerVOList = containers.stream().map(container -> {
            ContainerVO containerVO = ContainerVO.objToVo(container);
            String image = imageId2YunImageListMap.get(container.getImageId()).get(0).getRepository() + ":"
                    + imageId2YunImageListMap.get(container.getImageId()).get(0).getTag();
            containerVO.setImage(image);
            containerVO.setIp(finalIp);
            return containerVO;
        }).collect(Collectors.toList());

        containerVOPage.setRecords(containerVOList);
        return containerVOPage;
    }

    @Override
    public boolean startOrStop(String containerId, Long userId) {

        YunContainer yunContainer = isCtr2User(containerId, userId);
        if (yunContainer == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        if (yunContainer.getStatus().equals(CtrStatusConstant.EXITED)) {
            //向docker发送start命令
            if (rpcDockerService.startCtr(containerId)) {
                //数据库持久化
                yunContainer.setStatus(CtrStatusConstant.RUNNING);
                return updateById(yunContainer);
            } else {
                throw new BusinessException(ErrorCode.DOCKER_ERROR);
            }

        } else if (yunContainer.getStatus().equals(CtrStatusConstant.RUNNING)) {
            if (rpcDockerService.stopCtr(containerId)) {
                yunContainer.setStatus(CtrStatusConstant.EXITED);
                return updateById(yunContainer);
            }
        }

        return false;
    }

    @Override
    public void run(CtrRunRequest ctrRunRequest, User loginUser) {

        String imageId = ctrRunRequest.getImageId();
        Long userId = loginUser.getId();

        //根据id（主键）在数据库查询镜像
        QueryWrapper<YunImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("image_id", imageId);
        YunImage yunImage = yunImageMapper.selectOne(queryWrapper);

        if (yunImage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        log.info("yunImage:{}", yunImage);
        // 检查镜像是否属于当前用户，检查镜像是否是公共镜像
        if (!Objects.equals(yunImage.getUserId(), userId) && yunImage.getImageType() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        String repository = yunImage.getRepository().toLowerCase();

        //确认端口是否合法
        Integer hostPort = ctrRunRequest.getHostPort();
        Integer containerPort = ctrRunRequest.getContainerPort();
        //检查宿主机端口
        if (hostPort == null || hostPort == 0) {
            hostPort = yunPortService.generatePort();
        } else if (!yunPortService.isValidPort(hostPort)) {
            throw new BusinessException(ErrorCode.HOST_ERROR, "该端口已被占用，请更换端口！");
        }
        //如果是公共的镜像，容器端口号使用map里设置的
        if (yunImage.getImageType() == 0) {
            containerPort = yunPortService.getPublicContainerPort(repository);
        }

        //检查name是否存在
        String name = ctrRunRequest.getName();
        if (name == null || name.isEmpty()) {
            name = repository + "_" + UUID.randomUUID().toString().substring(0, 5);
        }

        String image = yunImage.getImageId();

        log.info("发送run命令之前的主机端口号：{}，容器端口号：{}", hostPort, containerPort);

        long startTime = System.currentTimeMillis();

        //向docker发送run命令
        CtrRunResponse ctrRunResponse = new CtrRunResponse();
        try {
            ctrRunResponse = rpcDockerService.runCtr(image, hostPort, containerPort, name);
        } catch (Exception e) {
            //记录镜像运行失败的次数
            metricsCollector.recordError(String.valueOf(userId), repository + ":" + yunImage.getTag(), e.getMessage());
            throw new BusinessException(ErrorCode.DOCKER_ERROR, e.getMessage());
        }

        //记录镜像运行成功的响应时间
        long endTime = System.currentTimeMillis();
        Duration duration = Duration.ofMillis(endTime - startTime);
        metricsCollector.recordResponseTime(String.valueOf(userId), repository + ":" + yunImage.getTag(), duration);

        //记录镜像被创建的次数
        metricsCollector.recordRequest(String.valueOf(userId), repository + ":" + yunImage.getTag(), "running");


        //将容器信息保存到数据库
        YunContainer yunContainer = new YunContainer().setImageId(yunImage.getId())
                .setUserId(userId)
                .setContainerId(ctrRunResponse.getCtrId())
                .setIp(ctrRunResponse.getIp())
                .setStatus(CtrStatusConstant.RUNNING).setContainerName(name);
        if (containerPort != null && containerPort != 0) {
            yunContainer.setPorts(hostPort + ":" + containerPort + "/tcp");
        }
        save(yunContainer);

        //扣减用户余额
        userService.updateBalance(-200.0, userId);
    }

    @Override
    public boolean restart(String containerId, Long userId) {
        YunContainer yunContainer = isCtr2User(containerId, userId);
        if (yunContainer == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        //向docker发送restart命令
        return rpcDockerService.restartCtr(containerId);

    }

    @Override
    public boolean remove(String containerId, Long userId) {
        YunContainer yunContainer = isCtr2User(containerId, userId);
        if (yunContainer == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        //增加用户余额
        userService.updateBalance(200.0, userId);

        Wrapper<YunContainer> queryWrapper = new QueryWrapper<YunContainer>()
                .eq("container_id", containerId)
                .eq("user_id", userId);
        yunContainerMapper.delete(queryWrapper);

        //向docker发送remove命令
        return rpcDockerService.removeCtr(containerId);
    }

    @Override
    public BaseResponse<?> readStats(String containerId, Long userId) {
        YunContainer yunContainer = isCtr2User(containerId, userId);
        if (yunContainer == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        rpcDockerService.readCtrStats(containerId, userId);

        return ResultUtils.success("操作成功！");
    }

    /**
     * 判断容器是否属于用户
     *
     * @param containerId
     * @param userId
     * @return
     */
    @Override
    public YunContainer isCtr2User(String containerId, Long userId) {
        QueryWrapper<YunContainer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("container_id", containerId).eq("user_id", userId);
        return yunContainerMapper.selectOne(queryWrapper);
    }

    @Override
    public void logCtr(String containerId, Long userId, HttpServletResponse response) {
        YunContainer yunContainer = isCtr2User(containerId, userId);
        if (yunContainer == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        try {
            byte[] data = rpcDockerService.logCtr(containerId);

            response.setCharacterEncoding("UTF-8");
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("log.txt", "UTF-8"));
            response.addHeader("Content-Length", String.valueOf(data.length));
            response.setContentType("application/force-download");

            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            outputStream.write(data);
            outputStream.flush();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DOCKER_ERROR, e.getMessage());
        }
    }

    /**
     * 删除所有的容器
     */
    @Override
    public void removeAllCtr() {
        List<YunContainer> containers = list();
        if (containers == null || containers.isEmpty()) {
            return;
        }

        for (YunContainer container : containers) {

            //增加用户余额
            userService.updateBalance(200.0, container.getUserId());
            //删除数据库中的容器
            removeById(container.getId());
            //向docker发送remove命令
            rpcDockerService.removeCtr(container.getContainerId());
        }
    }

    @Override
    public String getWorkerUrl(String containerId) {
        Wrapper<YunContainer> queryWrapper = new QueryWrapper<YunContainer>().eq("container_id", containerId);
        YunContainer yunContainer = getOne(queryWrapper);
        return yunContainer.getIp();
    }
}
