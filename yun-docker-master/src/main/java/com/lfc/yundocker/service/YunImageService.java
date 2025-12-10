package com.lfc.yundocker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lfc.yundocker.common.model.entity.YunImage;

/**
* @author ceng
* @description 针对表【yun_image(镜像)】的数据库操作Service
* @createDate 2025-04-05 14:46:58
*/
public interface YunImageService extends IService<YunImage> {

    void pull(String image, Long userId) throws InterruptedException;

    void removeImage(Long id, Long userId);

    void removeAllPrivateImages();
}
