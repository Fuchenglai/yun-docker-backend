package com.lfc.yundocker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

// todo 如需开启 Redis，须移除 exclude 中的内容
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@MapperScan("com.lfc.yundocker.mapper")
@EnableScheduling
@EnableWebSocket
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class YunDockerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YunDockerApplication.class, args);
    }
}
