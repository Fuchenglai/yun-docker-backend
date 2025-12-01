package com.lfc.common.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 登录用户信息
 *
 * @author laifucheng
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LoginUser {

    /**
     * 登录人id
     */
    private String id;

    /**
     * 登录人账号
     */
    private String username;

    /**
     * 登录人密码
     */
    private String password;

    /**
     * 电子邮件
     */
    private String email;

    /**
     * 电话
     */
    private String phone;

    /**
     * 状态(1：正常 2：冻结 ）
     */
    private Integer status;

    private Integer delFlag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 设备id uniapp推送用
     */
    private String clientId;

}
