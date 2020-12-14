package cn.edu.xmu.ooad.order.service.impl;

import cn.edu.xmu.ooad.other.required.IOtherOrderService;
import cn.edu.xmu.ooad.other.required.model.OtherOrderItemComplex;
import cn.edu.xmu.ooad.other.required.model.OtherOrderItemSimple;
import cn.edu.xmu.ooad.other.required.model.OtherOrderSimple;

public class IOtherOrderService17 implements IOtherOrderService {

    /**根据orderItemId获取orderItem相关信息，若资源不存在返回空
     * @author chenqw
     * @param orderItemId
     * @return OrderItemSimple or null
     */
    @Override
    public OtherOrderItemSimple getOrderItemByOrderItemId(Long orderItemId) {
        return null;
    }

    /**根据orderItemId获取orderItem相关信息和order信息，若资源不存在返回空
     *
     * @param orderItemId
     * @return
     */
    @Override
    public OtherOrderItemComplex getOrderItemComplexByOrderItemId(Long orderItemId) {
        return null;
    }

    /**根据orderItemId获取全部orderItem信息和order信息，若资源不存在返回空
     * @author chenqw
     * @param orderItemId
     * @return
     */
    @Override
    public OtherOrderSimple getOrderByOrderItemId(Long orderItemId) {
        return null;
    }
}
