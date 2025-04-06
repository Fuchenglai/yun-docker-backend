package com.lfc.clouddocker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lfc.clouddocker.common.ErrorCode;
import com.lfc.clouddocker.constant.CtrStatusConstant;
import com.lfc.clouddocker.docker.YunDockerClient;
import com.lfc.clouddocker.exception.BusinessException;
import com.lfc.clouddocker.mapper.UserMapper;
import com.lfc.clouddocker.mapper.YunContainerMapper;
import com.lfc.clouddocker.mapper.YunImageMapper;
import com.lfc.clouddocker.model.dto.CtrRunRequest;
import com.lfc.clouddocker.model.entity.User;
import com.lfc.clouddocker.model.entity.YunContainer;
import com.lfc.clouddocker.model.entity.YunImage;
import com.lfc.clouddocker.model.vo.ContainerVO;
import com.lfc.clouddocker.service.YunContainerService;
import com.lfc.clouddocker.service.YunImageService;
import com.lfc.clouddocker.util.PortManageUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/1
 * @Profile:
 */
@Service
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
    private UserMapper userMapper;

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
        if (CollectionUtils.isEmpty(containers)) {
            return containerVOPage;
        }

        Set<Long> imageIdSet = containers.stream().map(YunContainer::getImageId).collect(Collectors.toSet());
        Map<Long, List<YunImage>> imageId2YunImageListMap = yunImageService.listByIds(imageIdSet).stream().collect(Collectors.groupingBy(YunImage::getId));

        //填充信息
        List<ContainerVO> containerVOList = containers.stream().map(container -> {
            ContainerVO containerVO = ContainerVO.objToVo(container);
            String image = imageId2YunImageListMap.get(container.getImageId()).get(0).getRepository() + ":"
                    + imageId2YunImageListMap.get(container.getImageId()).get(0).getTag();
            containerVO.setImage(image);
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

        //根据imageId在数据库查询镜像
        QueryWrapper<YunImage> queryWrapper = new QueryWrapper<YunImage>()
                .eq("image_id", imageId);
        YunImage yunImage = yunImageMapper.selectOne(queryWrapper);
        if (yunImage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (yunImage.getUserId() != userId && yunImage.getImageType() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        String repository = yunImage.getRepository().toLowerCase();

        //确认端口是否合法
        Integer hostPort = ctrRunRequest.getHostPort();
        Integer containerPort = ctrRunRequest.getContainerPort();
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
            name = repository + UUID.randomUUID().toString().substring(0, 5);
        }

        //向docker发送run命令
        String ctrId = dockerClient.runCtr(imageId, hostPort, containerPort, name);

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
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .eq("id", userId)  // WHERE id = #{userId}
                .setSql("balance = balance - 300");  // 直接使用 SQL 表达式保证原子性
        userMapper.update(null, updateWrapper);
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
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<User>();
        updateWrapper
                .eq("id", userId)  // WHERE id = #{userId}
                .setSql("balance = balance + 300");  // 直接使用 SQL 表达式保证原子性
        userMapper.update(null, updateWrapper);

        remove(containerId, userId);

        //向docker发送remove命令
        return dockerClient.removeCtr(containerId);
    }


    /**
     * 判断容器是否属于用户
     *
     * @param containerId
     * @param userId
     * @return
     */
    public YunContainer isCtr2User(String containerId, Long userId) {
        QueryWrapper<YunContainer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("container_id", containerId).eq("user_id", userId);
        return yunContainerMapper.selectOne(queryWrapper);
    }


}
