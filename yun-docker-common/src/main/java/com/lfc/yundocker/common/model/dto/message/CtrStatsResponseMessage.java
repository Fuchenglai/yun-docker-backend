package com.lfc.yundocker.common.model.dto.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 容器统计响应消息
 *
 * @author laifucheng
 */
@Data
@Accessors(chain = true)
public class CtrStatsResponseMessage implements Serializable {

    /**
     * cpu总的使用使用时间 秒
     */
    private Double cpuTotalUsage;

    /**
     * cpu使用率
     */
    private List<Long> perCpuUsage;

    /**
     * 在线可用的cpu数量
     */
    private Long onlineCpus;

    /**
     * 容器当前占用内存 MB
     */
    private Double memoryUsage;

    /**
     * 容器启动以来占用的最大内存
     */
    private Double memoryMaxUsage;

    /**
     * 容器内存使用限制
     */
    private Double memoryLimit;

    /**
     * 容器内当前运行的进程数量。
     */
    private Long numProcess;

    /**
     * 网络入站流量 KB
     */
    private Double rxBytes;

    /**
     * 网络出站流量 KB
     */
    private Double txBytes;

    /**
     * 磁盘IO传输数据量 KB
     */
    private Double ioValue;

}
