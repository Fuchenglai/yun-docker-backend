package com.lfc.clouddocker.api;


import com.lfc.clouddocker.model.entity.LoginUser;
import com.lfc.clouddocker.model.vo.SysUserCacheInfo;

public interface CommonAPI {

    /**
     * 5根据用户账号查询用户信息
     *
     * @param username
     * @return
     */
    public LoginUser getUserByName(String username);


    /**
     * 9查询用户信息
     *
     * @param username
     * @return
     */
    SysUserCacheInfo getCacheUser(String username);
}
