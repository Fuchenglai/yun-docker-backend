package com.lfc.clouddocker.controller;

import com.lfc.clouddocker.model.entity.YunContainer;
import com.lfc.clouddocker.model.vo.Result;
import com.lfc.clouddocker.service.YunContainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/1
 * @Profile:
 */
@Controller
@ResponseBody
@RequestMapping("Container")
public class ContainerController {

    @Autowired
    private YunContainerService yunContainerService;


    @GetMapping(value = "/list")
    public Result<List<YunContainer>> queryContainerList(@RequestParam("userId") String userId) {
        List<YunContainer> list = yunContainerService.queryByUserId(userId);
        return Result.OK(list);
    }

    @GetMapping("/active")
    public Result<?> active(@RequestParam("userId") String userId,
                                 @RequestParam("cid") String cid) {
        List<YunContainer> list = yunContainerService.queryByUserId(userId);
        for (YunContainer container : list) {
            if (Objects.equals(container.getContainerId(), cid)) {
                // todo
            }
        }
        return Result.error("启动容器失败");
    }


}
