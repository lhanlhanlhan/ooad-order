package cn.edu.xmu.oomall.order.model.vo;

import cn.edu.xmu.oomall.order.enums.OrderStatus;
import lombok.Data;

/**
 * 订单状态 Vo
 * @author Han Li
 * Created at 25/11/2020 8:12 上午
 * Modified by Han Li at 25/11/2020 8:12 上午
 */
@Data
public class OrderStatusVo {
    private int code;
    private String name;

    public OrderStatusVo(OrderStatus os) {
        this.code = os.getCode();
        this.name = os.getName();
    }
}
