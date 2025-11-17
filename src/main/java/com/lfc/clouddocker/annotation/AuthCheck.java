package com.lfc.clouddocker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验
 *
 * 如果某个接口必须是登录状态才能调用，@AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
 * 如果某个接口必须是管理员登录状态才能调用，@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
 *
 * @author laifucheng
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有某个角色
     *
     * @return
     */
    String mustRole() default "";

}

