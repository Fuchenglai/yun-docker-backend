package com.lfc.yundocker.loadbalance;

import org.apache.dubbo.common.URL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MachineStatus {
    private static final ConcurrentMap<String, AtomicInteger> SERVICE_RESOURCES = new ConcurrentHashMap<>();

    public static int getRich(String ip) {
        AtomicInteger integer = SERVICE_RESOURCES.computeIfAbsent(ip, k -> new AtomicInteger(Integer.MAX_VALUE));
        return integer.get();
    }

    public static void setRich(String ip, int rich) {
        AtomicInteger integer = SERVICE_RESOURCES.computeIfAbsent(ip, k -> new AtomicInteger(rich));
        integer.set(rich);
    }
}
