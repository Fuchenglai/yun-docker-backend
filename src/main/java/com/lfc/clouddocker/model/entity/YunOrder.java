package com.lfc.clouddocker.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值订单表
 * @TableName yun_order
 * @author laifucheng
 */
@TableName(value ="yun_order")
@Data
public class YunOrder {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long buyerId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 充值积分数量
     */
    private Integer credit;

    /**
     * 支付金额(元)
     */
    private BigDecimal money;

    /**
     * 订单状态：0-待支付,1-支付成功,2-已取消,3-支付失败
     */
    private Integer status;

    /**
     * 支付宝交易号
     */
    private String tradeNo;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 完成时间
     */
    private Date finishedTime;

    /**
     * 取消时间
     */
    private Date cancelTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}
