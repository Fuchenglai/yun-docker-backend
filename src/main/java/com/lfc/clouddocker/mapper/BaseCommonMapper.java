package com.lfc.clouddocker.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.lfc.clouddocker.model.dto.LogDTO;
import org.apache.ibatis.annotations.Param;

public interface BaseCommonMapper {

    /**
     * 保存日志
     * @param dto
     */
    //@SqlParser(filter=true)
    @InterceptorIgnore(illegalSql = "true", tenantLine = "true")
    void saveLog(@Param("dto") LogDTO dto);

}
