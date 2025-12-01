package com.lfc.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 镜像实体
 *
 * @TableName yun_image
 * @author laifucheng
 */
@TableName(value = "yun_image")
@Accessors(chain = true)
@Data
public class YunImage {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 镜像名称
     */
    private String repository;

    /**
     * tag
     */
    private String tag;

    /**
     * 镜像id
     */
    private String imageId;

    /**
     * 镜像大小
     */
    private Double imageSize;

    /**
     * 镜像类型：0-public,1-private
     */
    private Integer imageType;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 对外暴露的端口号
     */
    private int port;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateIme;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}
