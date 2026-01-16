package com.lfc.yundocker.worker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IP地址服务测试类
 *
 * @author laifucheng
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IPAddressServiceTest {

    @Autowired
    private IPAddressService ipAddressService;

    /**
     * 测试获取内网IP地址
     */
    @Test
    public void testGetLocalIpAddress() {
        String localIp = ipAddressService.getLocalIpAddress();
        System.out.println("内网IP地址: " + localIp);
        assertNotNull(localIp, "内网IP地址不应为null");
        assertFalse(localIp.isEmpty(), "内网IP地址不应为空");
        // 验证IP地址格式
        assertTrue(isValidIp(localIp), "内网IP地址格式不正确: " + localIp);
    }

    /**
     * 测试获取公网IP地址
     */
    @Test
    public void testGetPublicIpAddress() {
        String publicIp = ipAddressService.getPublicIpAddress();
        System.out.println("公网IP地址: " + publicIp);
        // 公网IP可能获取失败，所以不做强制断言
        if (publicIp != null) {
            assertTrue(isValidIp(publicIp), "公网IP地址格式不正确: " + publicIp);
        }
    }

    /**
     * 测试获取完整IP信息
     */
    @Test
    public void testGetIPInfo() {
        IPAddressService.IPInfo ipInfo = ipAddressService.getIPInfo();
        System.out.println("完整IP信息: " + ipInfo);
        assertNotNull(ipInfo, "IP信息不应为null");
        assertNotNull(ipInfo.getLocalIp(), "内网IP不应为null");
        assertTrue(isValidIp(ipInfo.getLocalIp()), "内网IP格式不正确: " + ipInfo.getLocalIp());
    }

    /**
     * 测试刷新公网IP缓存
     */
    @Test
    public void testRefreshPublicIpCache() {
        // 先获取一次公网IP
        String publicIp1 = ipAddressService.getPublicIpAddress();
        System.out.println("第一次获取公网IP: " + publicIp1);
        
        // 刷新缓存
        String publicIp2 = ipAddressService.refreshPublicIpCache();
        System.out.println("刷新后获取公网IP: " + publicIp2);
        
        // 两个IP应该相同（除非在短时间内IP发生变化）
        if (publicIp1 != null && publicIp2 != null) {
            assertEquals(publicIp1, publicIp2, "刷新前后公网IP应该相同");
        }
    }

    /**
     * 测试清除公网IP缓存
     */
    @Test
    public void testClearPublicIpCache() {
        // 先获取一次公网IP以填充缓存
        ipAddressService.getPublicIpAddress();
        
        // 清除缓存
        ipAddressService.clearPublicIpCache();
        
        // 再次获取时应该会重新调用API
        String publicIp = ipAddressService.getPublicIpAddress();
        System.out.println("清除缓存后获取公网IP: " + publicIp);
        
        if (publicIp != null) {
            assertTrue(isValidIp(publicIp), "公网IP格式不正确: " + publicIp);
        }
    }

    /**
     * 验证IP地址格式
     *
     * @param ip IP地址
     * @return 是否为有效的IP地址
     */
    private boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // IP地址正则表达式
        String ipRegex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(ipRegex);
    }
}
