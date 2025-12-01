package com.lfc.yundocker.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lfc.yundocker.common.BaseResponse;
import com.lfc.yundocker.model.dto.CtrRunRequest;
import com.lfc.yundocker.model.entity.User;
import com.lfc.yundocker.model.entity.YunContainer;
import com.lfc.yundocker.model.vo.ContainerVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 容器服务
 *
 * @author laifucheng
 */
public interface YunContainerService extends IService<YunContainer> {

    List<YunContainer> queryByUserId(String UserId);

    Page<ContainerVO> getContainerVOPage(Page<YunContainer> containerPage);

    boolean startOrStop(String containerId, Long userId);

    void run(CtrRunRequest ctrRunRequest, User loginUser);

    boolean restart(String containerId, Long id);

    boolean remove(String containerId, Long userId);

    BaseResponse<?> readStats(String containerId, Long userId);

    YunContainer isCtr2User(String containerId, Long userId);

    void logCtr(String containerId, Long userId, HttpServletResponse response);
    
    void removeAllCtr();
}
