package com.lfc.clouddocker.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 镜像
 * @TableName yun_image
 */
@TableName(value ="yun_image")
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
    private Integer imageSize;

    /**
     * 镜像类型：0-public,1-private
     */
    private Integer imageType;

    /**
     * 创建用户 id
     */
    private Long userId;

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
