package com.lfc.clouddocker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lfc.clouddocker.model.entity.YunContainer;

import java.util.List;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/1
 * @Profile:
 */
public interface YunContainerService extends IService<YunContainer> {

    List<YunContainer> queryByUserId(String UserId);

}
