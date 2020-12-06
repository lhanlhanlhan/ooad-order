package cn.edu.xmu.oomall.order.dao;

import cn.edu.xmu.oomall.order.controller.OrderController;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.mapper.OrderItemPoMapper;
import cn.edu.xmu.oomall.order.mapper.OrderMapper;
import cn.edu.xmu.oomall.order.mapper.OrderSimplePoMapper;
import cn.edu.xmu.oomall.order.model.bo.Order;
import cn.edu.xmu.oomall.order.model.po.*;
import cn.edu.xmu.oomall.order.model.vo.OrderEditVo;
import cn.edu.xmu.oomall.order.model.vo.OrderSimpleVo;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    // Simple Order Po 的 Mapper
    @Autowired
    private OrderSimplePoMapper orderSimplePoMapper;

    // Order 的 Mapper
    @Autowired
    private OrderMapper orderMapper;

    // OrderItem 的 Mapper
    @Autowired
    private OrderItemPoMapper orderItemPoMapper;

    // 邱明规定的 Date Formatter
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");


    /**
     * 工具函數：列舉滿足用戶 id、商店 id、訂單 id 的訂單數量 (可以用來鑑定權限)
     * @param orderId
     * @param customerId
     * @param shopId
     * @return -1：查詢失敗；>=0：對應數量
     */
    public long countOrders(Long orderId, Long customerId, Long shopId, boolean includeDeleted) {
        // 查詢數據庫
        long results;
        try {
            results = orderMapper.countOrder(orderId, customerId, shopId, includeDeleted);
        } catch (Exception e) {
            logger.error(e.getMessage());
            // count 失敗
            return -1;
        }
        return results;
    }

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
                                                                    Long customerId,
                                                                    Long shopId,
                                                                    boolean includeDeleted) {
        APIReturnObject<List<OrderSimplePo>> orderSimplePos = getSimpleOrders(orderSn, state, beginTime, endTime, customerId, shopId, includeDeleted);
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
     * @return 不分页的订单概要
     */
    public APIReturnObject<List<OrderSimplePo>> getSimpleOrders(String orderSn, Byte state,
                                                                String beginTime, String endTime,
                                                                Long customerId, Long shopId,
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
            try {
                LocalDate time = LocalDate.parse(beginTime, dateFormatter);
                criteria.andGmtCreateGreaterThanOrEqualTo(time.atStartOfDay());
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
                criteria.andGmtCreateLessThanOrEqualTo(time.plusDays(1).atStartOfDay());
            } catch (Exception e) {
                // 日期 parse 错误
                if (logger.isInfoEnabled()) {
                    logger.info(e.getMessage());
                }
                return new APIReturnObject<>(ResponseCode.RESOURCE_NOT_EXIST, "结束日期格式错误");
            }
        }
        if (shopId != null) {
            criteria.andShopIdEqualTo(shopId);
        }
        if (customerId != null) {
            criteria.andCustomerIdEqualTo(customerId);
        }
        if (!includeDeleted) {
            criteria.andBeDeletedIsNull();
        }
        // 执行查询
        List<OrderSimplePo> orderSimplePoList;
        try {
            orderSimplePoList = orderSimplePoMapper.selectByExample(example);
        } catch (Exception e) {
            // 数据库 错误
            logger.error(e.getMessage());
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
    public APIReturnObject<Order> getOrder(Long orderId, Long customerId, Long shopId, boolean includeDeleted) {
        // 调用 Mapper 查询经过拼接 Item 的 OrderPo
        OrderPo orderPo;
        try {
            orderPo = orderMapper.findOrderWithItem(orderId, customerId, shopId, includeDeleted);
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
        // 檢查一下，是否根本沒有 OrderItem (查不出 OrderItem 時由於 JOIN 的關係 List 中會有一個 NULL 對象)
        if (orderPo.getOrderItemList().get(0).getId() == null) {
            orderPo.setOrderItemList(null);
        }
        // 创建订单业务对象
        Order order = new Order(orderPo);

        // 把 orderPo 转换成 bo 对象，再转为 Po 对象
        return new APIReturnObject<>(order);
    }


    /**
     * 获取订单信息 (不含 Item、Customer、Shop)
     * @param orderId 订单 id
     * @param customerId 客户 id
     * @return 订单
     */
    public APIReturnObject<Order> getSimpleOrder(Long orderId, Long customerId, Long shopId, boolean includeDeleted) {
        // 调用 Mapper 查询 OrderPo
        OrderPo orderPo;
        try {
            orderPo = orderMapper.findOrder(orderId, customerId, shopId, includeDeleted);
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
            return new APIReturnObject<>(ResponseCode.RESOURCE_NOT_EXIST, "订单不存在 / 已删除 / 無檢視權限");
        }
        // 创建订单业务对象
        Order order = new Order(orderPo);

        // 把 orderPo 转换成 bo 对象，再转为 Po 对象
        return new APIReturnObject<>(order);
    }

    /**
     * 无条件修改订单信息
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
            return new APIReturnObject<>(ResponseCode.RESOURCE_NOT_EXIST);
        }
        return new APIReturnObject<>();
    }

    /**
     * 无条件修改 OrderItem 的订单 Id
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
}
