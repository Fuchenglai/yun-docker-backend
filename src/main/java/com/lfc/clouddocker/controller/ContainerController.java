package com.lfc.clouddocker.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lfc.clouddocker.common.BaseResponse;
import com.lfc.clouddocker.common.ErrorCode;
import com.lfc.clouddocker.common.PageRequest;
import com.lfc.clouddocker.common.ResultUtils;
import com.lfc.clouddocker.exception.BusinessException;
import com.lfc.clouddocker.exception.ThrowUtils;
import com.lfc.clouddocker.model.dto.CtrRunRequest;
import com.lfc.clouddocker.model.entity.User;
import com.lfc.clouddocker.model.entity.YunContainer;
import com.lfc.clouddocker.model.vo.ContainerVO;
import com.lfc.clouddocker.service.UserService;
import com.lfc.clouddocker.service.YunContainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/1
 * @Profile:
 */
@Controller
@ResponseBody
@RequestMapping("container")
public class ContainerController {

    @Autowired
    private YunContainerService yunContainerService;

    @Resource
    private UserService userService;


    /**
     * 获取我的容器列表
     *
     * @param pageRequest
     * @param request
     * @return
     */
    @PostMapping(value = "/list/page")
    public BaseResponse<Page<ContainerVO>> listContainerByPage(@RequestBody PageRequest pageRequest,
                                                               HttpServletRequest request) {
        if (pageRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long current = pageRequest.getCurrent();
        long size = pageRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        //构造分页请求
        Page<YunContainer> page = new Page<>(current, size);
        QueryWrapper<YunContainer> queryWrapper = new QueryWrapper<YunContainer>()
                .eq("user_id", loginUser.getId());
        Page<YunContainer> containerPage = yunContainerService.page(page, queryWrapper);

        Page<ContainerVO> containerVOPage = yunContainerService.getContainerVOPage(containerPage);

        return ResultUtils.success(containerVOPage);
    }

    /**
     * 启动或者停止一个容器
     *
     * @param containerId
     * @param request
     * @return
     */
    @GetMapping("/startOrStop")
    public BaseResponse<String> startOrStop(@RequestParam String containerId, HttpServletRequest request) {
        if (containerId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 登录才能操作
        final User loginUser = userService.getLoginUser(request);
        return yunContainerService.startOrStop(containerId, loginUser.getId()) ?
                ResultUtils.success("操作成功！") : ResultUtils.error(ErrorCode.SYSTEM_ERROR, "操作失败！");
    }

    /**
     * 重启一个容器
     *
     * @param containerId
     * @param request
     * @return
     */
    @GetMapping("/restart")
    public BaseResponse<String> restart(@RequestParam String containerId, HttpServletRequest request) {
        if (containerId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 登录才能操作
        final User loginUser = userService.getLoginUser(request);
        return yunContainerService.restart(containerId, loginUser.getId()) ?
                ResultUtils.success("操作成功！") : ResultUtils.error(ErrorCode.SYSTEM_ERROR, "操作失败！");
    }


    /**
     * 创建（run）一个容器
     *
     * @param ctrRunRequest
     * @param request
     * @return
     */
    @PostMapping("/run")
    public BaseResponse<?> run(@RequestBody CtrRunRequest ctrRunRequest, HttpServletRequest request) {
        if (ctrRunRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        final User loginUser = userService.getLoginUser(request);

        //判断用户余额是否充足
        if (!userService.hasBalance(loginUser.getId())) {
            return ResultUtils.error(ErrorCode.INSUFFICIENT_BALANCE, "余额不足，请充值！");
        }
        yunContainerService.run(ctrRunRequest, loginUser);

        return ResultUtils.success("启动容器成功！");

    }

    /**
     * 删除一个容器
     *
     * @param containerId
     * @param request
     * @return
     */
    @GetMapping("/remove")
    public BaseResponse<String> remove(@RequestParam String containerId, HttpServletRequest request) {
        if (containerId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 登录才能操作
        final User loginUser = userService.getLoginUser(request);
        return yunContainerService.remove(containerId, loginUser.getId()) ?
                ResultUtils.success("操作成功！") : ResultUtils.error(ErrorCode.SYSTEM_ERROR, "操作失败！");
    }


}
