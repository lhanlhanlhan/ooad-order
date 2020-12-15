package cn.edu.xmu.ooad.order.order.model.vo;

import cn.edu.xmu.ooad.order.order.enums.OrderChildStatus;
import lombok.Data;

/**
 * 订单状态 Vo (后端返回)
 *
 * @author Han Li
 * Created at 25/11/2020 8:12 上午
 * Modified by Han Li at 25/11/2020 8:12 上午
 */
@Data
public class OrderStatusVo {
    private int code;
    private String name;

    public OrderStatusVo(OrderChildStatus os) {
        this.code = os.getCode();
        this.name = os.getName();
    }
}
