package cn.edu.xmu.ooad.order.controller;

import cn.edu.xmu.ooad.order.annotations.AdminShop;
import cn.edu.xmu.ooad.order.annotations.LoginUser;
import cn.edu.xmu.ooad.order.aspects.InspectAdmin;
import cn.edu.xmu.ooad.order.aspects.InspectCustomer;
import cn.edu.xmu.ooad.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.enums.OrderType;
import cn.edu.xmu.ooad.order.model.vo.AfterSaleOrderVo;
import cn.edu.xmu.ooad.order.model.vo.OrderEditVo;
import cn.edu.xmu.ooad.order.model.vo.OrderNewVo;
import cn.edu.xmu.ooad.order.model.vo.OrderStatusVo;
import cn.edu.xmu.ooad.order.service.OrderService;
import cn.edu.xmu.ooad.order.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.utils.Accessories;
import cn.edu.xmu.ooad.order.utils.ResponseCode;
import cn.edu.xmu.ooad.order.utils.ResponseUtils;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * @return java.lang.Object
     * @author Han Li
     * Created at 25/11/2020 08:34
     * Created by Han Li at 25/11/2020 08:34
     */
    @ApiOperation(value = "获得订单的所有状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @InspectAdmin // 需要登入
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
     * @param orderSn    订单号
     * @param state      订单状态
     * @param beginTime  开始时间
     * @param endTime    结束时间
     * @param page       页数
     * @param pageSize   每页包括的记录数量
     * @param customerId 用户 ID
     * @return java.lang.Object
     * @author Han Li
     * Created at 25/11/2020 15:30
     * Created by Han Li at 25/11/2020 15:30
     */
    @ApiOperation(value = "买家查询名下订单 (概要)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @InspectCustomer  // 需要登入
    @GetMapping("orders")
    public Object getAllOrders(@RequestParam(required = false) String orderSn,
                               @RequestParam(required = false) Byte state,
                               @RequestParam(required = false) Byte type, // API 没有但以备不时之需
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
     *
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:28
     * Modified at 2020/11/5 15:28
     */
    @ApiOperation(value = "买家申请建立订单 (普通，团购，预售)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 900, message = "商品库存不足"),
            @ApiResponse(code = 0, message = "成功"),
    })
    @InspectCustomer
    @PostMapping("orders")
    public Object createOrder(@LoginUser Long customerId, @Validated @RequestBody OrderNewVo orderInfo) {
        if (logger.isDebugEnabled()) {
            logger.debug("post orders; vo=" + orderInfo);
        }
        // TODO - region id 合法性检查
        return ResponseUtils.make(orderService.createOrder(customerId, orderInfo));
    }


    /**
     * o4: 买家查询订单完整信息 (普通，团购，预售) [DONE]
     *
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:44
     * Modified at 2020/11/5 15:44
     */
    @ApiOperation(value = "买家查询订单完整信息 (普通，团购，预售)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @InspectCustomer
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
     *
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:44
     * Modified at 2020/11/5 15:44
     */
    @ApiOperation(value = "买家修改本人名下订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "body", required = true, dataType = "Object", paramType = "body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectCustomer // 需要登入
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
     * o6: 买家取消 / 逻辑删除本人名下订单 [DONE]
     *
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:50
     * Modified at 2020/11/5 15:50
     */
    @ApiOperation(value = "买家取消 / 逻辑删除本人名下订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 800, message = "订单状态禁止"),
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectCustomer
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
     * o7: 买家确认收货 [DONE]
     *
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:50
     * Modified at 2020/11/5 15:50
     */
    @ApiOperation(value = "买家取消 / 逻辑删除本人名下订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 800, message = "订单状态禁止"),
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectCustomer
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
     * o8: 买家将团购订单转为普通订单 [DONE]
     *
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:50
     * Modified at 2020/11/5 15:50
     */
    @ApiOperation(value = "买家取消 / 逻辑删除本人名下订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 800, message = "订单状态禁止"),
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectCustomer
    @PostMapping("orders/{id}/groupon-normal")
    public Object exchangeToNormalOrder(@PathVariable Long id,
                                        @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("post orders/{id}/groupon-normal; id=" + id + " customerId=" + customerId);
        }
        // 调用服务层
        return ResponseUtils.make(orderService.buyerChangeGroupon(id, customerId));
    }


    /**
     * o9: 店家查询商户所有订单 (概要) [DONE]
     *
     * @param orderSn    订单号
     * @param state      订单状态
     * @param beginTime  开始时间
     * @param endTime    结束时间
     * @param page       页数
     * @param pageSize   每页包括的记录数量
     * @param customerId 用户 ID
     * @return java.lang.Object
     * @author Han Li
     * Created at 25/11/2020 15:30
     * Created by Han Li at 25/11/2020 15:30
     */
    @ApiOperation(value = "店家查询商户所有订单 (概要)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @InspectAdmin  // 需要管理员登入
    @GetMapping("shops/{shopId}/orders")
    public Object adminGetShopAllOrders(@PathVariable Long shopId,
                                        @RequestParam(required = false) Long customerId,
                                        @RequestParam(required = false) String orderSn,
                                        @RequestParam(required = false) Byte state, // API 没有但以备不时之需
                                        @RequestParam(required = false) Byte type, // API 没有但以备不时之需
                                        @RequestParam(required = false) String beginTime,
                                        @RequestParam(required = false) String endTime,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer pageSize,
                                        @LoginUser Long adminId,
                                        @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get states: shopId=" + shopId + " customerId=" + customerId + " orderSn=" + orderSn +
                    " state=" + state + " beginTime=" + beginTime +
                    " endTime=" + endTime + " page=" + page +
                    " pageSize=" + pageSize + " adminId=" + adminId);
        }
        // 检查是否具有查询对应店铺订单的权限，若没有返回 404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        // 获取数据并返回
        return ResponseUtils.make(orderService.getShopOrders(
                shopId, customerId, orderSn, state, beginTime, endTime, page, pageSize
        ));
    }

    /**
     * o10: 管理员建立售后订单 [改为内部 API：12/12/2020]
     *
     * @param orderVo 订单详情
     * @return java.lang.Object
     * @author Han Li
     * Created at 29/11/2020 13:24
     * Created by Han Li at 29/11/2020 13:24
     */
    @ApiOperation(value = "管理员建立售后订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 800, message = "商品库存不足"),
            @ApiResponse(code = 0, message = "成功"),
    })
    @InspectAdmin // 管理员登入
//    @PostMapping("shops/{shopId}/orders")
    @Deprecated
    public Object createAfterSaleOrder(@RequestBody AfterSaleOrderVo orderVo,
                                       @PathVariable Long shopId,
                                       @LoginUser Long adminId,
                                       @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("post orders; vo=" + orderVo);
        }
        // 检查是否具有创建对应店铺订单的权限，若没有返回 404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(orderService.createAfterSaleOrder(adminShopId, orderVo));
    }

    /**
     * o11: 店家修改订单 (留言) [DONE]
     *
     * @return java.lang.Object
     * @author Han Li
     * Created at 29/11/2020 17:23
     * Created by Han Li at 29/11/2020 17:23
     */
    @ApiOperation(value = "店家修改订单 (留言)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "body", required = true, dataType = "Object", paramType = "body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin // 需要登入
    @PutMapping("shops/{shopId}/orders/{id}")
    public Object shopModifyOrder(@PathVariable Long id,
                                  @PathVariable Long shopId,
                                  @RequestBody OrderEditVo orderEditVo,
                                  @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("shops/{shopId}/orders/{id}; shopId=" + shopId + " id=" + id + " vo=" + orderEditVo);
        }
        // 检查是否具有创建对应店铺订单的权限，若没有返回 404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        // 调用服务层
        return ResponseUtils.make(orderService.shopModifyOrder(id, shopId, orderEditVo));
    }

    /**
     * o12: 店家查询店内订单完整信息 (普通，团购，预售) [DONE]
     *
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:44
     * Modified at 2020/11/5 15:44
     */
    @ApiOperation(value = "店家查询店内订单完整信息 (普通，团购，预售)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @InspectAdmin
    @GetMapping("shops/{shopId}/orders/{id}")
    public Object getDetailedOrder(@PathVariable Long id,
                                   @PathVariable Long shopId,
                                   @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get shops/{shopId}/orders/{id}; shopId=" + shopId + " id=" + id);
        }
        // 检查是否具有创建对应店铺订单的权限，若没有返回 404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        // 调用服务层
        return ResponseUtils.make(orderService.getShopOrder(id, shopId));
    }

    /**
     * o13: 店铺取消订单 [DONE]
     *
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:50
     * Modified at 2020/11/5 15:50
     */
    @ApiOperation(value = "店铺取消订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 800, message = "订单状态禁止"),
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin
    @DeleteMapping("shops/{shopId}/orders/{id}")
    public Object shopCancelOrder(@PathVariable Long shopId,
                                  @PathVariable Long id,
                                  @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("shops/{shopId}/orders/{id}; shopId=" + shopId + " id=" + id);
        }
        // 检查是否具有创建对应店铺订单的权限，若没有返回 404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        // 调用服务层
        return ResponseUtils.make(orderService.shopCancelOrder(id, shopId));
    }

    /**
     * o14: 店铺发货 [DONE]
     *
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:50
     * Modified at 2020/11/5 15:50
     */
    @ApiOperation(value = "店铺发货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 800, message = "订单状态禁止"),
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin
    @PutMapping("shops/{shopId}/orders/{id}/deliver")
    public Object shopDeliverOrder(@PathVariable Long shopId,
                                   @PathVariable Long id,
                                   @RequestBody Map<String, String> body,
                                   @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("shops/{shopId}/orders/{id}/deliver; shopId=" + shopId + " id=" + id);
        }
        // 检查是否具有发货的权限，若没有返回 404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        String sn = body.get("freightSn");
        if (null == sn) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.BAD_REQUEST));
        }
        // 调用服务层
        return ResponseUtils.make(orderService.shopDeliverOrder(id, shopId, sn));
    }
}
