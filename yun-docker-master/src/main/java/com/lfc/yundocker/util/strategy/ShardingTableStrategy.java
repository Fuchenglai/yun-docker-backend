package com.lfc.yundocker.util.strategy;

/**
 * 分表策略接口
 *
 * @author laifucheng
 */
public interface ShardingTableStrategy {

    /**
     * 获取分表结果
     *
     * @param externalId 外部id
     * @param tableCount 表数量
     * @return 分表结果
     */
    public int getTable(String externalId, int tableCount);
}
