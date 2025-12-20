package com.lfc.yundocker.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

/**
 * 网络工具类
 *
 * @author laifucheng
 */
public class NetUtils {

    private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);

    // 公网IP缓存
    private static String cachedPublicIp;
    private static long lastPublicIpUpdateTime = 0;
    // 缓存有效期（1小时）
    private static final long PUBLIC_IP_CACHE_TTL = TimeUnit.HOURS.toMillis(1);

    // 公网IP获取API列表
    private static final String[] PUBLIC_IP_APIS = {
            "https://api.ipify.org",
            "https://ifconfig.me/ip",
            "https://icanhazip.com",
            "https://ident.me"
    };

    /**
     * 获取客户端 IP 地址
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1")) {
                // 根据网卡取本机配置的 IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    logger.error("获取本地主机地址失败", e);
                }
                if (inet != null) {
                    ip = inet.getHostAddress();
                }
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        if (ip == null) {
            return "127.0.0.1";
        }
        return ip;
    }

    /**
     * 获取本机内网IP地址
     *
     * @return 内网IP地址，如果获取失败返回127.0.0.1
     */
    public static String getLocalIpAddress() {
        try {
            // 枚举所有网络接口
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                
                // 过滤掉回环接口、非活动接口、虚拟接口
                if (networkInterface.isLoopback() || !networkInterface.isUp() || networkInterface.isVirtual()) {
                    continue;
                }
                
                // 获取接口下的所有IP地址
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    
                    // 过滤掉IPv6地址
                    if (inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
            
            // 如果没有找到有效IP，返回回环地址
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            logger.error("获取本地IP地址失败", e);
            return "127.0.0.1";
        }
    }

    /**
     * 获取本机公网IP地址（带缓存机制）
     *
     * @return 公网IP地址，如果获取失败返回null
     */
    public static String getPublicIpAddress() {
        // 检查缓存是否有效
        long currentTime = System.currentTimeMillis();
        if (cachedPublicIp != null && (currentTime - lastPublicIpUpdateTime) < PUBLIC_IP_CACHE_TTL) {
            return cachedPublicIp;
        }
        
        // 缓存无效，重新获取
        String publicIp = getPublicIpAddressWithFallback();
        if (publicIp != null) {
            cachedPublicIp = publicIp;
            lastPublicIpUpdateTime = currentTime;
        }
        
        return publicIp;
    }

    /**
     * 带备用机制的公网IP获取
     *
     * @return 公网IP地址，如果所有API都失败返回null
     */
    public static String getPublicIpAddressWithFallback() {
        for (String apiUrl : PUBLIC_IP_APIS) {
            try {
                String ip = getPublicIpFromApi(apiUrl);
                if (ip != null && !ip.isEmpty()) {
                    return ip.trim();
                }
            } catch (Exception e) {
                logger.warn("调用公网IP API失败: {}", apiUrl, e);
                // 继续尝试下一个API
            }
        }
        
        logger.error("所有公网IP获取API都失败");
        return null;
    }

    /**
     * 从指定API获取公网IP地址
     *
     * @param apiUrl API地址
     * @return 公网IP地址
     * @throws IOException
     */
    private static String getPublicIpFromApi(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP响应码错误: " + responseCode);
            }
            
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            return response.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("关闭流失败", e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 手动刷新公网IP缓存
     *
     * @return 新的公网IP地址，如果获取失败返回null
     */
    public static String refreshPublicIpCache() {
        String publicIp = getPublicIpAddressWithFallback();
        if (publicIp != null) {
            cachedPublicIp = publicIp;
            lastPublicIpUpdateTime = System.currentTimeMillis();
        }
        return publicIp;
    }

    /**
     * 清除公网IP缓存
     */
    public static void clearPublicIpCache() {
        cachedPublicIp = null;
        lastPublicIpUpdateTime = 0;
    }

}
