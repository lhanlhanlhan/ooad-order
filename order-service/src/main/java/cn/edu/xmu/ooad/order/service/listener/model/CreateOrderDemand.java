package cn.edu.xmu.ooad.order.service.listener.model;

import cn.edu.xmu.ooad.order.model.vo.OrderNewVo;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建订单 的 Rocket MQ 请求
 *
 * @author Han Li
 * Created at 12/12/2020 4:43 下午
 * Modified by Han Li at 12/12/2020 4:43 下午
 */
@Data
public class CreateOrderDemand implements Serializable {
    private Long customerId;
    private OrderNewVo orderNewVo;
    private String sn;
    private Byte type; // 种类：0 普通 1 预售 2 团购
}
