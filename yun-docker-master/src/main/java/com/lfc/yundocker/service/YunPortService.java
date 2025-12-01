package com.lfc.yundocker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lfc.yundocker.model.entity.YunPort;

/**
* @author ceng
* @description 针对表【yun_port(端口)】的数据库操作Service
* @createDate 2025-04-05 14:47:22
*/
public interface YunPortService extends IService<YunPort> {

    Integer getPublicContainerPort(String publicImage);

    Integer generatePort();

    boolean isValidPort(Integer port);
}
