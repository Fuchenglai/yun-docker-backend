package com.lfc.clouddocker.docker;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Docker客户端示例
 *
 * @author laifucheng
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

    /*public static void main(String[] args) throws InterruptedException, IOException {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://47.111.108.204:2375")
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

        *//*CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd("52209645d450");
        createContainerCmd.withExposedPorts(ExposedPort.tcp(8080))
                .withNetworkDisabled(false)
                .withPortBindings(PortBinding.parse("8080:8080"));
        CreateContainerResponse containerResponse = createContainerCmd.exec();
        System.out.println("结果：" + containerResponse.getRawValues().toString());

        dockerClient.startContainerCmd(containerResponse.getId()).exec();*//*


        //dockerClient.removeContainerCmd("9e744ce44a4a").withForce(true).exec();

        *//*InspectImageResponse imageResponse = dockerClient.inspectImageCmd("nginx:alpine").exec();
        System.out.println(imageResponse.getId());
        System.out.println(imageResponse.getSize());*//*

     *//*Statistics containerStats = dockerClient.statsCmd("230a049f5f30").exec(new InvocationBuilder.AsyncResultCallback<>()).awaitResult();
        long memoryUsageInBytes = 0;
        if (containerStats != null && containerStats.getMemoryStats() != null) {
            memoryUsageInBytes = containerStats.getMemoryStats().getUsage();
        }
        // 将字节数转换为 MB
        double memory = NumberUtil.div(memoryUsageInBytes, 1024.0 * 1024.0, 1);

        System.out.println("内存使用量：" + memory);*//*

        StatsCmd statsCmd = dockerClient.statsCmd("620ea5bf4cbd");

        ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
            private boolean again = true;

            @Override
            public void onNext(Statistics statistics) {
                if (again) {
                    // todo 这里要改成websocket向前端传输
                    System.out.println("统计信息：" + statistics.toString());
                }

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
                System.out.println("关闭了");
                again = false;
            }
        });

        Thread.sleep(5000);

        // 有bug，不能正常关闭
        System.out.println("休眠结束，下面开始关闭stats和statsCmd");
        System.out.println();
        System.out.println();
        System.out.println();
        *//*statisticsResultCallback.close();
        statsCmd.close();
        // httpClient.close();
        dockerClient.close();

        dockerClient.pingCmd().exec();*//*


        dockerClient.stopContainerCmd("620ea5bf4cbd").exec();
        Thread.sleep(1000);
        dockerClient.restartContainerCmd("620ea5bf4cbd").exec();

        Thread.sleep(8000);
        System.out.println("结束了");
    }*/


    private static final String GLOBAL_LOG_DIR_NAME = "tempLog";
    private static final String GLOBAL_LOG_NAME = "log.txt";

    public static void main(String[] args) throws InterruptedException {

        String userDir = System.getProperty("user.dir");
        String globalLogPathName = userDir + File.separator + GLOBAL_LOG_DIR_NAME;

        //判断全局日志目录是否存在，不存在则创建
        if (!FileUtil.exist(globalLogPathName)) {
            FileUtil.mkdir(globalLogPathName);
        }

        String userLogPath = globalLogPathName + File.separator + GLOBAL_LOG_NAME;
        FileUtil.writeString("", userLogPath, StandardCharsets.UTF_8);


        /*DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://47.111.110.79:2375")
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
        LogContainerResultCallback callback = new LogContainerResultCallback() {
            @Override
            public void onNext(Frame item) {

                // todo 使用websocket输出日志
                System.out.println("日志：" + new String(item.getPayload()));
                FileUtil.appendString(new String(item.getPayload()), userLogPath, StandardCharsets.UTF_8);
                super.onNext(item);
            }
        };
        dockerClient.logContainerCmd("500d96473b2f")
                .withStdErr(true)
                .withStdOut(true)
                .exec(callback).awaitCompletion();
    }*/
    }

}

