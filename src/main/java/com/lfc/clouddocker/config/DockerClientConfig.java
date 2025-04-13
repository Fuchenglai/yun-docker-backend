package com.lfc.clouddocker.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/1
 * @Profile:
 */
@Configuration
public class DockerClientConfig {

    @Value("${docker.client.url}")
    private String dockerClientUrl;

    @Bean
    public DockerClient defaultClient() {
        return DockerClientBuilder.getInstance(dockerClientUrl).build();
    }

    @Bean
    public HostConfig hostConfig() {
        // hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
        return new HostConfig();
    }
}
