package cn.edu.xmu.ooad.order.utils;

import java.util.UUID;

/**
 * 工具
 *
 * @author Han Li
 * Created at 2/12/2020 5:24 下午
 * Modified by Han Li at 2/12/2020 5:24 下午
 */
public class Accessories {

    public static String genSerialNumber() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
