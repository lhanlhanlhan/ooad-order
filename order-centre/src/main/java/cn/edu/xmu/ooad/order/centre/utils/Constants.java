package cn.edu.xmu.ooad.order.centre.utils;

import java.time.format.DateTimeFormatter;

/**
 * 常量定义
 *
 * @author Han Li
 * Created at 25/11/2020 9:15 上午
 * Modified by Han Li at 25/11/2020 9:15 上午
 */
public class Constants {
    public static final String LOGIN_TOKEN_KEY = "authorization";
    public static final String outDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String inDateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter outDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
}
