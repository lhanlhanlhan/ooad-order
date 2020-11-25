package cn.edu.xmu.oomall.order.controller;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运费控制器类
 * @author Han Li
 * Created at 2020/11/5 15:23
 * Modified by Chen Kechun at 2020/11/24 14:50
 **/
@Api(value = "运费服务", tags = "freight")
@RestController
@RequestMapping(value = "/freight", produces = "application/json;charset=UTF-8")
public class FreightController {

    private static final Logger logger = LoggerFactory.getLogger(FreightController.class);

}
