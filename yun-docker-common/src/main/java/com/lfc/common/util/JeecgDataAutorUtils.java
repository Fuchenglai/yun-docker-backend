package com.lfc.common.util;

import com.lfc.common.model.vo.SysUserCacheInfo;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName: JeecgDataAutorUtils
 * @Description: 数据权限查询规则容器工具类
 * @Author: 张代浩
 * @Date: 2012-12-15 下午11:27:39
 *
 */
public class JeecgDataAutorUtils {

	public static final String MENU_DATA_AUTHOR_RULES = "MENU_DATA_AUTHOR_RULES";

	public static final String MENU_DATA_AUTHOR_RULE_SQL = "MENU_DATA_AUTHOR_RULE_SQL";

	public static final String SYS_USER_INFO = "SYS_USER_INFO";



	/**
	 * 获取请求对应的数据权限SQL
	 *
	 * @return
	 */
	public static synchronized String loadDataSearchConditonSQLString() {
		return (String) SpringContextUtils.getHttpServletRequest().getAttribute(MENU_DATA_AUTHOR_RULE_SQL);
	}

	/**
	 * 往链接请求里面，传入数据查询条件
	 *
	 * @param request
	 * @param sql
	 */
	public static synchronized void installDataSearchConditon(HttpServletRequest request, String sql) {
		String ruleSql = (String)loadDataSearchConditonSQLString();
		if (!StringUtils.hasText(ruleSql)) {
			request.setAttribute(MENU_DATA_AUTHOR_RULE_SQL,sql);
		}
	}

	/**
	 * 将用户信息存到request
	 * @param request
	 * @param userinfo
	 */
	public static synchronized void installUserInfo(HttpServletRequest request, SysUserCacheInfo userinfo) {
		request.setAttribute(SYS_USER_INFO, userinfo);
	}

	/**
	 * 将用户信息存到request
	 * @param userinfo
	 */
	public static synchronized void installUserInfo(SysUserCacheInfo userinfo) {
		SpringContextUtils.getHttpServletRequest().setAttribute(SYS_USER_INFO, userinfo);
	}

	/**
	 * 从request获取用户信息
	 * @return
	 */
	public static synchronized SysUserCacheInfo loadUserInfo() {
		return (SysUserCacheInfo) SpringContextUtils.getHttpServletRequest().getAttribute(SYS_USER_INFO);

	}
}
