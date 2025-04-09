package com.lfc.clouddocker.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/3/31
 * @Profile:
 */
public class MyDockerExample {
    /*public static void main(String[] args) throws InterruptedException {

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://114.215.186.123:2375")
                .withDockerTlsVerify(false)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        dockerClient.pingCmd().exec();
        //获取默认的docker client
        // DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        *//*PingCmd pingCmd = dockerClient.pingCmd();
        pingCmd.exec();
        String image="bendahl/hello-docker";
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback=new PullImageResultCallback(){

            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("下载镜像："+image);
                super.onNext(item);
            }
        };

        pullImageCmd.exec(pullImageResultCallback).awaitCompletion();*//*

    }*/

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("开始了");
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://114.215.205.22:2375")
                .withApiVersion("1.41")
                .build();

        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient
                .Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        //DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://114.215.205.22:2375").build();
        //System.out.println("开始了");

        /*CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd("52209645d450");
        createContainerCmd.withExposedPorts(ExposedPort.tcp(8080))
                .withNetworkDisabled(false)
                .withPortBindings(PortBinding.parse("8080:8080"));
        CreateContainerResponse containerResponse = createContainerCmd.exec();
        System.out.println("结果：" + containerResponse.getRawValues().toString());

        dockerClient.startContainerCmd(containerResponse.getId()).exec();*/


        //dockerClient.removeContainerCmd("9e744ce44a4a").withForce(true).exec();

        /*InspectImageResponse imageResponse = dockerClient.inspectImageCmd("nginx:alpine").exec();
        System.out.println(imageResponse.getId());
        System.out.println(imageResponse.getSize());*/

        /*Statistics containerStats = dockerClient.statsCmd("230a049f5f30").exec(new InvocationBuilder.AsyncResultCallback<>()).awaitResult();
        long memoryUsageInBytes = 0;
        if (containerStats != null && containerStats.getMemoryStats() != null) {
            memoryUsageInBytes = containerStats.getMemoryStats().getUsage();
        }
        // 将字节数转换为 MB
        double memory = NumberUtil.div(memoryUsageInBytes, 1024.0 * 1024.0, 1);

        System.out.println("内存使用量：" + memory);*/

        StatsCmd statsCmd = dockerClient.statsCmd("61e2f27190d9");
        ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

            @Override
            public void onNext(Statistics statistics) {
                // todo 这里要改成websocket向前端传输
                System.out.println("统计信息：" + statistics.toString());
            }

            @Override
            public void onError(Throwable throwable) {
                // todo 报错信息需要返回给用户
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
        Thread.sleep(50000);

        // 有bug，不能正常关闭
        System.out.println("休眠结束，下面开始关闭stats");
        statisticsResultCallback.close();
        Thread.sleep(10000);
        System.out.println("结束了");
    }

}

