package cn.edu.xmu.ooad.order.centre.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 框架相关工具
 *
 * @author Han Li
 * Created at 5/12/2020 10:26 上午
 * Modified by Han Li at 5/12/2020 10:26 上午
 */
@Component
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext context = null;

    // 获取 ApplicationContext
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 程序运行起来后，把 Context 保存下来
        if (context == null) {
            context = applicationContext;
        }
    }

    // 根据 Class 获取种类唯一的 Bean
    public static <T> T getBean(Class<T> c) {
        return context.getBean(c);
    }

    // 根据名字获取 Bean
    public static <T> T getBean(String name, Class<T> c) {
        return context.getBean(name, c);
    }
}
