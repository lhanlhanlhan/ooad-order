package cn.edu.xmu.oomall.order.connector.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 优惠/优惠券服务 (功能由【商品模块】或【全局模块】提供)
 *
 * @author Han Li
 * Created at 1/12/2020 11:46 上午
 * Modified by Han Li at 1/12/2020 11:46 上午
 */
@Service
public class CouponService {


    /**
     * TODO - 计算订单优惠
     *
     * @param orderItems 订单项目
     * @return 0：计算完成，正常计算
     */
    public int computeDiscount(List<Map<String, Long>> orderItems) {
        for (Map<String, Long> orderItem : orderItems) {
            orderItem.put("discount", 1L);
        }
        return 0;
    }
}
