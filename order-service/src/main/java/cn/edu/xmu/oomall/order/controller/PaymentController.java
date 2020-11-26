package cn.edu.xmu.oomall.order.controller;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

}
