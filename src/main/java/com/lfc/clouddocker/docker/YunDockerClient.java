package com.lfc.clouddocker.docker;

import cn.hutool.core.util.NumberUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.InvocationBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.lfc.clouddocker.common.ErrorCode;
import com.lfc.clouddocker.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/2
 * @Profile:
 */
@Slf4j
@Component
public class YunDockerClient {

    @Autowired
    private DockerClient defaultClient;

    @Autowired
    private HostConfig hostConfig;

    private static HashMap<Long, ResultCallback<Statistics>> STATS_CMD_MAP;

    static {
        STATS_CMD_MAP = new HashMap<>();
    }

    public InspectImageResponse pullImage(String image) throws InterruptedException {
        PullImageCmd pullImageCmd = defaultClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
            }

            @Override
            public void onNext(PullResponseItem item) {
                log.info("下载镜像：" + image);
                super.onNext(item);
            }
        };
        pullImageCmd.exec(pullImageResultCallback).awaitCompletion();

        //获取镜像的详细信息
        InspectImageResponse imageResponse = defaultClient.inspectImageCmd(image).exec();
        return imageResponse;
    }

    public boolean removeImage(String image) {
        defaultClient.removeImageCmd(image).withForce(true).exec();
        return true;
    }

    /*-----------------------------上面为镜像，下面为容器------------------------------------*/

    public String runCtr(String imageId, Integer hostPort, Integer containerPort, String name) {


        CreateContainerCmd containerCmd = defaultClient.createContainerCmd(imageId);
        /*if (containerPort != null && containerPort != 0) {
            String bindPort = hostPort + ":" + containerPort;
            log.info("绑定端口：{}", bindPort);
            log.info("镜像为：{}", imageId);
            containerCmd.withNetworkDisabled(false)
                    .withExposedPorts(ExposedPort.tcp(containerPort))
                    .withPortBindings(PortBinding.parse(bindPort));
        }*/
        containerCmd.withName(name);
        // 限制内存256M
        hostConfig.withMemory(512 * 1024 * 1024L);
        PortBinding portBinding = new PortBinding(Ports.Binding.bindPort(hostPort), ExposedPort.tcp(containerPort));
        log.info("绑定端口：{}", portBinding);
        hostConfig.withPortBindings(portBinding);
        CreateContainerResponse createContainerResponse = containerCmd.withHostConfig(hostConfig).exec();

        //创建完之后，启动容器
        String id = createContainerResponse.getId();
        defaultClient.startContainerCmd(id).exec();
        return id;
    }

    /**
     * 创建容器时，容器挂载目录
     *
     * @param image
     * @return
     */
    public String createCtr(String image, String filePath) {
        CreateContainerCmd containerCmd = defaultClient.createContainerCmd(image);
        hostConfig.setBinds(new Bind(filePath, new Volume("/app")));
        //限制内存100M
        hostConfig.withMemory(100 * 1000 * 1000L);
        //限制cpu核心数
        // hostConfig.withCpuCount(1L);
        CreateContainerResponse containerResponse = containerCmd
                //下面这4个with可以让你和容器交互，获取容器的输出
                //.withAttachStdin(true)
                //.withAttachStderr(true)
                //.withAttachStdout(true)
                //.withTty(true)  //设置一个交互终端
                //.withCmd("echo", "Hello Docker")
                .withHostConfig(hostConfig)
                .exec();
        //获取到容器id，也就是cid

        return containerResponse.getId();
    }

    public void createCmd(String cid) {

        // docker exec keen_blackwell java -cp /app Main 1 3
        String[] cmdArray = new String[]{"java", "-cp", "/app", "Main", "1", "3"};
        ExecCreateCmdResponse execCreateCmdResponse = defaultClient.execCreateCmd(cid)
                .withCmd(cmdArray)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        String execId = execCreateCmdResponse.getId();
        if (execId != null) {
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onComplete() {
                    //将信息返回给前端
                    super.onComplete();
                }
            };
            defaultClient.execStartCmd(execId).exec(execStartResultCallback);
        }
    }


    public String createCtr(String image) {
        CreateContainerCmd containerCmd = defaultClient.createContainerCmd(image);
        CreateContainerResponse containerResponse = containerCmd
                //下面这4个with可以让你和容器交互，获取容器的输出
                //.withAttachStdin(true)
                //.withAttachStderr(true)
                //.withAttachStdout(true)
                //.withTty(true)  //设置一个交互终端，交互终端也相当于后台守护进程，可以保证程序一直运行状态
                //.withCmd("echo", "Hello Docker")
                .exec();
        //获取到容器id，也就是cid
        return containerResponse.getId();
    }

    final long[] maxMemory = {0L};

    /**
     * 读取容器的统计数据
     *
     * @param cid
     */
    public void readCtrStats(String cid, Long userId) {
        StatsCmd statsCmd = defaultClient.statsCmd(cid);
        ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

            @Override
            public void onNext(Statistics statistics) {
                // todo 这里要改成websocket向前端传输
                log.info("统计信息：内存：{}", statistics.getMemoryStats().toString());
                log.info("统计信息：CPU：{}", statistics.getCpuStats().toString());
                log.info("统计信息：CPU：{}", Objects.requireNonNull(statistics.getNetworks()).toString());

                //log.info(String.valueOf(statistics.getMemoryStats().getUsage()));
                maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
            }

            @Override
            public void onError(Throwable throwable) {
                // todo 报错信息需要返回给用户
                log.error("读取容器统计数据出错：{}", throwable.getMessage());
            }

            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void close() throws IOException {

            }
        });

        statsCmd.exec(statisticsResultCallback);
        STATS_CMD_MAP.put(userId, statisticsResultCallback);
    }

    public void closeStatsCmd(Long userId) {
        ResultCallback<Statistics> resultCallback = STATS_CMD_MAP.get(userId);
        if (resultCallback != null) {
            try {
                resultCallback.close();
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.DOCKER_ERROR, e.getMessage());
            }
        }
        STATS_CMD_MAP.remove(userId);
    }

    /**
     * 获取容器占用的内存
     *
     * @param cid
     * @return
     */
    public double getCtrMemory(String cid) {
        Statistics containerStats = defaultClient.statsCmd(cid).exec(new InvocationBuilder.AsyncResultCallback<>()).awaitResult();
        long memoryUsageInBytes = 0;
        if (containerStats != null && containerStats.getMemoryStats() != null) {
            memoryUsageInBytes = containerStats.getMemoryStats().getUsage();
        }
        // 将字节数转换为 MB
        double memory = NumberUtil.div(memoryUsageInBytes, 1024.0 * 1024.0, 1);  //小数点后一位
        return memory;
    }

    /**
     * 查看所有容器
     *
     * @return
     */
    public List<Container> listContainer() {
        ListContainersCmd listContainersCmd = defaultClient.listContainersCmd();
        List<Container> containers = listContainersCmd.withShowAll(true).exec();
        return containers;
    }

    /**
     * nginx如果没有开服务，没有开守护进程，那么它执行完以后就自动结束，状态变为exited
     */

    /**
     * 启动容器
     *
     * @param cid
     * @return
     */
    public boolean startCtr(String cid) {
        defaultClient.startContainerCmd(cid).exec();
        return true;
    }

    public boolean stopCtr(String cid) {
        defaultClient.stopContainerCmd(cid).exec();
        return true;
    }

    /**
     * 查看日志
     * 这里是异步查看，因为日志可能非常多，不可能一直卡在这里
     *
     * @param cid
     */
    public void logCtr(String cid) {
        LogContainerResultCallback callback = new LogContainerResultCallback() {
            @Override
            public void onNext(Frame item) {
                // todo 使用websocket输出日志
                System.out.println("日志：" + new String(item.getPayload()));
                super.onNext(item);
            }
        };
        defaultClient.logContainerCmd(cid)
                .withStdErr(true)
                .withStdOut(true)
                .exec(callback);
        //如果没有日志输出，可以加一个.awaitCompletion()，阻塞等待日志输出
    }

    /**
     * 删除容器
     *
     * @param cid
     * @return
     */
    public boolean removeCtr(String cid) {
        defaultClient.removeContainerCmd(cid).withForce(true).exec();
        return true;
    }

    /**
     * 重启容器
     *
     * @param containerId
     * @return
     */
    public boolean restartCtr(String containerId) {
        defaultClient.restartContainerCmd(containerId).exec();
        return true;
    }
}
