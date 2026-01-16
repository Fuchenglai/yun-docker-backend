package com.lfc.yundocker.worker.rpc;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.InvocationBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.lfc.yundocker.common.exception.BusinessException;
import com.lfc.yundocker.common.model.dto.message.CtrStatsResponseMessage;
import com.lfc.yundocker.common.model.enums.ErrorCode;
import com.lfc.yundocker.service.RpcDockerService;
import com.lfc.yundocker.service.dto.ImageResponseDTO;
import com.lfc.yundocker.worker.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Slf4j
@DubboService
@Component
public class RpcDockerServiceImpl implements RpcDockerService {
    @Autowired
    private DockerClient defaultClient;

    @Autowired
    private HostConfig hostConfig;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    private static HashMap<Long, ResultCallback<Statistics>> STATS_CMD_MAP;

    private static final String GLOBAL_LOG_DIR_NAME = "yun-docker-master/tempLog";
    private static final String GLOBAL_LOG_NAME = "log.txt";

    static {
        STATS_CMD_MAP = new HashMap<>();
    }

    @Override
    public ImageResponseDTO pullImage(String image) throws InterruptedException {
        log.info("pullImage方法开始被调用：" + image);
        PullImageCmd pullImageCmd = defaultClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
            }

            @Override
            public void onNext(PullResponseItem item) {
                log.info("下载镜像中：" + image);
                super.onNext(item);
            }
        };
        pullImageCmd.exec(pullImageResultCallback).awaitCompletion();

        //获取镜像的详细信息
        InspectImageResponse imageResponse = defaultClient.inspectImageCmd(image).exec();
        if(Objects.isNull(imageResponse)){
            throw new BusinessException(ErrorCode.DOCKER_ERROR);
        }
        log.info("下载镜像成功：" + image);
        return new ImageResponseDTO(imageResponse);
    }

    @Override
    public boolean removeImage(String image) {
        defaultClient.removeImageCmd(image).withForce(true).exec();
        return true;
    }

    /*-----------------------------上面为镜像，下面为容器------------------------------------*/

    @Override
    public String runCtr(String imageId, Integer hostPort, Integer containerPort, String name) {


        CreateContainerCmd containerCmd = defaultClient.createContainerCmd(imageId);
        /*if (containerPort != null && containerPort != 0) {
            String bindPort = hostPort + ":" + containerPort;
            log.info("绑定端口：{}", bindPort);
            log.info("镜像为：{}", imageId);
            containerCmd.withNetworkDisabled(false)
                    .withExposedPorts(ExposedPort.tcp(containerPort))
                    .withPortBindings(PortBinding.parse(bindPort));
        }*/
        containerCmd.withName(name);
        // 限制内存256M
        hostConfig.withMemory(512 * 1024 * 1024L);
        PortBinding portBinding = new PortBinding(Ports.Binding.bindPort(hostPort), ExposedPort.tcp(containerPort));
        log.info("绑定端口：{}", portBinding);
        hostConfig.withPortBindings(portBinding);
        CreateContainerResponse createContainerResponse = containerCmd.withHostConfig(hostConfig).exec();

        //创建完之后，启动容器
        String id = createContainerResponse.getId();
        defaultClient.startContainerCmd(id).exec();
        return id;
    }

    /**
     * 创建容器时，容器挂载目录
     *
     * @param image
     * @return
     */
    @Override
    public String createCtr(String image, String filePath) {
        CreateContainerCmd containerCmd = defaultClient.createContainerCmd(image);
        hostConfig.setBinds(new Bind(filePath, new Volume("/app")));
        //限制内存100M
        hostConfig.withMemory(100 * 1000 * 1000L);
        //限制cpu核心数
        // hostConfig.withCpuCount(1L);
        CreateContainerResponse containerResponse = containerCmd
                //下面这4个with可以让你和容器交互，获取容器的输出
                //.withAttachStdin(true)
                //.withAttachStderr(true)
                //.withAttachStdout(true)
                //.withTty(true)  //设置一个交互终端
                //.withCmd("echo", "Hello Docker")
                .withHostConfig(hostConfig)
                .exec();
        //获取到容器id，也就是cid

        return containerResponse.getId();
    }

    /**
     * 在指定容器中创建并执行命令
     *
     * @param cid 容器ID
     */
    @Override
    public void createCmd(String cid) {

        // docker exec keen_blackwell java -cp /app Main 1 3
        String[] cmdArray = new String[]{"java", "-cp", "/app", "Main", "1", "3"};
        ExecCreateCmdResponse execCreateCmdResponse = defaultClient.execCreateCmd(cid)
                .withCmd(cmdArray)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        String execId = execCreateCmdResponse.getId();
        if (execId != null) {
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onComplete() {
                    //将信息返回给前端
                    super.onComplete();
                }
            };
            defaultClient.execStartCmd(execId).exec(execStartResultCallback);
        }
    }


    /**
     * 根据镜像名称创建Docker容器，并返回容器ID
     *
     * @param image 指定的Docker镜像名称，用于创建容器
     * @return 创建成功后返回的容器ID
     */
    @Override
    public String createCtr(String image) {
        CreateContainerCmd containerCmd = defaultClient.createContainerCmd(image);
        CreateContainerResponse containerResponse = containerCmd
                //下面这4个with可以让你和容器交互，获取容器的输出
                //.withAttachStdin(true)
                //.withAttachStderr(true)
                //.withAttachStdout(true)
                //.withTty(true)  //设置一个交互终端，交互终端也相当于后台守护进程，可以保证程序一直运行状态
                //.withCmd("echo", "Hello Docker")
                .exec();
        //获取到容器id，也就是cid
        return containerResponse.getId();
    }


    // todo 当启动redis容器，读取统计数据时会报错：failed: Connection timed out: connect。其他容器正常
    /**
     * 读取容器的统计数据
     *
     * @param cid
     */
    @Override
    public void readCtrStats(String cid, Long userId) {
        StatsCmd statsCmd = defaultClient.statsCmd(cid);
        ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

            private boolean stopStats = false;

            @Override
            public void onNext(Statistics statistics) {
                if (stopStats) return;

                //封装传输对象
                CtrStatsResponseMessage ctrStatsResponseMessage = new CtrStatsResponseMessage();
                ctrStatsResponseMessage.setCpuTotalUsage(NumberUtil.div((long) statistics.getCpuStats().getCpuUsage().getTotalUsage(), 1000 * 1000, 1))
                        .setPerCpuUsage(statistics.getCpuStats().getCpuUsage().getPercpuUsage())
                        .setOnlineCpus(statistics.getCpuStats().getOnlineCpus())
                        .setMemoryUsage(NumberUtil.div((long) statistics.getMemoryStats().getUsage(), 1024, 1))
                        .setMemoryMaxUsage(NumberUtil.div((long) statistics.getMemoryStats().getMaxUsage(), 1024, 1))
                        .setMemoryLimit(NumberUtil.div((long) statistics.getMemoryStats().getLimit(), 1024 * 1024, 1))
                        .setNumProcess(statistics.getNumProcs());
                long rxSum = Objects.requireNonNull(statistics.getNetworks()).values().stream().mapToLong(StatisticNetworksConfig::getRxBytes).sum();
                long txSum = Objects.requireNonNull(statistics.getNetworks()).values().stream().mapToLong(StatisticNetworksConfig::getTxBytes).sum();
                long ioSum = Objects.requireNonNull(statistics.getBlkioStats().getIoServiceBytesRecursive()).stream().mapToLong(BlkioStatEntry::getValue).sum();

                ctrStatsResponseMessage.setRxBytes(NumberUtil.div(rxSum, 1000, 1))
                        .setTxBytes(NumberUtil.div(txSum, 1000, 1))
                        .setIoValue(NumberUtil.div(ioSum, 1000, 1));


                // 使用websocket向前端传输
                try {
                    String mes = jacksonObjectMapper.writeValueAsString(ctrStatsResponseMessage);
                    WebSocketServer.sendInfo(mes, userId);
                } catch (JsonProcessingException e) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
                }

                log.info("websocket传输对象信息：{}", ctrStatsResponseMessage.toString());
            }

            @Override
            public void onError(Throwable throwable) {
                // todo 报错信息需要返回给用户
                log.error("读取容器统计数据出错：{}", throwable.getMessage());
            }

            @Override
            public void onStart(Closeable closeable) {
                log.info("开始读取容器统计数据");
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void close() throws IOException {
                stopStats = true;
            }
        });

        statsCmd.exec(statisticsResultCallback);
        STATS_CMD_MAP.put(userId, statisticsResultCallback);
    }


    /**
     * 关闭监控统计命令
     *
     * @param userId
     */
    @Override
    public void closeStatsCmd(Long userId) {
        ResultCallback<Statistics> resultCallback = STATS_CMD_MAP.get(userId);
        if (resultCallback != null) {
            try {
                resultCallback.close();
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.DOCKER_ERROR, e.getMessage());
            }
        }
        STATS_CMD_MAP.remove(userId);
    }

    /**
     * 获取容器占用的内存
     *
     * @param cid
     * @return
     */
    @Override
    public double getCtrMemory(String cid) {
        Statistics containerStats = defaultClient.statsCmd(cid).exec(new InvocationBuilder.AsyncResultCallback<>()).awaitResult();
        long memoryUsageInBytes = 0;
        if (containerStats != null && containerStats.getMemoryStats() != null) {
            memoryUsageInBytes = containerStats.getMemoryStats().getUsage();
        }
        // 将字节数转换为 MB
        double memory = NumberUtil.div(memoryUsageInBytes, 1024.0 * 1024.0, 1);  //小数点后一位
        return memory;
    }

    /**
     * 查看所有容器
     *
     * @return
     */
    @Override
    public List<Container> listContainer() {
        ListContainersCmd listContainersCmd = defaultClient.listContainersCmd();
        List<Container> containers = listContainersCmd.withShowAll(true).exec();
        return containers;
    }

    /**
     * nginx如果没有开服务，没有开守护进程，那么它执行完以后就自动结束，状态变为exited
     */

    /**
     * 启动容器
     *
     * @param cid
     * @return
     */
    @Override
    public boolean startCtr(String cid) {
        defaultClient.startContainerCmd(cid).exec();
        return true;
    }

    /**
     * 停止容器
     *
     * @param cid 容器ID，用于标识需要停止的容器
     * @return 总是返回true，表示停止操作已执行
     */
    @Override
    public boolean stopCtr(String cid) {
        log.info("停止容器：{}", cid);
        defaultClient.stopContainerCmd(cid).exec();
        return true;
    }

    /**
     * 查看日志
     * 这里是异步查看，因为日志可能非常多，不可能一直输出，一直卡在这里
     *
     * @param cid
     */
    @Override
    public byte[] logCtr(String cid) {

        String userDir = System.getProperty("user.dir");
        String globalLogPathName = userDir + File.separator + GLOBAL_LOG_DIR_NAME;

        //判断全局日志目录是否存在，不存在则创建
        if (!FileUtil.exist(globalLogPathName)) {
            FileUtil.mkdir(globalLogPathName);
        }

        String userLogPath = globalLogPathName + File.separator + GLOBAL_LOG_NAME;
        // 清空日志文件里的内容
        FileUtil.writeString("", userLogPath, StandardCharsets.UTF_8);

        LogContainerResultCallback callback = new LogContainerResultCallback() {
            @Override
            public void onNext(Frame item) {

                /*ByteArrayInputStream inputStream = IoUtil.toStream(item.getPayload());
                byte[] bytes = IoUtil.readBytes(inputStream);
                try {
                    IoUtil.write(response.getOutputStream(), true, bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }*/
                FileUtil.appendString(new String(item.getPayload()), userLogPath, StandardCharsets.UTF_8);
                super.onNext(item);
            }
        };

        log.info("查看容器日志：{}", cid);
        try {
            defaultClient.logContainerCmd(cid)
                    .withStdErr(true)
                    .withStdOut(true)
                    .exec(callback).awaitCompletion();

            File file = new File(userLogPath);
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            return buffer;

            /*// 清空response
            //response.reset();
            // 设置response的Header
            response.setCharacterEncoding("UTF-8");
            //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
            //attachment表示以附件方式下载 inline表示在线打开 "Content-Disposition: inline; filename=文件名.mp3"
            // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
            // 告知浏览器文件的大小
            response.addHeader("Content-Length", "" + file.length());
            // 允许跨域
            // response.addHeader("Access-Control-Allow-Origin", "*");
            //response.setContentType("application/octet-stream");
            response.setContentType("application/force-download");

            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            outputStream.write(buffer);
            outputStream.flush();*/
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DOCKER_ERROR, e.getMessage());
        }
    }

    /**
     * 删除容器
     *
     * @param cid
     * @return
     */
    @Override
    public boolean removeCtr(String cid) {
        defaultClient.removeContainerCmd(cid).withForce(true).exec();
        return true;
    }

    /**
     * 重启容器
     *
     * @param containerId
     * @return
     */
    @Override
    public boolean restartCtr(String containerId) {
        log.warn("重启容器：{}", containerId);
        defaultClient.restartContainerCmd(containerId).exec();
        return true;
    }
}
