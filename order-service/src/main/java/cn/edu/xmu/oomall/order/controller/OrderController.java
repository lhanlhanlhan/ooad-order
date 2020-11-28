package cn.edu.xmu.oomall.order.controller;

import cn.edu.xmu.oomall.order.annotations.LoginUser;
import cn.edu.xmu.oomall.order.aspects.Inspect;
import cn.edu.xmu.oomall.order.enums.OrderStatus;
import cn.edu.xmu.oomall.order.model.vo.OrderEditVo;
import cn.edu.xmu.oomall.order.model.vo.NewOrderVo;
import cn.edu.xmu.oomall.order.model.vo.OrderStatusVo;
import cn.edu.xmu.oomall.order.service.OrderService;
import cn.edu.xmu.oomall.order.utils.ResponseUtils;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单控制器类
 *
 * @author Han Li
 * Created at 2020/11/5 15:21
 * Modified by Han Li at 2020/11/25 14:58
 **/
@Api(value = "订单服务", tags = "order")
@RestController
@RequestMapping(value = "/order", produces = "application/json;charset=UTF-8")
public class OrderController {

    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    // 订单服务
    @Autowired
    private OrderService orderService;

    /**
     * o1: 获得订单的所有状态 [DONE]
     *
     * @author Han Li
     * Created at 25/11/2020 08:34
     * Created by Han Li at 25/11/2020 08:34
     * @return java.lang.Object
     */
    @ApiOperation(value = "获得订单的所有状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Inspect // 需要登入
    @GetMapping("orders/states")
    public Object getAllStatus() {
        if (logger.isDebugEnabled()) {
            logger.debug("get orders/states");
        }
        // 创造对应枚举数组
        List<OrderStatusVo> orderStatusVos = new ArrayList<>();
        for (OrderStatus os : OrderStatus.values()) {
            orderStatusVos.add(new OrderStatusVo((os)));
        }
        // 返回
        return ResponseUtils.ok(orderStatusVos);
    }


    /**
     * o2: 买家查询名下订单 (概要) [DONE]
     *
     * @author Han Li
     * Created at 25/11/2020 15:30
     * Created by Han Li at 25/11/2020 15:30
     * @param orderSn 订单号
     * @param state 订单状态
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param page 页数
     * @param pageSize 每页包括的记录数量
     * @param customerId 用户 ID
     * @return java.lang.Object
     */
    @ApiOperation(value = "买家查询名下订单 (概要)")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Inspect  // 需要登入
    @GetMapping("orders")
    public Object getAllOrders(@RequestParam(required = false) String orderSn,
                               @RequestParam(required = false) Byte state,
                               @RequestParam(required = false) String beginTime,
                               @RequestParam(required = false) String endTime,
                               @RequestParam(required = false) Integer page,
                               @RequestParam(required = false) Integer pageSize,
                               @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get states: orderSn=" + orderSn +
                    " state=" + state + " beginTime=" + beginTime +
                    " endTime=" + endTime + " page=" + page +
                    " pageSize=" + pageSize + " customerId=" + customerId);
        }
        // 获取数据并返回
        return ResponseUtils.make(orderService.getCustomerOrders(
                orderSn, state, beginTime, endTime, page, pageSize, customerId
        ));
    }


    /**
     * o3: 买家申请建立订单 (普通，团购，预售)
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:28
     * Modified at 2020/11/5 15:28
     */
    @ApiOperation(value = "买家申请建立订单 (普通，团购，预售)")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header")
    })
    @ApiResponses({
            @ApiResponse(code = 900, message = "商品库存不足"),
            @ApiResponse(code = 0, message = "成功"),
    })
    @PostMapping("orders")
    public Object createOrder(@RequestBody NewOrderVo orderInfo) {
        if (logger.isDebugEnabled()) {
            logger.debug("post orders; vo=" + orderInfo);
        }
        return null;
    }


    /**
     * o4: 买家查询订单完整信息 (普通，团购，预售) [DONE]
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:44
     * Modified at 2020/11/5 15:44
     */
    @ApiOperation(value = "买家查询订单完整信息 (普通，团购，预售)")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Inspect
    @GetMapping("orders/{id}")
    public Object getDetailedOrder(@PathVariable Long id,
                                   @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get orders/{id}; id=" + id);
        }
        // 调用服务层
        return ResponseUtils.make(orderService.getOrder(id, customerId));
    }


    /**
     * o5: 买家修改本人名下订单 [DONE]
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:44
     * Modified at 2020/11/5 15:44
     */
    @ApiOperation(value = "买家修改本人名下订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="body", required = true, dataType="Object", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @Inspect // 需要登入
    @PutMapping("orders/{id}")
    public Object modifyOrder(@PathVariable Long id,
                              @RequestBody OrderEditVo orderEditVo,
                              @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put orders/{id}; id=" + id + " vo=" + orderEditVo);
        }
        // 调用服务层
        return ResponseUtils.make(orderService.buyerModifyOrder(id, customerId, orderEditVo));
    }

    /**
     * o6: 买家取消 / 逻辑删除本人名下订单
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:50
     * Modified at 2020/11/5 15:50
     */
    @ApiOperation(value = "买家取消 / 逻辑删除本人名下订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 800, message = "订单状态禁止"),
            @ApiResponse(code = 0, message = "成功")
    })
    @Inspect
    @DeleteMapping("orders/{id}")
    public Object deleteOrCancelOrder(@PathVariable Long id,
                                      @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("delete orders/{id}; id=" + id);
        }
        // 调用服务层
        return ResponseUtils.make(orderService.buyerDelOrCancelOrder(id, customerId));
    }

    /**
     * o7: 买家确认收货
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:50
     * Modified at 2020/11/5 15:50
     */
    @ApiOperation(value = "买家取消 / 逻辑删除本人名下订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 800, message = "订单状态禁止"),
            @ApiResponse(code = 0, message = "成功")
    })
    @Inspect
    @PutMapping("orders/{id}/confirm")
    public Object confirmOrder(@PathVariable Long id,
                               @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put orders/{id}/confirm; id=" + id);
        }
        // 调用服务层
        return ResponseUtils.make(orderService.buyerConfirm(id, customerId));
    }

    /**
     * o8: 买家将团购订单改换为普通订单
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:50
     * Modified at 2020/11/5 15:50
     */
    @ApiOperation(value = "买家将团购订单改换为普通订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 800, message = "订单状态禁止"),
            @ApiResponse(code = 0, message = "成功")
    })
    @Inspect
    @PostMapping("orders/{id}/groupon-normal")
    public Object changeOrderTo(@PathVariable Long id,
                                @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put orders/{id}/confirm; id=" + id);
        }
        // 调用服务层
        return ResponseUtils.make(orderService.buyerChangeGroupon(id, customerId));
    }
}
