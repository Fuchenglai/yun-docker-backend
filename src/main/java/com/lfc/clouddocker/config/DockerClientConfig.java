package com.lfc.clouddocker.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/1
 * @Profile:
 */
@Configuration
public class DockerClientConfig {

    @Bean
    public DockerClient defaultClient() {
        return DockerClientBuilder.getInstance("tcp://114.215.205.22:2375").build();
    }

    @Bean
    public HostConfig hostConfig() {
        // hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
        return new HostConfig();
    }
}
