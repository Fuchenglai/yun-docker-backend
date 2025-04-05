package com.lfc.clouddocker.controller;

import com.lfc.clouddocker.docker.YunDockerClient;
import com.lfc.clouddocker.model.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/1
 * @Profile:
 */

@Controller
@ResponseBody
@RequestMapping("image")
public class ImageController {

    @Autowired
    private YunDockerClient yunDockerClient;

    @GetMapping("/pull")
    public Result<?> pull(@RequestParam("userId") String userId,
                          @RequestParam("image") String image) throws InterruptedException {
        if (!image.contains(":")) {
            image = image + ":latest";
        }

        yunDockerClient.pullImage(image);

        // todo 做持久化操作

        return Result.OK();
    }

    @GetMapping("/remove")
    public Result<?> remove(@RequestParam("userId") String userId,
                            @RequestParam("imageId") String imageId) {
        yunDockerClient.removeImage(imageId);

        // todo 持久化操作
        return Result.OK();
    }


}
