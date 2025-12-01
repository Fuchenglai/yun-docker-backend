package com.lfc.yundocker.job.cycle;


import com.lfc.yundocker.service.impl.YunContainerServiceImpl;
import com.lfc.yundocker.service.impl.YunImageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 定时释放镜像，容器等资源
 *
 * @author laifucheng
 */

@Slf4j
@Component
public class ReleaseRes {

    @Resource
    private YunContainerServiceImpl yunContainerService;

    @Resource
    private YunImageServiceImpl yunImageService;

    /**
     * 每天中午1点删除容器
     */
    @Scheduled(cron = "0 0 13 * * ?")
    public void releaseCtr() {
        yunContainerService.removeAllCtr();
    }

    /**
     * 每天晚上11点30分执行，删除容器和镜像
     */
    @Scheduled(cron = "0 30 23 * * ?")
    public void releaseRes() {
        yunContainerService.removeAllCtr();

        yunImageService.removeAllPrivateImages();
    }

}
