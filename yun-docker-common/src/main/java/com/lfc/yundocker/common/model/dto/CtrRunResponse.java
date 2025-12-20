package com.lfc.yundocker.common.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class CtrRunResponse implements Serializable {
    private String ctrId;
    private String ip;

    public CtrRunResponse(String ctrId, String ip) {
        this.ctrId = ctrId;
        this.ip = ip;
    }

}
