package com.lfc.clouddocker.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求，因为删除请求一般都是传id的
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
