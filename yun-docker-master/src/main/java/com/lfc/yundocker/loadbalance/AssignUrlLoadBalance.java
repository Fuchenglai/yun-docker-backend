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

        //入参url的内容eg:
        //dubbo://192.168.88.1/com.lfc.yundocker.service.RpcDockerService?anyhost=true&application=yun-docker-master&check=false&deprecated=false&dubbo=2.0.2
        // &dynamic=true&generic=false&init=false&interface=com.lfc.yundocker.service.RpcDockerService&loadbalance=assignurl&metadata-type=remote
        // &methods=restartCtr,sayHello,readCtrStats,startCtr,closeStatsCmd,pullImage,logCtr,createCtr,removeImage,stopCtr,removeCtr,getCtrMemory,createCmd,listContainer,runCtr&pid=23796&qos.enable=false&register.ip=192.168.88.1&release=2.7.21
        // &remote.application=yun-docker-worker&retries=0&service.name=ServiceBean:/com.lfc.yundocker.service.RpcDockerService&side=consumer&sticky=false&timeout=5000&timestamp=1766224558319

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
            if (invoker.getUrl().getIp().equals(workerUrl)) {
                return invoker;
            }
        }

        // 如果没有找到匹配的worker，返回第一个可用的invoker
        return invokers.isEmpty() ? null : invokers.get(0);
    }
}
