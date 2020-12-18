package cn.edu.xmu.ooad.order.order.dao;

import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.order.mapper.OrderFullPoMapper;
import cn.edu.xmu.ooad.order.order.mapper.OrderItemPoMapper;
import cn.edu.xmu.ooad.order.order.mapper.OrderMapper;
import cn.edu.xmu.ooad.order.order.mapper.OrderSimplePoMapper;
import cn.edu.xmu.ooad.order.order.model.bo.order.Order;
import cn.edu.xmu.ooad.order.order.model.bo.order.OrderFactory;
import cn.edu.xmu.ooad.order.order.model.bo.order.OrderItem;
import cn.edu.xmu.ooad.order.order.model.po.*;
import cn.edu.xmu.ooad.util.ResponseCode;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单 Dao
 *
 * @author Han Li
 * Created at 25/11/2020 4:41 下午
 * Modified by Han Li at 25/11/2020 4:41 下午
 */
@Repository
public class OrderDao {

    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);
    // 邱明规定的 Date Formatter
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");
    // Simple Order Po 的 Mapper
    @Autowired
    private OrderSimplePoMapper orderSimplePoMapper;
    // Order 的 Mapper
    @Autowired
    private OrderFullPoMapper orderFullPoMapper;
    // Order 的 Mapper (手写)
    @Autowired
    private OrderMapper orderMapper;
    // OrderItem 的 Mapper
    @Autowired
    private OrderItemPoMapper orderItemPoMapper;

    /**
     * 获取分页的订单概要列表
     *
     * @param orderSn    订单号
     * @param state      订单状态
     * @param beginTime  开始时间 yyyy-M-d
     * @param endTime    结束时间 yyyy-M-d
     * @param customerId 用户号
     * @return 分页的订单概要
     */
    public PageInfo<OrderSimplePo> getSimpleOrders(String orderSn, Byte state,
                                                                    LocalDateTime beginTime, LocalDateTime endTime,
                                                                    Long customerId,
                                                                    Long shopId,
                                                                    boolean includeDeleted) {
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
            criteria.andGmtCreateGreaterThanOrEqualTo(beginTime);
        }
        if (endTime != null) {
            criteria.andGmtCreateGreaterThanOrEqualTo(endTime);
        }
        if (shopId != null) {
            criteria.andShopIdEqualTo(shopId);
        }
        if (customerId != null) {
            criteria.andCustomerIdEqualTo(customerId);
        }
        if (!includeDeleted) { // 要求不被删除的才被返回
            criteria.andBeDeletedEqualTo((byte) 0);
        }
        // 执行查询
        List<OrderSimplePo> orderSimplePoList;
        try {
            orderSimplePoList = orderSimplePoMapper.selectByExample(example);
        } catch (Exception e) {
            // 数据库 错误
            logger.error(e.getMessage());
            return null;
        }
        // 装入 PageInfo 后返回
        return new PageInfo<>(orderSimplePoList);
    }


    /**
     * 获取订单完整信息
     *
     * @return 订单
     */
    public Order getOrder(Long orderId, boolean includeDeleted) {
        // 调用 Mapper 查询经过拼接 Item 的 OrderPo
        OrderPo orderPo;
        try {
            orderPo = orderMapper.findOrder(orderId, includeDeleted);
        } catch (Exception e) {
            // 严重数据库错误
            logger.error(e.getMessage());
            return null;
        }

        // 不获取已被逻辑删除及根本不存在的订单
        if (orderPo == null || (!includeDeleted && orderPo.getBeDeleted() != null && orderPo.getBeDeleted() == 1)) {
            return null;
        }

        // 查询 order item
        OrderItemPoExample example = new OrderItemPoExample();
        OrderItemPoExample.Criteria criteria = example.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        List<OrderItemPo> orderItemPos;
        try {
            orderItemPos = orderItemPoMapper.selectByExample(example);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }

        // 转为业务对象
        List<OrderItem> orderItems = orderItemPos
                .stream()
                .map(OrderItem::new)
                .collect(Collectors.toList());

        // 创建订单业务对象
        Order order;
        try {
            order = OrderFactory.makeOrder(orderPo);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        order.setOrderItemList(orderItems);
        return order;
    }


    /**
     * 获取 (概要) 订单信息 (不含 Item、Customer、Shop，无条件获得)
     *
     * @param orderId    订单 id
     * @return 订单
     */
    public Order getSimpleOrder(Long orderId, boolean includeDeleted) {
        // 调用 Mapper 查询 OrderPo
        OrderPo orderPo;
        try {
            orderPo = orderMapper.findOrder(orderId, includeDeleted);
        } catch (Exception e) {
            // 严重数据库错误
            logger.error("数据库错误：" + e.getMessage());
            return null;
        }

        // 不获取已被逻辑删除及根本不存在的订单
        if (!includeDeleted) {
            if (orderPo == null || (orderPo.getBeDeleted() != null) && (orderPo.getBeDeleted() == 1)) {
                return null;
            }
        }

        // 创建订单业务对象
        Order order;
        try {
            order = OrderFactory.makeOrder(orderPo);
        } catch (Exception e) {
            logger.error("订单格式错误：orderId=" + orderId);
            return null;
        }

        // 把 orderPo 转换成 bo 对象，再转为 Po 对象
        return order;
    }


    /**
     * 无条件修改订单信息
     *
     * @return 返回
     */
    public APIReturnObject<?> modifyOrder(OrderEditPo po) {
        // 尝试修改
        int affected = 0;
        try {
            affected = orderMapper.updateOrder(po);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        // 检查修改结果
        if (affected <= 0) {
            logger.error("为啥没插进去？modifyOrder po=" + po);
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        return new APIReturnObject<>();
    }

    /**
     * 无条件修改 OrderItem 的订单 Id
     *
     * @return 返回：0 成功；1 失败
     */
    public int modifyOrderItemOrderId(Long itemId, Long newOrderId) {
        OrderItemPo po = new OrderItemPo();
        po.setId(itemId);
        po.setOrderId(newOrderId);
        po.setGmtModified(LocalDateTime.now());
        // 尝试修改
        int affected = 0;
        try {
            affected = orderItemPoMapper.updateByPrimaryKeySelective(po);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 1;
        }
        // 检查修改结果
        if (affected <= 0) {
            return 1;
        }
        return 0;
    }

    /**
     * 插入订单项目
     *
     * @param po 订单项目 Po
     */
    public int addOrderItem(OrderItemPo po) {
        return orderItemPoMapper.insert(po);
    }

    /**
     * 插入订单
     *
     * @param po 订单项目 Po
     */
    public int addOrder(OrderPo po) {
        return orderMapper.addOrder(po);
    }


    /**
     * 获取 OrderItem
     *
     * @param orderItemId
     * @return
     */
    public OrderItem getOrderItem(Long orderItemId) {
        OrderItemPo po = orderItemPoMapper.selectByPrimaryKey(orderItemId);
        if (po == null) {
            return null;
        }
        return new OrderItem(po);
    }

    /**
     * 获取 OrderItem Po
     *
     * @param orderItemId
     * @return
     */
    public OrderItemPo getOrderItemPo(Long orderItemId) {
        return orderItemPoMapper.selectByPrimaryKey(orderItemId);
    }
}
