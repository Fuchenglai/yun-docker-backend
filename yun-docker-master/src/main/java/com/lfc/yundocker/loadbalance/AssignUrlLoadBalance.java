package com.lfc.yundocker.loadbalance;

import com.lfc.yundocker.service.YunContainerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AssignUrlLoadBalance extends AbstractLoadBalance {

    @Autowired
    private YunContainerService yunContainerService;

    public static final String NAME = "assignurl";

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {

        // todo打印一下invoker的真实url

        // 获取调用方法的参数
        Object[] args = invocation.getArguments();
        if (args == null || args.length == 0) {
            return invokers.get(0);
        }

        String workerUrl = yunContainerService.getWorkerUrl((String) args[0]);
        log.info("call method: " + invocation.getMethodName() + ", args[0]: " + args[0]);
        if (workerUrl == null || workerUrl.trim().isEmpty()) {
            return invokers.get(0);
        }
        log.info(" workerUrl: " + workerUrl);

        for (Invoker<T> invoker : invokers) {
            if (invoker.getUrl().toFullString().equals(workerUrl)) {
                return invoker;
            }
        }

        // 如果没有找到匹配的worker，返回第一个可用的invoker
        return invokers.isEmpty() ? null : invokers.get(0);
    }
}
