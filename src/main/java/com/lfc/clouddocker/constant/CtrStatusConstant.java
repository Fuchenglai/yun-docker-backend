package com.lfc.clouddocker.constant;

/**
 * 用户常量
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
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
