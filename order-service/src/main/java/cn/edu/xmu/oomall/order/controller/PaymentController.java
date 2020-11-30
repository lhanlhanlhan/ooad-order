package cn.edu.xmu.oomall.order.controller;

import cn.edu.xmu.oomall.order.aspects.InspectCustomer;
import cn.edu.xmu.oomall.order.enums.PaymentStatus;
import cn.edu.xmu.oomall.order.model.vo.PayPatternsVo;
import cn.edu.xmu.oomall.order.model.vo.PaymentStatusVo;
import cn.edu.xmu.oomall.order.service.PaymentService;
import cn.edu.xmu.oomall.order.utils.ResponseUtils;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 支付控制器类
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
     * 01: 获得支付单的所有状态[DONE]
     *
     * @author 苗新宇
     * Creted ai 27/11/2020 08:32
     * @return java.lang.Object
     */
    @ApiOperation(value="获得支付单的所有状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required=true, dataType="String", paramType="header")
    })
    @ApiResponses({
            @ApiResponse(code=0,message="成功"),
    })
    @InspectCustomer//需要登录
    @GetMapping("payments/states")
    public Object getPaymentStatus() {
        if(logger.isDebugEnabled()){
            logger.debug("get payments/states");
        }
        //创造对应枚举数组
        List<PaymentStatusVo> paymentStatusVos=new ArrayList<>();
        for(PaymentStatus ps: PaymentStatus.values()){
            paymentStatusVos.add(new PaymentStatusVo(ps));
        }
        //返回
        return ResponseUtils.ok(paymentStatusVos);
    }

    /**
     * 02: 获得支付渠道，目前只返回002模拟支付渠道
     * Created at 29/11/2020 11:28
     * Created by 苗新宇at 29/11/2020 11:28
     * @return java.lang.Object
     */
    @ApiOperation(value="获得支付渠道，目前只返回002，模拟支付渠道")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization",value="Token",required=true,dataType="String",paramType="header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectCustomer//需要登录
    @GetMapping("payments/patterns")
    public Object getPaymentPattern(){
        if(logger.isDebugEnabled()){
            logger.debug("get payments/patterns");
        }
        PayPatternsVo payPatternsVo=new PayPatternsVo("002","成功");
        return ResponseUtils.ok(payPatternsVo);
    }
    /**
     * 03: 买家为订单创建支付订单
     *
     * @author 苗新宇
     * Creted ai 27/11/2020 08:32
     * @return java.lang.Object
     */
    @ApiOperation(value="买家为订单创建支付单")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization",value="Token",required=true,dataType="String",paramType="header"),
            @ApiImplicitParam(name="id",required=true,dataType="Integer",paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code=0,message="成功"),
    })
    @GetMapping("orders/{id}/payments")
    public Object getPaymentBill(@PathVariable Integer id){
        if(logger.isDebugEnabled()){
            logger.debug("get orders/{id}/payments;id="+id);
        }
        return null;
    }


}


