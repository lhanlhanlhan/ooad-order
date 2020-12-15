package cn.edu.xmu.ooad.order.centre.utils;

import java.util.Random;
import java.util.UUID;

/**
 * 工具
 *
 * @author Han Li
 * Created at 2/12/2020 5:24 下午
 * Modified by Han Li at 2/12/2020 5:24 下午
 */
public class Accessories {

    /**
     * UUID 算法生成 sn
     */
    public static String genSerialNumber() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 增加 20% 以内的随机时间
     * 如果 timeout <0 则会返回 60s+ 随机时间
     *
     * @param timeout 时间
     * @return 增加后的随机时间
     */
    public static long addRandomTime(long timeout) {
        if (timeout <= 0) {
            timeout = 60;
        }
        // 增加随机数
        timeout += (long) new Random().nextDouble() * (timeout / 5 - 1);
        return timeout;
    }
}
