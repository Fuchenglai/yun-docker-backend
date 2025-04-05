package com.lfc.clouddocker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lfc.clouddocker.constant.CacheConstant;
import com.lfc.clouddocker.constant.CommonConstant;
import com.lfc.clouddocker.mapper.SysUserMapper;
import com.lfc.clouddocker.model.entity.SysUser;
import com.lfc.clouddocker.model.vo.Result;
import com.lfc.clouddocker.service.ISysUserService;
import com.lfc.clouddocker.util.MinioUtil;
import com.lfc.clouddocker.util.PasswordUtil;
import com.lfc.clouddocker.util.oConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @Author: scott
 * @Date: 2018-12-20
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Resource
    private SysUserMapper userMapper;

    @Value(value = "${jeecg.minio.bucketName}")
    private String bucketName;

    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public Result<?> resetPassword(String username, String oldpassword, String newpassword, String confirmpassword) {
        SysUser user = userMapper.getUserByName(username);
        String passwordEncode = PasswordUtil.encrypt(username, oldpassword, user.getSalt());
        if (!user.getPassword().equals(passwordEncode)) {
            return Result.error("旧密码输入错误!");
        }
        if (oConvertUtils.isEmpty(newpassword)) {
            return Result.error("新密码不允许为空!");
        }
        if (!newpassword.equals(confirmpassword)) {
            return Result.error("两次输入密码不一致!");
        }
        String password = PasswordUtil.encrypt(username, newpassword, user.getSalt());
        this.userMapper.update(new SysUser().setPassword(password), new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, user.getId()));
        return Result.ok("密码重置成功!");
    }

    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public Result<?> changePassword(SysUser sysUser) {
        String salt = oConvertUtils.randomGen(8);
        sysUser.setSalt(salt);
        String password = sysUser.getPassword();
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), password, salt);
        sysUser.setPassword(passwordEncode);
        this.userMapper.updateById(sysUser);
        return Result.ok("密码修改成功!");
    }

    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(String userId) {
        //1.删除头像和电子签名
        SysUser user = getById(userId);
        delFile(user.getAvatar());
        delFile(user.getSignature());

        //2.删除用户
        this.removeById(userId);
        return false;
    }

    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatchUsers(String userIds) {
        List<String> idList = Arrays.asList(userIds.split(","));
        //1.删除文件
        Iterator<String> iterator = idList.iterator();
        SysUser sysUser;
        while (iterator.hasNext()) {
            sysUser = getById(iterator.next());
            delFile(sysUser.getAvatar());
            delFile(sysUser.getSignature());
        }
        //2.删除用户
        this.removeByIds(Arrays.asList(userIds.split(",")));
        return false;
    }

    private void delFile(String file) {
        if (file != null && !file.isEmpty()) {
            String[] parts = file.split("/");
            MinioUtil.removeObject(bucketName, "temp/" + parts[parts.length - 1]);
        }
    }

    @Override
    public SysUser getUserByName(String username) {
        return userMapper.getUserByName(username);
    }




    @Override
    public SysUser getUserByPhone(String phone) {
        return userMapper.getUserByPhone(phone);
    }


    /**
     * 校验用户是否有效
     *
     * @param sysUser
     * @return
     */
    @Override
    public Result<?> checkUserIsEffective(SysUser sysUser) {
        Result<?> result = new Result<Object>();
        //情况1：根据用户信息查询，该用户不存在
        if (sysUser == null) {
            result.error500("该用户不存在，请注册");
            return result;
        }
        //情况2：根据用户信息查询，该用户已注销
        //update-begin---author:王帅   Date:20200601  for：if条件永远为falsebug------------
        if (CommonConstant.DEL_FLAG_1.equals(sysUser.getDelFlag())) {
            //update-end---author:王帅   Date:20200601  for：if条件永远为falsebug------------
            result.error500("该用户已注销");
            return result;
        }
        //情况3：根据用户信息查询，该用户已冻结
        if (CommonConstant.USER_FREEZE.equals(sysUser.getStatus())) {
            result.error500("该用户已冻结");
            return result;
        }
        return result;
    }

    @Override
    public List<SysUser> queryLogicDeleted() {
        return this.queryLogicDeleted(null);
    }

    @Override
    public List<SysUser> queryLogicDeleted(LambdaQueryWrapper<SysUser> wrapper) {
        if (wrapper == null) {
            wrapper = new LambdaQueryWrapper<>();
        }
        wrapper.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_1);
        return userMapper.selectLogicDeleted(wrapper);
    }

    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public boolean revertLogicDeleted(List<String> userIds, SysUser updateEntity) {
        String ids = String.format("'%s'", String.join("','", userIds));
        return userMapper.revertLogicDeleted(ids, updateEntity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeLogicDeleted(List<String> userIds) {
        String ids = String.format("'%s'", String.join("','", userIds));
        // 1. 删除用户
        int line = userMapper.deleteLogicDeleted(ids);
        return line != 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNullPhoneEmail() {
        userMapper.updateNullByEmptyString("email");
        userMapper.updateNullByEmptyString("phone");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUser(SysUser user, String selectedRoles, String selectedDeparts) {
        //step.1 保存用户
        this.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public void editUser(SysUser user, String roles, String departs) {
        //step.1 修改用户基础信息
        this.updateById(user);

        //step.4 修改手机号和邮箱
        // 更新手机号、邮箱空字符串为 null
        userMapper.updateNullByEmptyString("phone");

    }

    @Override
    public List<String> userIdToUsername(Collection<String> userIdList) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUser::getId, userIdList);
        List<SysUser> userList = super.list(queryWrapper);
        return userList.stream().map(SysUser::getUsername).collect(Collectors.toList());
    }

}
