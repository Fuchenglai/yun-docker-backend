package com.lfc.yundocker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lfc.yundocker.common.ErrorCode;
import com.lfc.yundocker.exception.BusinessException;
import com.lfc.yundocker.mapper.YunContainerMapper;
import com.lfc.yundocker.mapper.YunPortMapper;
import com.lfc.yundocker.model.entity.YunPort;
import com.lfc.yundocker.service.YunPortService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author ceng
 * @description 针对表【yun_port(端口)】的数据库操作Service实现
 * @createDate 2025-04-05 14:47:22
 */
@Service
@Slf4j
public class YunPortServiceImpl extends ServiceImpl<YunPortMapper, YunPort>
        implements YunPortService {

    @Resource
    private YunContainerMapper yunContainerMapper;

    @Value("${docker.open-port.max}")
    private Integer maxPort;

    @Value("${docker.open-port.min}")
    private Integer minPort;


    private Random random = new Random();
    private static final Map<String, Integer> PUBLIC_IMAGE_PORT_MAP = new HashMap<String, Integer>() {{
        put("mysql", 3306);
        put("redis", 6379);
        put("kafka", 9092);
        put("zookeeper", 2181);
        put("nginx", 80);
        put("yun-docker-demo", 9000);
    }};

    @Override
    public Integer getPublicContainerPort(String publicImage) {
        return PUBLIC_IMAGE_PORT_MAP.get(publicImage);
    }

    @Override
    public Integer generatePort() {
        int num;
        int count = 0;
        do {
            num = random.nextInt(maxPort - minPort) + minPort;
            count++;
            if (count > maxPort - minPort) {
                throw new BusinessException(ErrorCode.HOST_ERROR, "无可用端口，请稍后再试");
            }
        } while (!isValidPort(num));  // 生成随机端口，直到找到一个有效的端口
        return num;
    }

    @Override
    public boolean isValidPort(Integer port) {
        List<String> ports = yunContainerMapper.selectPortsList();
        if (ports.isEmpty()) {
            return true;
        }

        Set<Integer> hostPortSet = new HashSet<>();
        //格式转换
        for (String portStr : ports) {
            String[] parts = portStr.split(":");
            if (parts.length > 0) {
                try {
                    int hostPort = Integer.parseInt(parts[0].trim());
                    hostPortSet.add(hostPort);
                } catch (NumberFormatException e) {
                    // 处理转换失败的情况，记录日志
                    log.error("端口号转换失败: {}", parts[0], e);
                }
            }
        }

        return !hostPortSet.contains(port);
    }
}




