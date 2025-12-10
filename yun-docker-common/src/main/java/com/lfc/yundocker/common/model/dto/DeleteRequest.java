package com.lfc.yundocker.common.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求，因为删除请求一般都是传id的
 *
 * @author laifucheng
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
