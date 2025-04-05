package com.lfc.clouddocker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lfc.clouddocker.mapper.YunContainerMapper;
import com.lfc.clouddocker.model.entity.YunContainer;
import com.lfc.clouddocker.service.YunContainerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/1
 * @Profile:
 */
@Service
public class YunContainerServiceImpl extends ServiceImpl<YunContainerMapper, YunContainer> implements YunContainerService {

    @Resource
    private YunContainerMapper yunContainerMapper;

    @Override
    public List<YunContainer> queryByUserId(String userId) {
        QueryWrapper<YunContainer> queryWrapper = new QueryWrapper<YunContainer>()
                .eq("user_id", userId);
        List<YunContainer> list = yunContainerMapper.selectList(queryWrapper);
        return list;
    }




}
