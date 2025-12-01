package com.lfc.yundocker.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 容器运行请求
 *
 * @author laifucheng
 */
@Data
public class CtrRunRequest implements Serializable {

    /**
     * 镜像id,不是镜像的主键，主键都是Long类型的
     */
    private String imageId;

    /**
     * 宿主机端口
     */
    private Integer hostPort;

    /**
     * 容器端口
     */
    private Integer containerPort;

    /**
     * 自定义容器名称
     */
    private String name;
}
