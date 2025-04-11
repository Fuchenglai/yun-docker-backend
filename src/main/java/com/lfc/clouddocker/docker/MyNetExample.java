package com.lfc.clouddocker.docker;

import java.net.InetAddress;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/11
 * @Profile:
 */
public class MyNetExample {
    public static void main(String[] args) {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("Local IP Address: " + localHost.getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
