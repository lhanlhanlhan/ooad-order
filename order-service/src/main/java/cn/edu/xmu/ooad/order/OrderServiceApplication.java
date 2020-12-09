package cn.edu.xmu.ooad.order;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
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
@EnableDubbo
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
