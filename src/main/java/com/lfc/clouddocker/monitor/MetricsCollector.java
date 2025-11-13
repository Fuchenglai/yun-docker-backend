package com.lfc.clouddocker.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class MetricsCollector {
    @Resource
    private MeterRegistry meterRegistry;

    // 缓存已创建的指标，避免重复创建（按指标类型分离缓存）
    private final ConcurrentMap<String, Counter> requestCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> errorCountersCache = new ConcurrentHashMap<>();
    //private final ConcurrentMap<String, Counter> tokenCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> responseTimersCache = new ConcurrentHashMap<>();

    /**
     * 记录请求次数
     */
    public void recordRequest(String userId, String imageName, String status) {
        String key = String.format("%s_%s_%s", userId, imageName, status);
        Counter counter = requestCountersCache.computeIfAbsent(key, k -> Counter.builder("image_request_total")
                .description("镜像被创建的总次数")
                .tag("user_id", userId)
                .tag("image_name", imageName)
                .tag("status", status)
                .register(meterRegistry));
        counter.increment();
    }

    /**
     * 记录错误次数
     */
    public void recordError(String userId, String imageName, String errorMessage) {
        String key = String.format("%s_%s_%s", userId, imageName, errorMessage);
        Counter counter = errorCountersCache.computeIfAbsent(key, k -> Counter.builder("image_errors_total")
                .description("镜像创建容器时的错误总次数")
                .tag("user_id", userId)
                .tag("image_name", imageName)
                .tag("status", errorMessage)
                .register(meterRegistry));
        counter.increment();
    }

    /**
     * 记录响应时间
     */
    public void recordResponseTime(String userId, String imageName, Duration duration) {
        String key = String.format("%s_%s", userId, imageName);
        Timer timer = responseTimersCache.computeIfAbsent(key, k -> Timer.builder("image_response_duration_seconds")
                .description("镜像创建容器时的响应时间")
                .tag("user_id", userId)
                .tag("image_name", imageName)
                .register(meterRegistry));
        timer.record(duration);
    }


}
