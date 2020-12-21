package cn.edu.xmu.ooad.order.freight;

import cn.edu.xmu.ooad.order.centre.utils.GlobalVariables;
import cn.edu.xmu.ooad.order.require.IOtherService;
import cn.edu.xmu.ooad.order.require.models.RegionInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

/**
 * Freight Service 应用程序入口
 *
 * @author Han Li
 * Created at 2020/12/15 14:48
 * Modified by Han Li at 2020/12/15 9:35
 **/
@SpringBootApplication
@MapperScan("cn.edu.xmu.ooad.order.freight.mapper")
@ComponentScan("cn.edu.xmu.ooad")  // 让 Spring 在这个包范围内找 Bean 进行初始化，否则引入 QM 的包将会不起作用
@EnableDubbo(scanBasePackages = "cn.edu.xmu.ooad.order.freight.service.impl")
public class FreightServiceApplication implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(FreightServiceApplication.class);

    @DubboReference
    private IOtherService iOtherService;

    public static void main(String[] args) {
        SpringApplication.run(FreightServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.debug("========= 正在从 其他模块 获取所有地区资讯 =========");
        List<RegionInfo> regionInfoList;
        while (true) {
            try {
                regionInfoList = iOtherService.getAllRegions();
                logger.debug("========= 其他模块 获取所有地区资讯 成功 =========");
                GlobalVariables.setRegionInfoList(regionInfoList);
                break;
            } catch (Exception e) {
                logger.error("*** 从 其他模块 获取所有地区资讯 失败 *** " + e.getMessage());

            }
        }
    }
}
