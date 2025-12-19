package com.lfc.yundocker.loadbalance;

import org.apache.dubbo.common.URL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MachineStatus {
    private static final ConcurrentMap<String, AtomicInteger> SERVICE_RESOURCES = new ConcurrentHashMap<>();

    public static int getRich(URL url){
        String uri = url.toIdentityString();
        AtomicInteger integer = SERVICE_RESOURCES.computeIfAbsent(uri, k -> new AtomicInteger(Integer.MAX_VALUE));
        return integer.get();
    }

}
