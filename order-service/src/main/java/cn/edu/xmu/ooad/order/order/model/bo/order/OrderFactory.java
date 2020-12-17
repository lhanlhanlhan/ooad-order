package cn.edu.xmu.ooad.order.order.model.bo.order;

import cn.edu.xmu.ooad.order.order.exceptions.NoSuchOrderTypeException;
import cn.edu.xmu.ooad.order.order.model.bo.order.impl.GrouponOrder;
import cn.edu.xmu.ooad.order.order.model.bo.order.impl.NormalOrder;
import cn.edu.xmu.ooad.order.order.model.bo.order.impl.PreSaleOrder;
import cn.edu.xmu.ooad.order.order.model.po.OrderPo;

public class OrderFactory {

    public static Order makeOrder(OrderPo orderPo) throws NoSuchOrderTypeException {
        if (orderPo.getOrderType() == null) {
            throw new NoSuchOrderTypeException();
        }
        switch (orderPo.getOrderType()) {
            case 0: // 普通订单
                return new NormalOrder(orderPo);
            case 1: // 团购
                return new GrouponOrder(orderPo);
            case 2: // 预售
                return new PreSaleOrder(orderPo);
            default:
                throw new NoSuchOrderTypeException();
        }
    }

}
