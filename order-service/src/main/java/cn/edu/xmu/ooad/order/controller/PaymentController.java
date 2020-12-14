package cn.edu.xmu.ooad.order.controller;

import cn.edu.xmu.ooad.annotation.Audit;
import cn.edu.xmu.ooad.annotation.Depart;
import cn.edu.xmu.ooad.annotation.LoginUser;
import cn.edu.xmu.ooad.order.enums.PayPattern;
import cn.edu.xmu.ooad.order.enums.PaymentStatus;
import cn.edu.xmu.ooad.order.model.vo.PaymentNewVo;
import cn.edu.xmu.ooad.order.model.vo.PaymentPatternVo;
import cn.edu.xmu.ooad.order.model.vo.PaymentStatusVo;
import cn.edu.xmu.ooad.order.service.PaymentService;
import cn.edu.xmu.ooad.order.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.utils.ResponseCode;
import cn.edu.xmu.ooad.order.utils.ResponseUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /**
     * p1: 获得支付单的所有状态 [DONE]
     *
     * @return java.lang.Object
     * @author 苗新宇
     * Created at 27/11/2020 08:32
     * Modified by Han Li at 5/12/2020
     */
    @ApiOperation(value = "获得支付单的所有状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header")
    })
    @Audit//需要登录
    @GetMapping("payments/states")
    public Object getPaymentStatus(@LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get payments/states, Login customerId" + customerId);
        }
        // 创造对应枚举数组
        List<PaymentStatusVo> paymentStatusVos = new ArrayList<>();
        for (PaymentStatus ps : PaymentStatus.values()) {
            paymentStatusVos.add(new PaymentStatusVo((ps)));
        }
        // 返回
        return ResponseUtils.ok(paymentStatusVos);
    }

    /**
     * p2: 获得支付渠道，目前只返回002模拟支付渠道 [DONE]
     * Created at 29/11/2020 11:28
     * Created by 苗新宇at 29/11/2020 11:28
     *
     * @return java.lang.Object
     */
    @ApiOperation(value = "获得支付渠道，目前只返回002，模拟支付渠道")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header")
    })
    @Audit//需要登录
    @GetMapping("payments/patterns")
    public Object getPaymentPattern(@LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get payments/patterns,Login customerId" + customerId);
        }
        // 创造对应枚举数组
        List<PaymentPatternVo> patternVos = new ArrayList<>();
        for (PayPattern pp : PayPattern.values()) {
            patternVos.add(new PaymentPatternVo((pp)));
        }
        // 返回
        return ResponseUtils.ok(patternVos);
    }


    /**
     * p3: 买家为订单创建支付订单
     * TODO - 订单分单
     *
     * @param paymentNewVo
     * @param id
     * @param customerId
     * @return java.lang.Object
     * @author 苗新宇
     * Created at 05/12/2020 15:10
     * Created by Han Li at 05/12/2020 15:10
     */
    @ApiOperation(value = "买家为订单创建支付单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @Audit  // 需要登入
    @PostMapping("orders/{id}/payments")
    public Object createPaymentBill(@RequestBody PaymentNewVo paymentNewVo,
                                    @PathVariable Long id,
                                    @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get orders/{id}/payments; orderid=" + id + ";customerId " + customerId + " vo=" + paymentNewVo);
        }
        return ResponseUtils.make(paymentService.createPayment(id, customerId, paymentNewVo));
    }

    /**
     * p4: 买家【根据订单号】查询自己的支付信息 [DONE]
     *
     * @param id
     * @param customerId
     * @return java.lang.Object
     * @author Miao Xinyu
     * Created at 05/12/2020 15:09
     * Created by Han Li at 05/12/2020 15:09
     */
    @ApiOperation(value = "买家查询自己的支付信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Long", paramType = "path")
    })
    @Audit  // 需要登入
    @GetMapping("orders/{id}/payments")
    public Object getSelfPaymentBillByOrderId(@PathVariable Long id,
                                              @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get orders/{id}/payments;id=" + id + ";customerId " + customerId);
        }
        //调用服务层
        return ResponseUtils.make(paymentService.getPaymentByOrderId(customerId, id));
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
    @Audit // 管理员登入
    @GetMapping("shops/{shopId}/orders/{id}/payments")
    public Object getOrderPayInfo(@PathVariable Long shopId,
                                  @PathVariable Long id,
                                  @LoginUser Long adminId,
                                  @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get states: shopId = " + shopId + "; orderId = " + id +
                    "; adminId = " + adminId + "; adminShopId= " + adminShopId);
        }
        //检查是否具有查询对应店铺支付单的权限，若没有就返回404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_OUT_SCOPE));
        }

        return ResponseUtils.make(paymentService.getOrderPaymentInfo(shopId, id));
    }

    /**
     * 06. 买家为售后单创建支付单[DONE]
     *
     * @param paymentNewVo 支付单信息Vo
     * @param id           售后单ID
     * @return java.lang.Object
     * @author 苗新宇
     * Creted ai 02/12/2020 17:39
     */
    @ApiOperation(value = "买家为售后单创建支付单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @Audit
    @PostMapping("aftersales/{id}/payments")
    public Object createPaymentBillFromAftersale(@RequestBody PaymentNewVo paymentNewVo,
                                                 @PathVariable Long id,
                                                 @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get orders/{id}/payments;id=" + id + ";customerId " + customerId);
        }

        return ResponseUtils.make(paymentService.createPaymentForAftersaleOrder(id, paymentNewVo));
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
    @Audit//需要登入
    @GetMapping("aftersales/{id}/payments")
    public Object getSelfPaymentBillByAfterSaleId(@PathVariable Long id,
                                                  @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get aftersales/{id}/payments;id=" + id + ";customerId " + customerId);
        }
        //调用服务层
        return ResponseUtils.make(paymentService.getPaymentByAftersaleId(id, customerId));
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
    @Audit
    @GetMapping("shops/{shopId}/aftersales/{id}/payments")
    public Object getAfterSalePayInfo(@PathVariable Long shopId,
                                      @PathVariable Long id,
                                      @LoginUser Long adminId,
                                      @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get states: shopId = " + shopId + "aftersaleId = " + id +
                    " adminId = " + adminId + " adminShopId " + adminShopId);
        }
        //检查是否具有查询对应店铺支付单的权限，若没有就返回404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_OUT_SCOPE));
        }

        //调用服务层
        return ResponseUtils.make(paymentService.getPaymentInfo(shopId, id));
    }

    /**
     * 09. 管理员创建退款信息，需检查Payment是否是此商铺的payment [Done]
     *
     * @param shopId         店铺ID
     * @param id             支付单ID
     * @param refundAmountVo 退款金额
     * @param adminId        管理员ID
     * @param adminShopId
     * @return
     */
    @ApiOperation(value = "管理员创建退款信息，需检查Payment是否是此商铺的payment")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Long", paramType = "path")
    })
    @Audit
    @PostMapping("shops/{shopId}/payments/{id}/refund")
    public Object createRefund(@PathVariable Long shopId,
                               @PathVariable Long id,
                               @RequestBody Map<String, Long> refundAmountVo,
                               @LoginUser Long adminId,
                               @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get states: shopId = " + shopId + "; paymentId = " + id +
                    "; refundAmount =" + refundAmountVo +
                    "; adminId = " + adminId + "; adminShopId " + adminShopId);
        }
        // 检查是否具有查询对应店铺支付单的权限，若没有就返回404
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_OUT_SCOPE));
        }
        // 获取冀退款之金额
        Long refundAmount = refundAmountVo.get("amount");
        if (refundAmount == null) {
            // 输入金额有误，没有输入
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOT_VALID));
        }
        // 调用服务层
        return ResponseUtils.make(paymentService.createRefund(shopId, id, refundAmount));
    }

    /**
     * 10. 管理员【根据订单ID】查询订单的退款信息
     *
     * @param shopId      店铺ID
     * @param id          订单ID
     * @param adminId
     * @param adminShopId
     * @return java.lang.Object
     */
    @ApiOperation(value = "管理员查询订单的退款信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Long", paramType = "path")
    })
    @Audit
    @GetMapping("shops/{shopId}/orders/{id}/refund")
    public Object adminGetRefundInfoByOrderId(@PathVariable Long shopId,
                                              @PathVariable Long id,
                                              @LoginUser Long adminId,
                                              @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get states: shopId = " + shopId + "; orderId = " + id +
                    "; adminId = " + adminId + "; adminShopId = " + adminShopId);
        }
        return ResponseUtils.make(paymentService.getRefundByOrderId(shopId, id));
    }

    /**
     * 11. 管理员【根据售后单ID】查询订单的退款信息[Done]
     *
     * @param shopId      店铺ID
     * @param id          售后单ID
     * @param adminId
     * @param adminShopId
     * @return java.lang.Object
     */
    @ApiOperation(value = "管理员查询订单的退款信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Long", paramType = "path")
    })
    @Audit
    @GetMapping("shops/{shopId}/aftersales/{id}/refund")
    public Object adminGetRefundInfoByAftersaleId(@PathVariable Long shopId,
                                                  @PathVariable Long id,
                                                  @LoginUser Long adminId,
                                                  @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get states: shopId = " + shopId + "; aftersaleId = " + id +
                    "; adminId = " + adminId + "; adminShopId = " + adminShopId);
        }
        return ResponseUtils.make(paymentService.getRefundByAftersaleId(shopId, id));
    }

    /**
     * 12: 买家【根据订单号】查询自己的退款信息
     *
     * @param id 订单ID
     * @return
     */
    @ApiOperation(value = "买家查询自己的支付信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Long", paramType = "path")
    })
    @Audit  // 需要登入
    @GetMapping("orders/{id}/refunds")
    public Object getSelfRefundInfoByOrderId(@PathVariable Long id,
                                             @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get orders/{id}/payments;orderId=" + id + ";customerId = " + customerId);
        }
        //调用服务层
        return ResponseUtils.make(paymentService.getCustomerRefundByOrderId(customerId, id));
    }

    /**
     * 13: 买家【根据售后单号】查询自己的退款信息
     *
     * @param id 售后单ID
     * @return
     */
    @ApiOperation(value = "买家查询自己的支付信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Long", paramType = "path")
    })
    @Audit  // 需要登入
    @GetMapping("aftersales/{id}/refunds")
    public Object getSelfRefundInfoByAftersaleId(@PathVariable Long id,
                                                 @LoginUser Long customerId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get aftersales/{id}/payments;aftersaleId=" + id + ";customerId = " + customerId);
        }
        //调用服务层
        return ResponseUtils.make(paymentService.getCustomerRefundByAftersaleId(customerId, id));
    }

}


