package com.lfc.clouddocker.model.dto.message;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/12
 * @Profile:
 */
@Data
public class CtrStatsRequestMessage implements Serializable {
    private Long userId;
    private String ctrId;
}
