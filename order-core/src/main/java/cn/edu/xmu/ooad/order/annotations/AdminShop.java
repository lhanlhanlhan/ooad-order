package cn.edu.xmu.ooad.order.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述登入管理员的 shopId 注入位置
 *
 * @author Han Li
 * Created at 29/11/2020 12:04 下午
 * Modified by Han Li at 29/11/2020 12:04 下午
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminShop {
}
