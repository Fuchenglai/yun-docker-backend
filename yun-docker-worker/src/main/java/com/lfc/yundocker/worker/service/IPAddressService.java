package com.lfc.yundocker.worker.service;

import com.lfc.yundocker.common.util.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * IP地址服务类
 * 提供当前运行机器的IP地址获取功能
 *
 * @author laifucheng
 */
@Service
public class IPAddressService {

    private static final Logger logger = LoggerFactory.getLogger(IPAddressService.class);

    /**
     * 获取当前机器的内网IP地址
     *
     * @return 内网IP地址
     */
    public String getLocalIpAddress() {
        String localIp = NetUtils.getLocalIpAddress();
        logger.debug("获取到内网IP地址: {}", localIp);
        return localIp;
    }

    /**
     * 获取当前机器的公网IP地址
     *
     * @return 公网IP地址，如果获取失败返回null
     */
    public String getPublicIpAddress() {
        String publicIp = NetUtils.getPublicIpAddress();
        if (publicIp != null) {
            logger.debug("获取到公网IP地址: {}", publicIp);
        } else {
            logger.warn("获取公网IP地址失败");
        }
        return publicIp;
    }

    /**
     * 手动刷新公网IP缓存
     *
     * @return 新的公网IP地址，如果获取失败返回null
     */
    public String refreshPublicIpCache() {
        logger.info("手动刷新公网IP缓存");
        String publicIp = NetUtils.refreshPublicIpCache();
        if (publicIp != null) {
            logger.info("公网IP缓存已更新: {}", publicIp);
        } else {
            logger.error("刷新公网IP缓存失败");
        }
        return publicIp;
    }

    /**
     * 清除公网IP缓存
     */
    public void clearPublicIpCache() {
        logger.info("清除公网IP缓存");
        NetUtils.clearPublicIpCache();
    }

    /**
     * 获取完整的IP地址信息
     *
     * @return 包含内网和公网IP的信息对象
     */
    public IPInfo getIPInfo() {
        String localIp = getLocalIpAddress();
        String publicIp = getPublicIpAddress();
        
        return new IPInfo(localIp, publicIp);
    }

    /**
     * IP地址信息对象
     */
    public static class IPInfo {
        private String localIp;
        private String publicIp;

        public IPInfo(String localIp, String publicIp) {
            this.localIp = localIp;
            this.publicIp = publicIp;
        }

        public String getLocalIp() {
            return localIp;
        }

        public String getPublicIp() {
            return publicIp;
        }

        @Override
        public String toString() {
            return "IPInfo{" +
                    "localIp='" + localIp + '\'' +
                    ", publicIp='" + publicIp + '\'' +
                    '}';
        }
    }
}
