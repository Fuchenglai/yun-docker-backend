package com.lfc.common.model.vo;

import com.lfc.common.model.entity.YunOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单视图
 *
 * @author laifucheng
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class YunOrderVO implements Serializable {
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

    private String frontMoney;

    /**
     * 订单状态：0-待支付,1-支付成功,2-已取消,3-支付失败
     */
    private Integer status;

    private String frontStatus;

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

    public void setMoney(BigDecimal money) {
        this.money = money;
        this.frontMoney = money.setScale(2, BigDecimal.ROUND_UP).toString();
    }

    public void setStatus(Integer status) {
        this.status = status;
        if (status == 0) {
            this.frontStatus = "待支付";
        } else if (status == 1) {
            this.frontStatus = "支付成功";
        } else if (status == 2) {
            this.frontStatus = "已取消";
        } else if (status == 3) {
            this.frontStatus = "支付失败";
        }
    }

    public static YunOrderVO objToVo(YunOrder order) {
        if (order == null) {
            return null;
        }
        YunOrderVO orderVO = new YunOrderVO();
        BeanUtils.copyProperties(order, orderVO);

        return orderVO;
    }
}
