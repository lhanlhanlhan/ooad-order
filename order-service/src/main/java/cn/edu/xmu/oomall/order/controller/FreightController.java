package cn.edu.xmu.oomall.order.controller;

import cn.edu.xmu.oomall.order.annotations.LoginUser;
import cn.edu.xmu.oomall.order.aspects.InspectCustomer;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运费控制器类
 * @author Chen Kechun
 * Created at 2020/11/5 15:23
 * Modified by Chen Kechun at 2020/11/25 17:06 下午
 **/
@Api(value = "运费服务", tags = "freight")
@RestController
@RequestMapping(value = "/freight", produces = "application/json;charset=UTF-8")
public class FreightController {

    private static final Logger logger = LoggerFactory.getLogger(FreightController.class);



    @InspectCustomer //登录
    @GetMapping("shops/{id}/freightmodel")
    public Object getFreightModel(@RequestParam(required = true) Integer shopId,
                                  @RequestParam(required = false) String name,
                                  @RequestParam(required = false) Integer page,
                                  @RequestParam(required = false) Integer pageSize,
                                  @LoginUser Integer customerId){

        return null;
    }
}
