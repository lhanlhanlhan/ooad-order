package cn.edu.xmu.ooad.order.order.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 售后订单视图对象
 *
 * @author Han Li
 * Created at 29/11/2020 1:25 下午
 * Modified by Han Li at 29/11/2020 1:25 下午
 */
@Data
public class AfterSaleOrderVo {

    private List<Map<String, Object>> orderItems;
    private String consignee;
    private Long region_id; // 不知道为啥，但是 API 就是如此写的
    private Long customerId; // API 没有，但是要防患于未然
    private String address;
    private String mobile;
    private String message;

}
