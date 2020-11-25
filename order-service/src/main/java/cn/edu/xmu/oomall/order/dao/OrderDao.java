package cn.edu.xmu.oomall.order.dao;

import cn.edu.xmu.oomall.order.mapper.OrderSimplePoMapper;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePo;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePoExample;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单 Dao
 *
 * @author Han Li
 * Created at 25/11/2020 4:41 下午
 * Modified by Han Li at 25/11/2020 4:41 下午
 */
@Repository
public class OrderDao {

    @Autowired
    private OrderSimplePoMapper orderSimplePoMapper;


    public PageInfo<OrderSimplePo> getSimpleOrders(String orderSn, Byte state,
                                                   String beginTime, String endTime,
                                                   int page, int pageSize,
                                                   Long customerId) {
        List<OrderSimplePo> orderSimplePos = getSimpleOrders(orderSn, state, beginTime, endTime, customerId);
        // 装入 PageInfo 后返回
        return new PageInfo<>(orderSimplePos);
    }

    public List<OrderSimplePo> getSimpleOrders(String orderSn, Byte state,
                                               String beginTime, String endTime,
                                               Long customerId) {
        // 创建 PoExample 对象，以实现多参数查询
        OrderSimplePoExample example = new OrderSimplePoExample();
        // 将查询字段放入 Example 对象的 查询规则 (Criteria) 里面去
        OrderSimplePoExample.Criteria criteria = example.createCriteria();
        if (orderSn != null) {
            criteria.andOrderSnEqualTo(orderSn);
        }
        if (state != null) {
            criteria.andStateEqualTo(state);
        }
        if (beginTime != null) {
            criteria.andGmtCreatedGreaterThanOrEqualTo(LocalDateTime.parse(beginTime));
        }
        if (endTime != null) {
            criteria.andGmtCreatedLessThanOrEqualTo(LocalDateTime.parse(endTime));
        }
        // 将用户 id 放到查询规则里面去
        criteria.andCustomerIdEqualTo(customerId);
        // 执行查询
        return orderSimplePoMapper.selectByExample(example);
    }
}
