package com.lfc.clouddocker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lfc.clouddocker.model.entity.SysUser;
import com.lfc.clouddocker.model.vo.Result;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
public interface ISysUserService extends IService<SysUser> {

	/**
	 * 重置密码
	 *
	 * @param username
	 * @param oldpassword
	 * @param newpassword
	 * @param confirmpassword
	 * @return
	 */
	public Result<?> resetPassword(String username, String oldpassword, String newpassword, String confirmpassword);

	/**
	 * 修改密码
	 *
	 * @param sysUser
	 * @return
	 */
	public Result<?> changePassword(SysUser sysUser);

	/**
	 * 删除用户
	 * @param userId
	 * @return
	 */
	public boolean deleteUser(String userId);

	/**
	 * 批量删除用户
	 * @param userIds
	 * @return
	 */
	public boolean deleteBatchUsers(String userIds);

	public SysUser getUserByName(String username);


	/**
	 * 根据手机号获取用户名和密码
	 */
	public SysUser getUserByPhone(String phone);


	/**
	   * 校验用户是否有效
	 * @param sysUser
	 * @return
	 */
	Result checkUserIsEffective(SysUser sysUser);

	/**
	 * 查询被逻辑删除的用户
	 */
	List<SysUser> queryLogicDeleted();

	/**
	 * 查询被逻辑删除的用户（可拼装查询条件）
	 */
	List<SysUser> queryLogicDeleted(LambdaQueryWrapper<SysUser> wrapper);

	/**
	 * 还原被逻辑删除的用户
	 */
	boolean revertLogicDeleted(List<String> userIds, SysUser updateEntity);

	/**
	 * 彻底删除被逻辑删除的用户
	 */
	boolean removeLogicDeleted(List<String> userIds);

    /**
     * 更新手机号、邮箱空字符串为 null
     */
    @Transactional(rollbackFor = Exception.class)
    boolean updateNullPhoneEmail();


	/**
	 * 保存用户
	 * @param user 用户
	 * @param selectedRoles 选择的角色id，多个以逗号隔开
	 * @param selectedDeparts 选择的部门id，多个以逗号隔开
	 */
	void saveUser(SysUser user, String selectedRoles, String selectedDeparts);

	/**
	 * 编辑用户
	 * @param user 用户
	 * @param roles 选择的角色id，多个以逗号隔开
	 * @param departs 选择的部门id，多个以逗号隔开
	 */
	void editUser(SysUser user, String roles, String departs);

	/** userId转为username */
	List<String> userIdToUsername(Collection<String> userIdList);

}
