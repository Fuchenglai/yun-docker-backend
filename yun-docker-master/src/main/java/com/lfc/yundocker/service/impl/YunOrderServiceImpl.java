package com.lfc.yundocker.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lfc.yundocker.mapper.YunOrderMapper;
import com.lfc.yundocker.common.model.entity.YunOrder;
import com.lfc.yundocker.common.model.vo.PayAsyncVO;
import com.lfc.yundocker.common.model.vo.YunOrderVO;
import com.lfc.yundocker.service.UserService;
import com.lfc.yundocker.service.YunOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ceng
 * @description 针对表【yun_order(充值订单表)】的数据库操作Service实现
 * @createDate 2025-05-17 17:10:24
 */
@Slf4j
@Service
public class YunOrderServiceImpl extends ServiceImpl<YunOrderMapper, YunOrder>
        implements YunOrderService {

    @Autowired
    private UserService userService;
    @Autowired
    private YunOrderMapper yunOrderMapper;

    @Override
    public Page<YunOrderVO> getOrderVOPage(Page<YunOrder> orderPage) {
        List<YunOrder> orders = orderPage.getRecords();
        Page<YunOrderVO> orderVOPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        if (orders == null || orders.isEmpty()) {
            return orderVOPage;
        }

        List<YunOrderVO> orderVOList = orders.stream().map(order -> {
            checkOrder(order);
            return YunOrderVO.objToVo(order);
        }).collect(Collectors.toList());

        orderVOPage.setRecords(orderVOList);


        return orderVOPage;
    }

    @Override
    public String create(Integer credit, Long userId) {
        YunOrder order = new YunOrder();
        order.setCredit(credit);
        order.setBuyerId(userId);
        // todo 后续要改进成雪花算法+基因法
        String orderId = IdUtil.getSnowflake().nextIdStr();
        /*String orderId = DistributeID.generateWithSnowflake(BusinessCode.TRADE_ORDER, WorkerIdHolder.WORKER_ID, request.getBuyerId());
        tradeOrder.setOrderId(orderId);*/
        order.setOrderId(orderId);
        order.setStatus(0);

        order.setMoney(BigDecimal.valueOf(0.01 * credit));
        save(order);
        return orderId;
    }

    /**
     * 处理支付宝的支付结果
     *
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVO vo) {
        log.info("支付宝异步回调开始");
        if (vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")) {
            String outTradeNo = vo.getOut_trade_no();
            this.update().set("status", 1)
                    .set("trade_no", vo.getTrade_no())
                    .set("finished_time", new Date())
                    .eq("order_id", outTradeNo).update();
            log.info("订单{}本地状态修改成功", outTradeNo);

            //更新用户积分余额
            QueryWrapper<YunOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_id", outTradeNo);
            YunOrder yunOrder = yunOrderMapper.selectOne(queryWrapper);
            userService.updateBalance(Double.valueOf(yunOrder.getCredit()), yunOrder.getBuyerId());
            return "success";
        }
        return "error";
    }

    /**
     * 在用户查看订单时检查订单是否已完成
     *
     * @param order
     */
    @Override
    public void checkOrder(YunOrder order) {
        if (order.getStatus() == 0) {
            //创建超过15分钟未支付，则取消订单
            Date createTime = order.getCreateTime();
            if (new Date().getTime() - createTime.getTime() > 15 * 60 * 1000) {

                //更新数据库
                this.update().set("status", 2)
                        .set("cancel_time", new Date())
                        .eq("order_id", order.getOrderId()).update();
                order.setStatus(2);
                order.setCancelTime(new Date());
            }
        }

    }

    @Override
    public YunOrder getByOrderId(String orderId) {
        QueryWrapper<YunOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        YunOrder order = getOne(queryWrapper);
        return order;
    }
}




