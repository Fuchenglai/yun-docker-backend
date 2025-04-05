package com.lfc.clouddocker.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lfc.clouddocker.api.ISysBaseAPI;
import com.lfc.clouddocker.constant.CacheConstant;
import com.lfc.clouddocker.constant.CommonConstant;
import com.lfc.clouddocker.model.SysLoginModel;
import com.lfc.clouddocker.model.entity.LoginUser;
import com.lfc.clouddocker.model.entity.SysUser;
import com.lfc.clouddocker.model.vo.Result;
import com.lfc.clouddocker.service.BaseCommonService;
import com.lfc.clouddocker.service.ISysUserService;
import com.lfc.clouddocker.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/3
 * @Profile:
 */
@Controller
@ResponseBody
@RequestMapping("user")
@Slf4j
public class LoginController {

    @Autowired
    private ISysBaseAPI sysBaseAPI;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ISysUserService sysUserService;

    @Resource
    private BaseCommonService baseCommonService;


    @PostMapping("/login")
    public Result<JSONObject> login(@RequestBody SysLoginModel sysLoginModel) {
        Result<JSONObject> result = new Result<JSONObject>();
        String username = sysLoginModel.getUsername();
        String password = sysLoginModel.getPassword();

        String captcha = sysLoginModel.getCaptcha();
        if (captcha == null) {
            result.error500("验证码无效");
            return result;
        }

        String lowerCaseCaptcha = captcha.toLowerCase();
        String realKey = MD5Util.MD5Encode(lowerCaseCaptcha + sysLoginModel.getCheckKey(), "utf-8");
        Object checkCode = redisUtil.get(realKey);

        //当进入登录页时，有一定几率出现验证码错误 #1714
        if (checkCode == null || !checkCode.toString().equals(lowerCaseCaptcha)) {
            log.warn("验证码错误，key= {} , Ui checkCode= {}, Redis checkCode = {}", sysLoginModel.getCheckKey(), lowerCaseCaptcha, checkCode);
            result.error500("验证码错误");
            return result;
        }

        //1. 校验用户是否有效
        //update-begin-author:wangshuai date:20200601 for: 登录代码验证用户是否注销bug，if条件永远为false
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        SysUser sysUser = sysUserService.getOne(queryWrapper);
        //update-end-author:wangshuai date:20200601 for: 登录代码验证用户是否注销bug，if条件永远为false
        result = sysUserService.checkUserIsEffective(sysUser);
        if (!result.isSuccess()) {
            return result;
        }

//2. 校验用户名或密码是否正确
        String userpassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
        String syspassword = sysUser.getPassword();
        if (!syspassword.equals(userpassword)) {
            result.error500("用户名或密码错误");
            return result;
        }

        //用户登录信息
        userInfo(sysUser, result);
        //update-begin--Author:liusq  Date:20210126  for：登录成功，删除redis中的验证码
        redisUtil.del(realKey);
        //update-begin--Author:liusq  Date:20210126  for：登录成功，删除redis中的验证码
        LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(sysUser, loginUser);
        baseCommonService.addLog("用户名: " + username + ",登录成功！", CommonConstant.LOG_TYPE_1, null, loginUser);
        //update-end--Author:wangshuai  Date:20200714  for：登录日志没有记录人员
        return result;

    }

    /**
     * 短信登录接口
     *
     * @param jsonObject
     * @return
     */
    /*@PostMapping(value = "/sms")
    public Result<String> sms(@RequestBody JSONObject jsonObject) {
        Result<String> result = new Result<String>();
        String mobile = jsonObject.get("mobile").toString();
        //手机号模式 登录模式: "2"  注册模式: "1"
        String smsmode=jsonObject.get("smsmode").toString();
        log.info(mobile);
        if(oConvertUtils.isEmpty(mobile)){
            result.setMessage("手机号不允许为空！");
            result.setSuccess(false);
            return result;
        }
        Object object = redisUtil.get(mobile);
        if (object != null) {
            result.setMessage("验证码10分钟内，仍然有效！");
            result.setSuccess(false);
            return result;
        }

        //随机数
        String captcha = RandomUtil.randomNumbers(6);
        JSONObject obj = new JSONObject();
        obj.put("code", captcha);
        try {
            boolean b = false;
            //注册模板
            if (CommonConstant.SMS_TPL_TYPE_1.equals(smsmode)) {
                SysUser sysUser = sysUserService.getUserByPhone(mobile);
                if(sysUser!=null) {
                    result.error500(" 手机号已经注册，请直接登录！");
                    baseCommonService.addLog("手机号已经注册，请直接登录！", CommonConstant.LOG_TYPE_1, null);
                    return result;
                }
                b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.REGISTER_TEMPLATE_CODE);
            }else {
                //登录模式，校验用户有效性
                SysUser sysUser = sysUserService.getUserByPhone(mobile);
                result = sysUserService.checkUserIsEffective(sysUser);
                if(!result.isSuccess()) {
                    String message = result.getMessage();
                    if("该用户不存在，请注册".equals(message)){
                        result.error500("该用户不存在或未绑定手机号");
                    }
                    return result;
                }

                *//**
                 * smsmode 短信模板方式  0 .登录模板、1.注册模板、2.忘记密码模板
                 *//*
                if (CommonConstant.SMS_TPL_TYPE_0.equals(smsmode)) {
                    //登录模板
                    b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.LOGIN_TEMPLATE_CODE);
                } else if(CommonConstant.SMS_TPL_TYPE_2.equals(smsmode)) {
                    //忘记密码模板
                    b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.FORGET_PASSWORD_TEMPLATE_CODE);
                }
            }

            if (b == false) {
                result.setMessage("短信验证码发送失败,请稍后重试");
                result.setSuccess(false);
                return result;
            }
            //验证码10分钟内有效
            redisUtil.set(mobile, captcha, 600);
            //update-begin--Author:scott  Date:20190812 for：issues#391
            //result.setResult(captcha);
            //update-end--Author:scott  Date:20190812 for：issues#391
            result.setSuccess(true);

        } catch (ClientException e) {
            e.printStackTrace();
            result.error500(" 短信接口未配置，请联系管理员！");
            return result;
        }
        return result;
    }*/


    /**
     * 退出登录
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/logout")
    public Result<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        //用户退出逻辑
        String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
        if (oConvertUtils.isEmpty(token)) {
            return Result.error("退出登录失败！");
        }
        String username = JwtUtil.getUsername(token);
        LoginUser sysUser = sysBaseAPI.getUserByName(username);
        if (sysUser != null) {
            //update-begin--Author:wangshuai  Date:20200714  for：登出日志没有记录人员
            baseCommonService.addLog("用户名: " + sysUser.getUsername() + ",退出成功！", CommonConstant.LOG_TYPE_1, null, sysUser);
            //update-end--Author:wangshuai  Date:20200714  for：登出日志没有记录人员
            log.info(" 用户名:  " + sysUser.getUsername() + ",退出成功！ ");
            //清空用户登录Token缓存
            redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + token);
            //清空用户登录Shiro权限缓存
            redisUtil.del(CommonConstant.PREFIX_USER_SHIRO_CACHE + sysUser.getId());
            //清空用户的缓存信息（包括部门信息），例如sys:cache:user::<username>
            redisUtil.del(String.format("%s::%s", CacheConstant.SYS_USERS_CACHE, sysUser.getUsername()));
            //调用shiro的logout
            SecurityUtils.getSubject().logout();
            return Result.ok("退出登录成功！");
        } else {
            return Result.error("Token无效!");
        }
    }


    /**
     * 用户信息
     *
     * @param sysUser
     * @param result
     * @return
     */
    private Result<JSONObject> userInfo(SysUser sysUser, Result<JSONObject> result) {
        String syspassword = sysUser.getPassword();
        String username = sysUser.getUsername();
        // 获取用户部门信息
        JSONObject obj = new JSONObject();

        // update-begin--Author:sunjianlei Date:20210802 for：获取用户租户信息
        String tenantIds = sysUser.getRelTenantIds();
        if (oConvertUtils.isNotEmpty(tenantIds)) {
            List<Integer> tenantIdList = new ArrayList<>();
            for (String id : tenantIds.split(",")) {
                tenantIdList.add(Integer.valueOf(id));
            }

        }
        // update-end--Author:sunjianlei Date:20210802 for：获取用户租户信息
        // 生成token
        String token = JwtUtil.sign(username, syspassword);
        // 设置token缓存有效时间
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);
        obj.put("token", token);
        obj.put("userInfo", sysUser);
        result.setResult(obj);
        result.success("登录成功");
        return result;
    }

}
