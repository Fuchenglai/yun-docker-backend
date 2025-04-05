package com.lfc.clouddocker.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.time.Duration;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/3/31
 * @Profile:
 */
public class MyDockerExample {
    public static void main(String[] args) throws InterruptedException {

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:2375")
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
        /*PingCmd pingCmd = dockerClient.pingCmd();
        pingCmd.exec();*/
        /*String image="bendahl/hello-docker";
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback=new PullImageResultCallback(){

            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("下载镜像："+image);
                super.onNext(item);
            }
        };

        pullImageCmd.exec(pullImageResultCallback).awaitCompletion();*/

    }
}
