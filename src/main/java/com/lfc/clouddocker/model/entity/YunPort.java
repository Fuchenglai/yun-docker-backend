package com.lfc.clouddocker.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 端口实体
 *
 * @TableName yun_port
 * @author laifucheng
 */

@Data
@TableName("yun_port")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class YunPort {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private String id;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
