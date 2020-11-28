package cn.edu.xmu.oomall.order.dao;

import cn.edu.xmu.oomall.order.controller.OrderController;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.mapper.OrderMapper;
import cn.edu.xmu.oomall.order.mapper.OrderSimplePoMapper;
import cn.edu.xmu.oomall.order.model.bo.Order;
import cn.edu.xmu.oomall.order.model.bo.OrderItem;
import cn.edu.xmu.oomall.order.model.po.OrderPo;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePo;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePoExample;
import cn.edu.xmu.oomall.order.model.vo.OrderVo;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    // Simple Order Po 的 Mapper
    @Autowired
    private OrderSimplePoMapper orderSimplePoMapper;

    // Order 的 Mapper
    @Autowired
    private OrderMapper orderMapper;

    // 邱明规定的 Date Formatter
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");


    /**
     * 获取分页的订单概要列表
     *
     * @param orderSn 订单号
     * @param state 订单状态
     * @param beginTime 开始时间 yyyy-M-d
     * @param endTime 结束时间 yyyy-M-d
     * @param page 第几页
     * @param pageSize 每页记录数
     * @param customerId 用户号
     * @return 分页的订单概要
     */
    public APIReturnObject<PageInfo<OrderSimplePo>> getSimpleOrders(String orderSn, Byte state,
                                                                    String beginTime, String endTime,
                                                                    int page, int pageSize,
                                                                    Long customerId) {
        APIReturnObject<List<OrderSimplePo>> orderSimplePos = getSimpleOrders(orderSn, state, beginTime, endTime, customerId);
        if (orderSimplePos.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(orderSimplePos.getCode(), orderSimplePos.getErrMsg());
        }
        // 装入 PageInfo 后返回
        return new APIReturnObject<>(new PageInfo<>(orderSimplePos.getData()));
    }

    /**
     * 获取不分页的订单概要列表
     *
     * @param orderSn 订单号
     * @param state 订单状态
     * @param beginTime 开始时间 yyyy-M-d
     * @param endTime 结束时间 yyyy-M-d
     * @param customerId 用户号
     * @return 分页的订单概要
     */
    public APIReturnObject<List<OrderSimplePo>> getSimpleOrders(String orderSn, Byte state,
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
            try {
                LocalDate time = LocalDate.parse(beginTime, dateFormatter);
                criteria.andGmtCreatedGreaterThanOrEqualTo(time.atStartOfDay());
            } catch (Exception e) {
                // 日期 parse 错误
                if (logger.isInfoEnabled()) {
                    logger.info(e.getMessage());
                }
                return new APIReturnObject<>(ResponseCode.RESOURCE_NOT_EXIST, "起始日期格式错误");
            }
        }
        if (endTime != null) {
            try {
                LocalDate time = LocalDate.parse(endTime, dateFormatter);
                criteria.andGmtCreatedLessThanOrEqualTo(time.plusDays(1).atStartOfDay());
            } catch (Exception e) {
                // 日期 parse 错误
                if (logger.isInfoEnabled()) {
                    logger.info(e.getMessage());
                }
                return new APIReturnObject<>(ResponseCode.RESOURCE_NOT_EXIST, "结束日期格式错误");
            }
        }
        // 将用户 id 放到查询规则里面去
        criteria.andCustomerIdEqualTo(customerId);
        // 执行查询
        List<OrderSimplePo> orderSimplePoList;
        try {
            orderSimplePoList = orderSimplePoMapper.selectByExample(example);
        } catch (Exception e) {
            // 数据库 错误
            logger.info(e.getMessage());
            return new APIReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>(orderSimplePoList);
    }

    /**
     * 获取订单完整信息
     * @param orderId 订单 id
     * @param customerId 客户 id
     * @return 订单
     */
    public APIReturnObject<Order> getOrder(Long orderId, Long customerId) {
        // 调用 Mapper 查询经过拼接 Item 的 OrderPo
        OrderPo orderPo;
        try {
            orderPo = orderMapper.findOrderWithItem(orderId, customerId);
        } catch (Exception e) {
            // 严重数据库错误
            logger.error(e.getMessage());
            return new APIReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, "数据库错误");
        }
        // 不获取已被逻辑删除及根本不存在的订单
        if (orderPo == null || (orderPo.getBeDeleted() != null)) {
            if (logger.isInfoEnabled()) {
                logger.info("订单不存在或已被删除或不属于该用户：id = " + orderId);
            }
            return new APIReturnObject<>(ResponseCode.RESOURCE_NOT_EXIST, "订单不存在 / 已删除 / 不属于用户");
        }
        // 订单被篡改过所以是不合法的
        Order order = new Order(orderPo);
        if (!order.isAuthentic()) {
            if (logger.isInfoEnabled()) {
                logger.info("订单被篡改了：id = " + orderId);
            }
            return new APIReturnObject<>(ResponseCode.ORDER_DISTORTED);
        }

        // 把 orderPo 转换成 bo 对象，再转为 Po 对象
        return new APIReturnObject<>(order);
    }
}
