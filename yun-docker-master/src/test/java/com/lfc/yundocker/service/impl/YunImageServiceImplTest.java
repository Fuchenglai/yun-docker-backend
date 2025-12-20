package com.lfc.yundocker.service.impl;

import com.lfc.yundocker.config.WebsocketConfig;
import com.lfc.yundocker.service.RpcDockerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import javax.annotation.Resource;

/**
 * 镜像服务测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class YunImageServiceImplTest {

    @Resource
    private YunImageServiceImpl yunImageService;

    @Test
    void callSayHello() throws InterruptedException {

        for (int i = 0; i < 10; i++) {
            // 测试用例1：传入正常参数
            String name = "testUser" + i;
            //yunImageService.callSayHello(name);
            Thread.sleep(2000);
        }


    }
}
