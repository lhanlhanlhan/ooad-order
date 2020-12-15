package cn.edu.xmu.ooad.order.order.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述登入用户的用户 id 注入位置
 *
 * @author Han Li
 * Created at 25/11/2020 10:09 上午
 * Modified by Han Li at 25/11/2020 10:09 上午
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
}
