package com.lfc.clouddocker.util;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/7
 * @Profile:
 */
public class RandomNameUtil {
    private static final String[] ADJECTIVES = {
            "快乐", "机智", "温柔", "阳光", "勇敢",
            "优雅", "神秘", "安静", "活泼", "聪明",
            "幸运", "幽默", "坚韧", "热情", "自由",
            "逍遥", "月光", "森林", "海洋", "星辰"
    };

    private static final String[] NAMES = {
            "熊猫", "猎豹", "海豚", "向日葵", "枫叶",
            "雪山", "鲸鱼", "蝴蝶", "银杏", "极光",
            "飞鸟", "流星", "竹叶", "珊瑚", "蒲公英",
            "白鹤", "松果", "溪流", "麦穗", "萤火虫"
    };

    public static String generateRandomName() {
        int adjectiveIndex = (int) (Math.random() * ADJECTIVES.length);
        int nameIndex = (int) (Math.random() * NAMES.length);
        return ADJECTIVES[adjectiveIndex] + NAMES[nameIndex];
    }

}
