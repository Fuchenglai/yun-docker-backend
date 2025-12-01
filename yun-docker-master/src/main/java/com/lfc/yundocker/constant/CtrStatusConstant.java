package com.lfc.yundocker.constant;

/**
 * 用户常量
 *
 * @author laifucheng
 */
public interface CtrStatusConstant {

    /**
     * 已结束
     */
    String EXITED = "exited";

    /**
     * 运行中
     */
    String RUNNING = "running";

    /**
     * 暂停中
     */
    String PAUSED = "paused";

    /**
     * 重启中
     */
    String RESTARTING = "restarting";
}
