package com.lfc.clouddocker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lfc.clouddocker.common.ErrorCode;
import com.lfc.clouddocker.docker.YunDockerClient;
import com.lfc.clouddocker.exception.BusinessException;
import com.lfc.clouddocker.mapper.YunImageMapper;
import com.lfc.clouddocker.model.entity.YunImage;
import com.lfc.clouddocker.service.YunImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author ceng
 * @description 针对表【yun_image(镜像)】的数据库操作Service实现
 * @createDate 2025-04-05 14:46:58
 */
@Service
public class YunImageServiceImpl extends ServiceImpl<YunImageMapper, YunImage>
        implements YunImageService {

    @Autowired
    private YunDockerClient yunDockerClient;

    @Resource
    private YunImageMapper yunImageMapper;

    @Override
    public void pull(String image, Long userId) throws InterruptedException {

        //向docker发送pull命令
        yunDockerClient.pullImage(image);

        //数据库存入一个镜像
        String[] strings = image.split(":");
        YunImage yunImage = new YunImage().setUserId(userId).
                setImageType(1).setRepository(strings[0]).setTag(strings[1]);

        save(yunImage);
    }

    @Override
    public void removeImage(Long id, Long userId) {
        YunImage yunImage = isImage2User(id, userId);
        if (yunImage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        //向docker发送rmi命令
        String image = yunImage.getRepository() + ":" + yunImage.getTag();
        yunDockerClient.removeImage(image);

        //删除数据库中的镜像
        removeById(id);
    }

    /**
     * 判断镜像是否属于用户
     *
     * @param id
     * @param userId
     * @return
     */
    public YunImage isImage2User(Long id, Long userId) {
        QueryWrapper<YunImage> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("id", id).eq("user_id", userId);
        return yunImageMapper.selectOne(queryWrapper);
    }
}




