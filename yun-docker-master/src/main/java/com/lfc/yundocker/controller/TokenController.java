package com.lfc.yundocker.controller;


import com.lfc.yundocker.common.model.dto.BaseResponse;
import com.lfc.yundocker.common.model.entity.User;
import com.lfc.yundocker.common.model.vo.Result;
import com.lfc.yundocker.common.util.ResultUtils;
import com.lfc.yundocker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.security.auth.message.AuthException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.lfc.yundocker.common.constant.CacheConstant.CACHE_KEY_SEPARATOR;

/**
 *@author laifucheng
 */

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("token")
public class TokenController {

    private static final String TOKEN_PREFIX = "token:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserService userService;

    @GetMapping("/get")
    public BaseResponse<String> get(@NotBlank String scene, HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        String token = UUID.randomUUID().toString();
        String tokenKey = TOKEN_PREFIX + scene + CACHE_KEY_SEPARATOR + token;
        stringRedisTemplate.opsForValue().set(tokenKey, token, 30, TimeUnit.MINUTES);
        return ResultUtils.success(tokenKey);
    }
}
