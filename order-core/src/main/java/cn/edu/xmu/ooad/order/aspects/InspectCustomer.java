package cn.edu.xmu.ooad.order.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 「审查」的切点 - 具有此注解的 Controller 必须登入才能使用
 *
 * @author Han Li
 * Created at 25/11/2020 8:58 上午
 * Modified by Han Li at 25/11/2020 8:58 上午
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InspectCustomer {
}