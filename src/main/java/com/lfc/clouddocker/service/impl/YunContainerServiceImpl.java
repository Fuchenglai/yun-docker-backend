package com.lfc.clouddocker.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lfc.clouddocker.common.BaseResponse;
import com.lfc.clouddocker.common.ErrorCode;
import com.lfc.clouddocker.common.ResultUtils;
import com.lfc.clouddocker.constant.CtrStatusConstant;
import com.lfc.clouddocker.docker.YunDockerClient;
import com.lfc.clouddocker.exception.BusinessException;
import com.lfc.clouddocker.mapper.YunContainerMapper;
import com.lfc.clouddocker.mapper.YunImageMapper;
import com.lfc.clouddocker.model.dto.CtrRunRequest;
import com.lfc.clouddocker.model.entity.User;
import com.lfc.clouddocker.model.entity.YunContainer;
import com.lfc.clouddocker.model.entity.YunImage;
import com.lfc.clouddocker.model.vo.ContainerVO;
import com.lfc.clouddocker.service.UserService;
import com.lfc.clouddocker.service.YunContainerService;
import com.lfc.clouddocker.service.YunImageService;
import com.lfc.clouddocker.util.PortManageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/1
 * @Profile:
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

    @Autowired
    private YunDockerClient dockerClient;

    @Resource
    private UserService userService;

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

        String ip = "0.0.0.0";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ip = localHost.getHostAddress();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DOCKER_ERROR, e.getMessage());
        }

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
            if (dockerClient.startCtr(containerId)) {
                //数据库持久化
                yunContainer.setStatus(CtrStatusConstant.RUNNING);
                return updateById(yunContainer);
            } else {
                throw new BusinessException(ErrorCode.DOCKER_ERROR);
            }

        } else if (yunContainer.getStatus().equals(CtrStatusConstant.RUNNING)) {
            if (dockerClient.stopCtr(containerId)) {
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
        if (!Objects.equals(yunImage.getUserId(), userId) && yunImage.getImageType() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        String repository = yunImage.getRepository().toLowerCase();

        //确认端口是否合法
        Integer hostPort = ctrRunRequest.getHostPort();
        Integer containerPort = ctrRunRequest.getContainerPort();

        //如果是公共的镜像，则使用Map里面规定好的端口号，否则生成随机端口号
        if (yunImage.getImageType() == 0) {
            hostPort = PortManageUtil.getPublicHostPort(repository);
            containerPort = PortManageUtil.getPublicContainerPort(repository);
        } else if (containerPort != null && containerPort != 0) {
            if (hostPort == null || !PortManageUtil.isValidPort(hostPort)) {
                hostPort = PortManageUtil.generatePort();
            }
        }

        //检查name是否存在
        String name = ctrRunRequest.getName();
        if (name == null || name.isEmpty()) {
            name = repository + "_" + UUID.randomUUID().toString().substring(0, 5);
        }

        //String image = repository + ":" + yunImage.getTag();
        String image = yunImage.getImageId();

        log.info("发送run命令之前的主机端口号：{}，容器端口号：{}", hostPort, containerPort);

        //向docker发送run命令
        String ctrId = dockerClient.runCtr(image, hostPort, containerPort, name);

        //将容器信息保存到数据库
        YunContainer yunContainer = new YunContainer().setImageId(yunImage.getId())
                .setUserId(userId)
                .setContainerId(ctrId)
                .setStatus(CtrStatusConstant.RUNNING).setContainerName(name);
        if (containerPort != null && containerPort != 0) {
            yunContainer.setPorts(hostPort + ":" + containerPort + "/tcp");
        }
        save(yunContainer);

        //扣减用户余额
        userService.updateBalance(-300.0, userId);
    }

    @Override
    public boolean restart(String containerId, Long userId) {
        YunContainer yunContainer = isCtr2User(containerId, userId);
        if (yunContainer == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        //向docker发送restart命令
        return dockerClient.restartCtr(containerId);

    }

    @Override
    public boolean remove(String containerId, Long userId) {
        YunContainer yunContainer = isCtr2User(containerId, userId);
        if (yunContainer == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        //增加用户余额
        userService.updateBalance(300.0, userId);

        Wrapper<YunContainer> queryWrapper = new QueryWrapper<YunContainer>()
                .eq("container_id", containerId)
                .eq("user_id", userId);
        yunContainerMapper.delete(queryWrapper);

        //向docker发送remove命令
        return dockerClient.removeCtr(containerId);
    }

    @Override
    public BaseResponse<?> readStats(String containerId, Long userId) {
        YunContainer yunContainer = isCtr2User(containerId, userId);
        if (yunContainer == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        dockerClient.readCtrStats(containerId, userId);

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


}
