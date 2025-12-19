package com.lfc.yundocker.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance;

import java.util.List;

/**
 * 自定义负载均衡策略
 *
 * @author ceng
 */
@Slf4j
public class FirstCallLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "firstcall";


    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {

        if (invokers == null || invokers.isEmpty()) {
            throw new IllegalArgumentException("Invokers list cannot be null or empty");
        }

        int length = invokers.size();
        int leastRich = -1;
        int maxResource = Integer.MIN_VALUE;

        for (int i = 0; i < length; ++i) {
            Invoker<T> invoker = (Invoker) invokers.get(i);
            int resource = MachineStatus.getRich(invoker.getUrl());
            if (resource > maxResource) {
                maxResource = resource;
                leastRich = i;
            }
        }

        if (leastRich == -1) {
            throw new IllegalStateException("No invoker available");
        }
        return invokers.get(leastRich);
    }
}
