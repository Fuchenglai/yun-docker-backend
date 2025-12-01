package com.lfc.yundocker.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 容器实体
 *
 * @author laifucheng
 */

@Data
@TableName("yun_container")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class YunContainer {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private String id;

    /**
     * 镜像 id（主键）
     */
    private Long imageId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 容器 id
     */
    private String containerId;

    /**
     * 命令
     */
    private String command;

    /**
     * 状态
     * exited
     * running
     * paused
     * restarting
     */
    private String status;

    /**
     * 端口 9081:6379/tcp
     */
    private String ports;

    /**
     * 容器名称 用户在启动容器时自定义的名称
     */
    private String containerName;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
