package com.lfc.common.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * docker客户端配置
 *
 * @author laifucheng
 */
@Configuration
public class DockerClientConfig {

    @Value("${docker.server.url}")
    private String dockerServerUrl;

    @Bean
    public DockerClient defaultClient() {
        return DockerClientBuilder.getInstance(dockerServerUrl).build();
    }

    @Bean
    public HostConfig hostConfig() {
        // hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
        return new HostConfig();
    }
}
