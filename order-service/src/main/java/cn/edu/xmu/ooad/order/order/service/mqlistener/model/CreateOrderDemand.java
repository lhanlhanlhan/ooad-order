package cn.edu.xmu.ooad.order.order.service.mqlistener.model;

import cn.edu.xmu.ooad.order.order.model.po.OrderItemPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.order.model.vo.OrderNewVo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建订单 的 Rocket MQ 请求
 *
 * @author Han Li
 * Created at 12/12/2020 4:43 下午
 * Modified by Han Li at 12/12/2020 4:43 下午
 */
@Data
public class CreateOrderDemand implements Serializable {
    OrderPo o;
    List<OrderItemPo> i;
    Long couponId;
    List<Long> writeBackQueue;
    Long cId; // Customer ID;
    Byte type;
}
