package com.lfc.clouddocker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lfc.clouddocker.model.entity.YunContainer;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/1
 * @Profile:
 */
public interface YunContainerMapper extends BaseMapper<YunContainer> {

    @Select("SELECT ports FROM yun_container")
    List<String> selectPortsList();
}
