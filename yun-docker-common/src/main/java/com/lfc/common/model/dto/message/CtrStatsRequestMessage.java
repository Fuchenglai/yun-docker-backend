package com.lfc.common.model.dto.message;

import lombok.Data;

import java.io.Serializable;

/**
 * 容器统计请求消息
 *
 * @author laifucheng
 */
@Data
public class CtrStatsRequestMessage implements Serializable {
    private Long userId;
    private String ctrId;
}
