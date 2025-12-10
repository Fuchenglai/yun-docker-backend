package com.lfc.yundocker.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lfc.yundocker.common.exception.BusinessException;
import com.lfc.yundocker.common.exception.ThrowUtils;
import com.lfc.yundocker.common.model.dto.BaseResponse;
import com.lfc.yundocker.common.model.dto.PageRequest;
import com.lfc.yundocker.common.model.entity.User;
import com.lfc.yundocker.common.model.entity.YunOrder;
import com.lfc.yundocker.common.model.enums.ErrorCode;
import com.lfc.yundocker.common.model.vo.YunOrderVO;
import com.lfc.yundocker.common.util.ResultUtils;
import com.lfc.yundocker.service.UserService;
import com.lfc.yundocker.service.YunOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 订单接口
 *
 * @author laifucheng
 */
@Controller
@ResponseBody
@RequestMapping("order")
public class OrderController {

    @Resource
    private UserService userService;

    @Resource
    private YunOrderService orderService;


    @PostMapping("/list/page")
    public BaseResponse<Page<YunOrderVO>> listOrderByPage(@RequestBody PageRequest pageRequest,
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
        Page<YunOrder> page = new Page<>(current, size);
        QueryWrapper<YunOrder> queryWrapper = new QueryWrapper<YunOrder>()
                .eq("buyer_id", loginUser.getId());
        Page<YunOrder> orderPage = orderService.page(page, queryWrapper);

        Page<YunOrderVO> orderVOPage = orderService.getOrderVOPage(orderPage);

        return ResultUtils.success(orderVOPage);
    }

    @GetMapping("/create")
    public BaseResponse<String> addOrder(@RequestParam Integer credit,
                                         HttpServletRequest request) throws InterruptedException {

        if (credit == null || credit <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        final User loginUser = userService.getLoginUser(request);

        String orderId = orderService.create(credit, loginUser.getId());

        return ResultUtils.success(orderId);
    }


    @GetMapping("/detail")
    public BaseResponse<YunOrderVO> detail(@RequestParam String orderId, HttpServletRequest request) {
        if (orderId == null || orderId.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        YunOrder order = orderService.getByOrderId(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        orderService.checkOrder(order);
        YunOrderVO yunOrderVO = YunOrderVO.objToVo(order);


        return ResultUtils.success(yunOrderVO);
    }

    @GetMapping("/cancel")
    public BaseResponse<?> cancelOrder(@RequestParam String orderId, HttpServletRequest request) {

        if (orderId == null || orderId.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);

        // 根据订单id和userId查询订单
        QueryWrapper<YunOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId).eq("buyer_id", loginUser.getId());
        YunOrder order = orderService.getOne(queryWrapper);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        UpdateWrapper<YunOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("status", 2).eq("order_id", orderId);
        orderService.update(updateWrapper);

        return ResultUtils.success("取消成功！");
    }


}
