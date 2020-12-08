package cn.edu.xmu.ooad.order.client;

/**
 * 订单模块 - 订单部分内部调用接口
 *
 * @author Han Li
 * Created at 6/12/2020 8:13 下午
 * Modified by Han Li at 6/12/2020 8:13 下午
 */
public interface IOrderService {

    /**
     * 判断客户名下是否有某个订单
     * @param orderId 订单号
     */
    boolean isOrderExist(Long orderId, Long customerId);
}
