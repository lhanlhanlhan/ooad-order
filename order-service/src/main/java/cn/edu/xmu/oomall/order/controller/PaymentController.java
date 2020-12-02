package cn.edu.xmu.oomall.order.controller;

import cn.edu.xmu.oomall.order.annotations.AdminShop;
import cn.edu.xmu.oomall.order.annotations.LoginUser;
import cn.edu.xmu.oomall.order.aspects.InspectAdmin;
import cn.edu.xmu.oomall.order.aspects.InspectCustomer;
import cn.edu.xmu.oomall.order.enums.PaymentStatus;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.model.vo.PayPatternsVo;
import cn.edu.xmu.oomall.order.model.vo.PaymentInfoVo;
import cn.edu.xmu.oomall.order.model.vo.PaymentStatusVo;
import cn.edu.xmu.oomall.order.service.OrderService;
import cn.edu.xmu.oomall.order.service.PaymentService;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import cn.edu.xmu.oomall.order.utils.ResponseUtils;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 支付控制器类
 *
 * @author Miao Xinyu
 * Created at 2020/11/5 15:23
 * Modified by Han Li at 2020/11/5 15:23
 **/
@Api(value = "支付服务", tags = "payment")
@RestController
@RequestMapping(value = "/payment", produces = "application/json;charset=UTF-8")
public class PaymentController {

    //日志记录器
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    //支付服务
    @Autowired
    private PaymentService paymentService;

    //订单服务
    @Autowired
    private OrderService orderService;

    /**
     * 01: 获得支付单的所有状态[DONE]
     *
     * @return java.lang.Object
     * @author 苗新宇
     * Creted ai 27/11/2020 08:32
     */
    @ApiOperation(value = "获得支付单的所有状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header")
    })
    @InspectCustomer//需要登录
    @GetMapping("payments/states")
    public Object getPaymentStatus(@LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get payments/states,Login customerId" + customerId);
        }
        //创造对应枚举数组
        List<PaymentStatusVo> paymentStatusVos = new ArrayList<>();
        for (PaymentStatus ps : PaymentStatus.values()) {
            paymentStatusVos.add(new PaymentStatusVo(ps));
        }
        //返回
        return ResponseUtils.ok(paymentStatusVos);
    }

    /**
     * 02: 获得支付渠道，目前只返回002模拟支付渠道[DONE]
     * Created at 29/11/2020 11:28
     * Created by 苗新宇at 29/11/2020 11:28
     *
     * @return java.lang.Object
     */
    @ApiOperation(value = "获得支付渠道，目前只返回002，模拟支付渠道")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header")
    })
    @InspectCustomer//需要登录
    @GetMapping("payments/patterns")
    public Object getPaymentPattern(@LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get payments/patterns,Login customerId" + customerId);
        }
        PayPatternsVo payPatternsVo = new PayPatternsVo("002", "成功");
        return ResponseUtils.ok(payPatternsVo);
    }

    /**
     * 03: 买家为订单创建支付订单[DONE]
     *
     * @return java.lang.Object
     * @author 苗新宇
     * Creted ai 27/11/2020 08:32
     */
    @ApiOperation(value = "买家为订单创建支付单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @InspectCustomer  // 需要登入
    @PostMapping("orders/{id}/payments")
    public Object createPaymentBill(@RequestBody PaymentInfoVo paymentInfoVO,
                                    @PathVariable Long id,
                                    @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get orders/{id}/payments;orderid=" + id + ";customerId " + customerId);
        }

        return ResponseUtils.make(paymentService.createPaymentOrder(id, paymentInfoVO));
    }

    /**
     * 04: 买家【根据订单号】查询自己的支付信息[DONE]
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "买家查询自己的支付信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Long", paramType = "path")
    })
    @InspectCustomer  // 需要登入
    @GetMapping("orders/{id}/payments")
    public Object getSelfPaymentBillByOrderId(@PathVariable Long id,
                                              @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get orders/{id}/payments;id=" + id + ";customerId " + customerId);
        }
        //调用服务层
        return ResponseUtils.make(paymentService.getPaymentOrderByOrderId(id));
    }

    /**
     * 05. 管理员查询订单的支付信息[DONE]
     *
     * @param shopId 店铺ID
     * @param id     订单ID
     * @return java.lang.Object
     * @author 苗新宇
     * Creted ai 02/12/2020 17:20
     */
    @ApiOperation(value = "管理员查询订单的支付信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Long", paramType = "path")
    })
    @InspectAdmin // 管理员登入
    @GetMapping("shops/{shopId}/orders/{id}/payments")
    public Object getOrderPayInfo(@PathVariable Long shopId,
                                  @PathVariable Long id,
                                  @LoginUser Long adminId,
                                  @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get states: shopId = " + shopId + "; orderId = " + id +
                    "; adminId = " + adminId + "; adminShopId= " + adminShopId);
        }
        //检查是否具有查询对应店铺支付单的权限，若没有就返回404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }

        return ResponseUtils.make(paymentService.getOrderPaymentInfo(shopId, id));
    }

    /**
     * 06. 买家为售后单创建支付单[DONE]
     *
     * @param paymentInfoVo 支付单信息Vo
     * @param id            售后单ID
     * @return java.lang.Object
     * @author 苗新宇
     * Creted ai 02/12/2020 17:39
     */
    @ApiOperation(value = "买家为售后单创建支付单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @InspectCustomer
    @PostMapping("aftersales/{id}/payments")
    public Object createPaymentBillFromAftersale(@RequestBody PaymentInfoVo paymentInfoVo,
                                                 @PathVariable Long id,
                                                 @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get orders/{id}/payments;id=" + id + ";customerId " + customerId);
        }

        return ResponseUtils.make(paymentService.createPaymentBillForAftersaleOrder(id, paymentInfoVo));
    }

    /**
     * 07 买家【根据售后单号】查询自己的支付信息[DONE]
     *
     * @param id 支付单ID
     * @return java.lang.Object
     * @author 苗新宇
     * Creted at 02/12/2020 19:22
     */
    @ApiOperation(value = "买家【根据售后单号】查询自己的支付信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Long", paramType = "path")
    })
    @InspectCustomer//需要登入
    @GetMapping("aftersales/{id}/payments")
    public Object getSelfPaymentBillByAfterSaleId(@PathVariable Long id,
                                                  @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get aftersales/{id}/payments;id=" + id + ";customerId " + customerId);
        }
        //调用服务层
        return ResponseUtils.make(paymentService.getPaymentOrderByAftersaleId(id));
    }

    /**
     * 08. 管理员查询售后单的支付信息[DONE]
     *
     * @param shopId 店铺ID
     * @param id     售后单ID
     * @return java.lang.Object
     * @author 苗新宇
     * Creted ai 02/12/2020 17:20
     */
    @ApiOperation(value = "管理员查询售后单的支付信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Long", paramType = "path")
    })
    @InspectAdmin
    @GetMapping("shops/{shopId}/aftersales/{id}/payments")
    public Object getAfterSalePayInfo(@PathVariable Long shopId,
                                      @PathVariable Long id,
                                      @LoginUser Long adminId,
                                      @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get states: shopId = " + shopId + "aftersaleId = " + id +
                    " adminId = " + adminId + " adminShopId " + adminShopId);
        }
        //检查是否具有查询对应店铺支付单的权限，若没有就返回404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }

        //调用服务层
        return ResponseUtils.make(paymentService.getPaymentOrderInfo(shopId, id));
    }

}


