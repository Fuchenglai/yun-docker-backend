package com.lfc.clouddocker.model.vo;

import com.lfc.clouddocker.model.entity.YunContainer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 容器视图层
 *
 * @author laifucheng
 */

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ContainerVO implements Serializable {

    /**
     * 容器 id
     */
    private String containerId;

    /**
     * 命令
     */
    private String command;

    /**
     * 状态
     * exited
     * running
     * paused
     * restarting
     */
    private String status;

    /**
     * 端口
     */
    private String ports;

    /**
     * 容器 ip
     */
    private String ip;

    /**
     * 容器名称
     */
    private String containerName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * repository:tag
     */
    private String image;

    /**
     * 对象转包装类
     *
     * @param container
     * @return
     */
    public static ContainerVO objToVo(YunContainer container) {
        if (container == null) {
            return null;
        }
        ContainerVO containerVO = new ContainerVO();
        BeanUtils.copyProperties(container, containerVO);

        return containerVO;
    }
}
