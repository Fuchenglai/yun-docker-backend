package com.lfc.yundocker.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.ContainerConfig;
import com.github.dockerjava.api.model.ExposedPort;
import com.lfc.yundocker.common.exception.BusinessException;
import com.lfc.yundocker.common.model.enums.ErrorCode;
import com.lfc.yundocker.mapper.YunImageMapper;
import com.lfc.yundocker.common.model.entity.YunImage;
import com.lfc.yundocker.service.RpcDockerService;
import com.lfc.yundocker.service.UserService;
import com.lfc.yundocker.service.YunImageService;
import com.lfc.yundocker.service.dto.ImageResponseDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author ceng
 * @description 针对表【yun_image(镜像)】的数据库操作Service实现
 * @createDate 2025-04-05 14:46:58
 */
@Service
public class YunImageServiceImpl extends ServiceImpl<YunImageMapper, YunImage>
        implements YunImageService {

    @DubboReference(timeout = 120000)
    private RpcDockerService rpcDockerService;

    @Resource
    private YunImageMapper yunImageMapper;

    @Resource
    private UserService userService;

    @Override
    public void pull(String image, Long userId) throws InterruptedException {

        String[] strings = image.split(":");

        //如果这个镜像之前拉取过，那就不用再拉取了
        QueryWrapper<YunImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("repository", strings[0])
                .eq("tag", strings[1]);
        YunImage selectedImage = this.baseMapper.selectOne(queryWrapper);
        if (selectedImage != null) {
            if (selectedImage.getImageType() != 0) {
                selectedImage.setUserId(userId);
                save(selectedImage);
            }
            return;
        }

        //向docker发送pull命令
        ImageResponseDTO imageResponse = rpcDockerService.pullImage(image);
        int port = imageResponse.getPort() != null ? imageResponse.getPort() : 0;

        // 原格式：sha256:cc44224bfe208a46fbc45471e8f9416f66b75d6307573e29634e7f42e27a9268
        String imageId = imageResponse.getId().split(":")[1];

        // 原格式：23456409 B
        double memory = NumberUtil.div((long) imageResponse.getSize(), 1000 * 1000, 1);

        //扣减余额
        userService.updateBalance(-memory, userId);

        //数据库存入一个镜像
        YunImage yunImage = new YunImage().setUserId(userId).
                setImageType(1).setRepository(strings[0]).setTag(strings[1]).
                setImageId(imageId).setImageSize(memory).setPort(port);

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
        rpcDockerService.removeImage(image);

        //增加用户余额
        userService.updateBalance(yunImage.getImageSize(), userId);

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

        //镜像属于该用户并且是私人的
        queryWrapper.eq("id", id).eq("user_id", userId).eq("image_type", 1);
        return yunImageMapper.selectOne(queryWrapper);
    }

    @Override
    public void removeAllPrivateImages() {
        // 查找出所有私人的镜像
        QueryWrapper<YunImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("image_type", 1);
        List<YunImage> imageList = list(queryWrapper);

        for (YunImage image : imageList) {
            // 向docker发送rmi命令
            String imageName = image.getRepository() + ":" + image.getTag();
            rpcDockerService.removeImage(imageName);

            // 增加用户余额
            userService.updateBalance(image.getImageSize(), image.getUserId());

            // 删除数据库中的镜像
            removeById(image.getId());
        }


    }
}




