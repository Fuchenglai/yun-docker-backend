package com.lfc.yundocker.service;

import com.lfc.yundocker.common.model.dto.LogDTO;
import com.lfc.yundocker.common.model.entity.LoginUser;

/**
 * common接口
 *
 * @author laifucheng
 */
public interface BaseCommonService {

    /**
     * 保存日志
     * @param logDTO
     */
    void addLog(LogDTO logDTO);

    /**
     * 保存日志
     * @param LogContent
     * @param logType
     * @param operateType
     * @param user
     */
    void addLog(String LogContent, Integer logType, Integer operateType, LoginUser user);

    /**
     * 保存日志
     * @param LogContent
     * @param logType
     * @param operateType
     */
    void addLog(String LogContent, Integer logType, Integer operateType);

}
