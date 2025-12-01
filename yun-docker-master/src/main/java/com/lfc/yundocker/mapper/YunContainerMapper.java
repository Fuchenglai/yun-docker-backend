package com.lfc.yundocker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lfc.yundocker.model.entity.YunContainer;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 容器数据库操作
 *
 * @author laifucheng
 */
public interface YunContainerMapper extends BaseMapper<YunContainer> {

    @Select("SELECT ports FROM yun_container")
    List<String> selectPortsList();
}
