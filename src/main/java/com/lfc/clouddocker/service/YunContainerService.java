package com.lfc.clouddocker.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lfc.clouddocker.common.BaseResponse;
import com.lfc.clouddocker.model.dto.CtrRunRequest;
import com.lfc.clouddocker.model.entity.User;
import com.lfc.clouddocker.model.entity.YunContainer;
import com.lfc.clouddocker.model.vo.ContainerVO;

import java.util.List;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/1
 * @Profile:
 */
public interface YunContainerService extends IService<YunContainer> {

    List<YunContainer> queryByUserId(String UserId);

    Page<ContainerVO> getContainerVOPage(Page<YunContainer> containerPage);

    boolean startOrStop(String containerId, Long userId);

    void run(CtrRunRequest ctrRunRequest, User loginUser);

    boolean restart(String containerId, Long id);

    boolean remove(String containerId, Long userId);

    BaseResponse<?> readStats(String containerId, Long userId);
}
