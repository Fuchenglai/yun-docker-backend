package com.lfc.clouddocker.api;


import com.lfc.clouddocker.model.entity.LoginUser;

/**
 * @Description 底层共通业务API，提供其他独立模块调用
 * @Author scott
 * @Date 2019-4-20
 * @Version V1.0
 */
public interface ISysBaseAPI extends CommonAPI {

    /**
     * 6根据用户id查询用户信息
     *
     * @param id
     * @return
     */
    LoginUser getUserById(String id);

}
