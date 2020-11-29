package cn.edu.xmu.oomall.order.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 「审查管理员」的切点 - 具有此注解的 Controller 必须管理员登入才能使用
 *
 * @author Han Li
 * Created at 29/11/2020 11:29 上午
 * Modified by Han Li at 29/11/2020 11:29 上午
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InspectAdmin {
}
