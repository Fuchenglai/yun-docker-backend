package com.lfc.yundocker.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lfc.yundocker.model.entity.YunOrder;
import com.lfc.yundocker.model.vo.PayAsyncVO;
import com.lfc.yundocker.model.vo.YunOrderVO;

/**
* @author ceng
* @description 针对表【yun_order(充值订单表)】的数据库操作Service
* @createDate 2025-05-17 17:10:24
*/
public interface YunOrderService extends IService<YunOrder> {

    Page<YunOrderVO> getOrderVOPage(Page<YunOrder> orderPage);

    String create(Integer credit, Long id);

    String handlePayResult(PayAsyncVO vo);

    void checkOrder(YunOrder order);

    YunOrder getByOrderId(String orderId);
}
