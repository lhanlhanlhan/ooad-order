package cn.edu.xmu.ooad.order.mapper;

import cn.edu.xmu.ooad.order.model.po.OrderEditPo;
import cn.edu.xmu.ooad.order.model.po.OrderPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 完整订单的 Mapper
 *
 * @author Han Li
 * Created at 26/11/2020 2:58 下午
 * Modified by Han Li at 26/11/2020 2:58 下午
 */
@Mapper
public interface OrderMapper {

    OrderPo findOrder(Long id, Long customerId, Long shopId, boolean includeDeleted);

    OrderPo findOrderWithItem(Long id, Long customerId, Long shopId, boolean includeDeleted);

    int updateOrder(OrderEditPo editPo);

    int addOrder(OrderPo orderPo);

    long countOrder(Long id, Long customerId, Long shopId, boolean includeDeleted);
}
