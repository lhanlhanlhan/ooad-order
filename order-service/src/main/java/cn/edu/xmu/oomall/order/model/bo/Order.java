package cn.edu.xmu.oomall.order.model.bo;

import cn.edu.xmu.oomall.order.annotations.AuthenticObject;
import cn.edu.xmu.oomall.order.annotations.SimpleVoCreatable;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePo;
import cn.edu.xmu.oomall.order.model.vo.OrderSimpleVo;

/**
 * 订单
 *
 * @author Han Li
 * Created at 25/11/2020 4:44 下午
 * Modified by Han Li at 25/11/2020 4:44 下午
 */
public class Order implements SimpleVoCreatable, AuthenticObject {

    /**
     * 创建概要业务对象
     *
     * @param orderSimplePo SimplePo 对象
     */
    public Order(OrderSimplePo orderSimplePo) {

    }

    /**
     * 创建概要 Vo 对象
     *
     * @return 概要 Vo 对象
     */
    @Override
    public OrderSimpleVo createSimpleVo() {
        return null;
    }

    /**
     * 判断该对象是否被篡改
     *
     * @return 是否被篡改，若被篡改，返回 false
     */
    @Override
    public boolean isAuthentic() {
        return true;
    }
}
