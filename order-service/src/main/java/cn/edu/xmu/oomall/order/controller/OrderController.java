package cn.edu.xmu.oomall.order.controller;

import cn.edu.xmu.oomall.order.annotations.LoginUser;
import cn.edu.xmu.oomall.order.aspects.Inspect;
import cn.edu.xmu.oomall.order.enums.OrderStatus;
import cn.edu.xmu.oomall.order.model.vo.EditOrderVo;
import cn.edu.xmu.oomall.order.model.vo.NewOrderVo;
import cn.edu.xmu.oomall.order.model.vo.OrderStatusVo;
import cn.edu.xmu.oomall.order.utils.ResponseUtils;
import io.swagger.annotations.*;
import org.apache.ibatis.annotations.Delete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 订单控制器类
 *
 * @author Han Li
 * Created at 2020/11/5 15:21
 * Modified by Han Li at 2020/11/25 00:09
 **/
@Api(value = "订单服务", tags = "order")
@RestController
@RequestMapping(value = "/order", produces = "application/json;charset=UTF-8")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

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

//    @Inspect  // 需要登入
    @GetMapping("orders")
    public Object getAllOrders(@RequestParam(required = false) String orderSn,
                               @RequestParam(required = false) Integer state,
                               @RequestParam(required = false) String beginTime,
                               @RequestParam(required = false) String endTime,
                               @RequestParam(required = false) Integer page,
                               @RequestParam(required = false) Integer pageSize,
                               @LoginUser Integer customerId) {
        return null;
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
     * o4: 买家查询订单完整信息 (普通，团购，预售)
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
    @GetMapping("orders/{id}")
    public Object getDetailedOrder(@PathVariable Integer id) {
        if (logger.isDebugEnabled()) {
            logger.debug("get orders/{id}; id=" + id);
        }
        return null;
    }


    /**
     * o5: 买家修改本人名下订单
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
    @PutMapping("orders/{id}")
    public Object modifyOrder(@PathVariable Integer id, @RequestBody EditOrderVo editOrderVo) {
        if (logger.isDebugEnabled()) {
            logger.debug("put orders/{id}; id=" + id + " vo=" + editOrderVo);
        }
        return null;
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
    @DeleteMapping("orders/{id}")
    public Object deleteOrCancelOrder(@PathVariable Integer id) {
        if (logger.isDebugEnabled()) {
            logger.debug("delete orders/{id}; id=" + id);
        }
        return null;
    }
}
