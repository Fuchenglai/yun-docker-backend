package com.lfc.yundocker.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lfc.yundocker.common.BaseResponse;
import com.lfc.yundocker.common.ErrorCode;
import com.lfc.yundocker.common.PageRequest;
import com.lfc.yundocker.common.ResultUtils;
import com.lfc.yundocker.exception.BusinessException;
import com.lfc.yundocker.exception.ThrowUtils;
import com.lfc.yundocker.model.entity.User;
import com.lfc.yundocker.model.entity.YunImage;
import com.lfc.yundocker.service.UserService;
import com.lfc.yundocker.service.YunImageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 镜像接口
 *
 * @author laifucheng
 */

@Controller
@ResponseBody
@RequestMapping("image")
public class ImageController {

    @Resource
    private UserService userService;

    @Resource
    private YunImageService yunImageService;

    /**
     * 拉取一个镜像
     *
     * @param image
     * @param request
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/pull")
    public BaseResponse<?> pull(@RequestParam String image,
                                HttpServletRequest request) throws InterruptedException {

        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        final User loginUser = userService.getLoginUser(request);

        if (!image.contains(":")) {
            image = image + ":latest";
        }
        yunImageService.pull(image, loginUser.getId());

        return ResultUtils.success("拉取镜像成功");
    }

    /**
     * 删除一个镜像
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/remove")
    public BaseResponse<?> remove(@RequestParam Long id, HttpServletRequest request) {

        if (id == null || id == 0L) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        final User loginUser = userService.getLoginUser(request);

        yunImageService.removeImage(id, loginUser.getId());

        return ResultUtils.success("删除成功！");
    }

    /**
     * 获取我的镜像列表
     *
     * @param pageRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<YunImage>> listImageByPage(@RequestBody PageRequest pageRequest,
                                                        HttpServletRequest request) {
        if (pageRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long current = pageRequest.getCurrent();
        long size = pageRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        //构造分页请求
        Page<YunImage> page = new Page<>(current, size);
        QueryWrapper<YunImage> queryWrapper = new QueryWrapper<YunImage>()
                .eq("user_id", loginUser.getId()).or().eq("image_type", 0);

        Page<YunImage> yunImagePage = yunImageService.page(page, queryWrapper);

        return ResultUtils.success(yunImagePage);
    }


}
