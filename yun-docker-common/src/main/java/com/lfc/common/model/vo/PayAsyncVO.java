package com.lfc.common.model.vo;

import lombok.Data;
import lombok.ToString;

/**
 * 异步支付回调视图层
 *
 * @author laifucheng
 */
@Data
@ToString
public class PayAsyncVO {
    private String gmt_create; // 创建时间
    private String charset; // 字符集
    private String gmt_payment; // 支付时间
    private String notify_time; // 通知时间
    private String subject; // 订单标题
    private String sign; // 签名
    private String buyer_id; // 买家ID
    private String body; // 商品描述
    private String invoice_amount; // 发票金额
    private String version; // 版本号
    private String notify_id; // 通知ID
    private String fund_bill_list; // 资金账单列表
    private String notify_type; // 通知类型
    private String out_trade_no; // 商户订单号
    private String total_amount; // 订单总金额
    private String trade_status; // 交易状态
    private String trade_no; // 交易号
    private String auth_app_id; // 授权应用的APP ID
    private String receipt_amount; // 实收金额
    private String point_amount; // 集分宝金额
    private String app_id; // 应用ID
    private String buyer_pay_amount; // 买家支付金额
    private String sign_type; // 签名类型
    private String seller_id; // 卖家ID
}
