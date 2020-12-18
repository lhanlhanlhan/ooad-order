package cn.edu.xmu.ooad.order.order.controller;

import cn.edu.xmu.ooad.annotation.Audit;
import cn.edu.xmu.ooad.annotation.Depart;
import cn.edu.xmu.ooad.annotation.LoginUser;
import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.centre.utils.ResponseUtils;
import cn.edu.xmu.ooad.order.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.order.model.vo.OrderBuyerEditVo;
import cn.edu.xmu.ooad.order.order.model.vo.OrderNewVo;
import cn.edu.xmu.ooad.order.order.model.vo.OrderShopEditVo;
import cn.edu.xmu.ooad.order.order.model.vo.OrderStatusVo;
import cn.edu.xmu.ooad.order.order.service.OrderService;
import cn.edu.xmu.ooad.order.require.IOtherService;
import cn.edu.xmu.ooad.util.ResponseCode;
import io.swagger.annotations.*;
import org.apache.dubbo.config.annotation.DubboReference;
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

    @DubboReference(check = false)
    private IOtherService iOtherService;

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
    @Audit // 需要登入
    @GetMapping("orders/states")
    public Object getAllStatus() {
        if (logger.isDebugEnabled()) {
            logger.debug("get orders/states");
        }
        // 创造对应枚举数组
        List<OrderStatusVo> orderStatusVos = new ArrayList<>();
        for (OrderChildStatus os : OrderChildStatus.values()) {
            orderStatusVos.add(new OrderStatusVo((os)));
        }
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
    @Audit  // 需要登入
    @GetMapping("orders")
    public Object getAllOrders(@RequestParam(required = false) String orderSn,
                               @RequestParam(required = false) Byte state,
                               @RequestParam(required = false) Byte type, // API 没有但以备不时之需
                               @RequestParam(required = false) String beginTime,
                               @RequestParam(required = false) String endTime,
                               @RequestParam(required = false, defaultValue = "1") Integer page,
                               @RequestParam(required = false, defaultValue = "10") Integer pageSize,
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
    @Audit
    @PostMapping("orders")
    public Object createOrder(@LoginUser Long customerId, @Validated @RequestBody OrderNewVo orderInfo) {
        if (logger.isDebugEnabled()) {
            logger.debug("post orders; vo=" + orderInfo);
        }
        // region id 合法性检查
        boolean regionId;
        try {
            regionId = iOtherService.isRegionIdExists(orderInfo.getRegionId());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR, e.getMessage()));
        }
        if (regionId) {
            // 地区不可达
            return ResponseUtils.make(new APIReturnObject<>(ResponseCode.REGION_NOT_REACH));
        }
        return ResponseUtils.make(orderService.createNormalOrder(customerId, orderInfo));
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
    @Audit
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
    @Audit // 需要登入
    @PutMapping("orders/{id}")
    public Object modifyOrder(@PathVariable Long id,
                              @RequestBody OrderBuyerEditVo orderBuyerEditVo,
                              @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put orders/{id}; id=" + id + " vo=" + orderBuyerEditVo);
        }
        // 调用服务层
        return ResponseUtils.make(orderService.buyerModifyOrder(id, customerId, orderBuyerEditVo));
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
    @Audit
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
    @Audit
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
    @ApiOperation(value = "买家将团购订单转为普通订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 800, message = "订单状态禁止"),
            @ApiResponse(code = 0, message = "成功")
    })
    @Audit
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
    @Audit  // 需要管理员登入
    @GetMapping("shops/{shopId}/orders")
    public Object adminGetShopAllOrders(@PathVariable Long shopId,
                                        @RequestParam(required = false) Long customerId,
                                        @RequestParam(required = false) String orderSn,
                                        @RequestParam(required = false) Byte state, // API 没有但以备不时之需
                                        @RequestParam(required = false) Byte type, // API 没有但以备不时之需
                                        @RequestParam(required = false) String beginTime,
                                        @RequestParam(required = false) String endTime,
                                        @RequestParam(required = false, defaultValue = "1") Integer page,
                                        @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                        @LoginUser Long adminId,
                                        @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get states: shopId=" + shopId + " customerId=" + customerId + " orderSn=" + orderSn +
                    " state=" + state + " beginTime=" + beginTime +
                    " endTime=" + endTime + " page=" + page +
                    " pageSize=" + pageSize + " adminId=" + adminId);
        }
        // 检查是否具有查询对应店铺订单的权限，若没有返回 404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
        }
        // 获取数据并返回
        return ResponseUtils.make(orderService.getShopOrders(
                shopId, customerId, orderSn, state, beginTime, endTime, page, pageSize
        ));
    }


    /*
    o10: 管理员建立售后订单 [改为内部 API：12/12/2020]
     */


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
    @Audit // 需要登入
    @PutMapping("shops/{shopId}/orders/{id}")
    public Object shopModifyOrder(@PathVariable Long id,
                                  @PathVariable Long shopId,
                                  @Validated @RequestBody OrderShopEditVo orderShopEditVo,
                                  @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("shops/{shopId}/orders/{id}; shopId=" + shopId + " id=" + id + " vo=" + orderShopEditVo);
        }
        // 检查是否具有创建对应店铺订单的权限，若没有返回 404
        if (adminShopId == null || (adminShopId != 0 && !adminShopId.equals(shopId))) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
        }
        // 调用服务层
        return ResponseUtils.make(orderService.shopModifyOrder(id, shopId, orderShopEditVo));
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
    @Audit
    @GetMapping("shops/{shopId}/orders/{id}")
    public Object getDetailedOrder(@PathVariable Long id,
                                   @PathVariable Long shopId,
                                   @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get shops/{shopId}/orders/{id}; shopId=" + shopId + " id=" + id);
        }
        // 检查是否具有创建对应店铺订单的权限，若没有返回 404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit
    @DeleteMapping("shops/{shopId}/orders/{id}")
    public Object shopCancelOrder(@PathVariable Long shopId,
                                  @PathVariable Long id,
                                  @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("shops/{shopId}/orders/{id}; shopId=" + shopId + " id=" + id);
        }
        // 检查是否具有创建对应店铺订单的权限，若没有返回 404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit
    @PutMapping("shops/{shopId}/orders/{id}/deliver")
    public Object shopDeliverOrder(@PathVariable Long shopId,
                                   @PathVariable Long id,
                                   @RequestBody Map<String, String> body,
                                   @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("shops/{shopId}/orders/{id}/deliver; shopId=" + shopId + " id=" + id);
        }
        // 检查是否具有发货的权限，若没有返回 404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
        }
        String sn = body.get("freightSn");
        if (null == sn) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID));
        }
        // 调用服务层
        return ResponseUtils.make(orderService.shopDeliverOrder(id, shopId, sn));
    }
}
