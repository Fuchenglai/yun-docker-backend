package com.lfc.clouddocker.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @Author: 赖富城
 * @CreateTime: 2025/4/3
 * @Profile: 生成端口工具类
 */
public class GeneratePortUtil {

    private static Set<Integer> PORT_SET = new HashSet<>();
    private static Random random = new Random();

    public static Integer generatePort() {
        int num;
        do {
            num = random.nextInt(1000) + 9000;
        } while (!PORT_SET.add(num));

        return num;
    }

}
