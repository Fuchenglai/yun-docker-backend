package com.lfc.yundocker.service;

import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Container;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface RpcDockerService {

    /**
     * 拉取镜像
     *
     * @param image 镜像名称
     * @return 镜像信息
     * @throws InterruptedException
     */
    InspectImageResponse pullImage(String image) throws InterruptedException;

    /**
     * 删除镜像
     *
     * @param image 镜像名称
     * @return
     */
    boolean removeImage(String image);

    /**
     * 运行容器
     *
     * @param imageId 镜像id
     * @param hostPort 主机端口
     * @param containerPort 容器端口
     * @param name 容器名称
     * @return 容器id
     */
    String runCtr(String imageId, Integer hostPort, Integer containerPort, String name);


    /**
     * 创建容器时，容器挂载目录
     *
     * @param image 镜像名称
     * @return 容器
     */
    String createCtr(String image, String filePath);


    /**
     * 在指定容器中创建并执行命令
     *
     * @param cid 容器ID
     */
    void createCmd(String cid);

    /**
     * 根据指定的镜像名称创建Docker容器，并返回容器ID
     *
     * @param image 指定的Docker镜像名称，用于创建容器
     * @return 创建成功后返回的容器ID
     */
    String createCtr(String image);


    /**
     * 读取容器的统计数据
     *
     * @param cid 容器id
     */
    void readCtrStats(String cid, Long userId);


    /**
     * 关闭监控统计命令
     *
     * @param userId 用户id
     */
    void closeStatsCmd(Long userId);


    /**
     * 获取容器占用的内存
     *
     * @param cid
     * @return 内存大小
     */
    double getCtrMemory(String cid);


    /**
     * 查看所有容器
     *
     * @return
     */
    List<Container> listContainer();


    /**
     * 启动容器
     *
     * @param cid
     * @return
     */
    boolean startCtr(String cid);


    /**
     * 停止容器
     *
     * @param cid 容器ID，用于标识需要停止的容器
     * @return
     */
    boolean stopCtr(String cid);


    /**
     * 查看日志
     * 这里是异步查看，因为日志可能非常多，不可能一直卡在这里
     *
     * @param cid
     */
    void logCtr(String cid, HttpServletResponse response);

    /**
     * 删除容器
     *
     * @param cid
     * @return
     */
    boolean removeCtr(String cid);


    /**
     * 重启容器
     *
     * @param cid
     * @return
     */
    boolean restartCtr(String cid);

}
