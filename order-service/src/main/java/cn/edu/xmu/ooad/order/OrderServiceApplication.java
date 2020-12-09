package cn.edu.xmu.ooad.order;

import cn.edu.xmu.ooad.util.JwtHelper;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Order Service 应用程序入口
 *
 * @author Han Li
 * Created at 2020/11/5 14:48
 * Modified by Han Li at 2020/11/24 9:35
 **/
@SpringBootApplication
@MapperScan("cn.edu.xmu.ooad.order.mapper")
@ComponentScan("cn.edu.xmu.ooad")  // 让 Spring 在这个包范围内找 Bean 进行初始化，否则引入 QM 的包将会不起作用
@EnableDubbo(scanBasePackages = "cn.edu.xmu.ooad.order.service.impl")
public class OrderServiceApplication implements ApplicationRunner {

    @Value("${orders.print-token}")
    private Boolean printToken;

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (printToken) {
            JwtHelper jwtHelper = new JwtHelper();
            // 十年后过期的超级管理员
            String tokenAdmin = jwtHelper.createToken(1L, 0L, 315360000);
            // 90 秒后过期的超级管理员
            String shortTokenAdmin = jwtHelper.createToken(1L, 0L, 90);
            // 十年后过期的 1 号店铺管理员
            String tokenShopAdmin = jwtHelper.createToken(2L, 1L, 315360000);
            // 90 秒后过期的 1 号店铺管理员
            String shortTokenShopAdmin = jwtHelper.createToken(2L, 1L, 90);
            // 十年后过期的 1 号客户
            String tokenCustomer = jwtHelper.createToken(1L, -2L, 315360000);
            // 90 秒后过期的 1 号客户
            String shortTokenCustomer = jwtHelper.createToken(1L, -2L, 90);
            // 打印这些 token
            logger.debug("Token of super admin (expired after 10 years): \n" + tokenAdmin);
            logger.debug("Token of super admin (expired after 90 secs): \n" + shortTokenAdmin);
            logger.debug("Token of shop admin (expired after 10 years): \n" + tokenShopAdmin);
            logger.debug("Token of shop admin (expired after 90 secs): \n" + shortTokenShopAdmin);
            logger.debug("Token of customer (expired after 10 years): \n" + tokenCustomer);
            logger.debug("Token of customer (expired after 90 secs): \n" + shortTokenCustomer);
        }
    }
}
