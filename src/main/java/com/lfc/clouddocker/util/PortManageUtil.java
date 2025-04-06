package com.lfc.clouddocker.util;

import java.util.*;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/3
 * @Profile: 端口管理工具类
 */
public class PortManageUtil {

    private static Set<Integer> PORT_SET = new HashSet<>();
    private static Random random = new Random();
    private static final Map<String, Integer> PUBLIC_IMAGE_PORT_MAP = new HashMap<>();

    static {
        PUBLIC_IMAGE_PORT_MAP.put("mysql", 3306);
        PUBLIC_IMAGE_PORT_MAP.put("redis", 6379);
        PUBLIC_IMAGE_PORT_MAP.put("kafka", 9092);
        PUBLIC_IMAGE_PORT_MAP.put("zookeeper", 2181);
        PUBLIC_IMAGE_PORT_MAP.put("nginx", 80);
    }

    public static boolean delOccupyPort(Integer port) {
        return PORT_SET.remove(port);
    }

    public static Integer getPublicHostPort(String publicImage) {
        Integer i = PUBLIC_IMAGE_PORT_MAP.get(publicImage);
        if (PORT_SET.add(i)) {
            return i;
        }
        return generatePort();
    }

    public static Integer getPublicContainerPort(String publicImage) {
        return PUBLIC_IMAGE_PORT_MAP.get(publicImage);
    }

    public static Integer generatePort() {
        int num;
        do {
            num = random.nextInt(1000) + 9000;
        } while (!PORT_SET.add(num));

        PORT_SET.add(num);
        return num;
    }

    public static boolean isValidPort(Integer port) {
        return !PORT_SET.contains(port);
    }

}
