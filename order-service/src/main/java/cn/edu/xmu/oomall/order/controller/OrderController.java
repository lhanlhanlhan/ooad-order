package cn.edu.xmu.oomall.order.controller;

import cn.edu.xmu.oomall.order.model.vo.EditOrderVo;
import cn.edu.xmu.oomall.order.model.vo.NewOrderVo;
import io.swagger.annotations.*;
import org.apache.ibatis.annotations.Delete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器类
 * @author Han Li
 * Created at 2020/11/5 15:21
 * Modified at 2020/11/5 15:21
 **/
@Api(value = "订单服务", tags = "order")
@RestController
@RequestMapping(value = "/order", produces = "application/json;charset=UTF-8")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    /**
     * o1: 获得订单的所有状态
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:25
     * Modified at 2020/11/5 15:25
     */
    @ApiOperation(value = "获得订单的所有状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("orders/states")
    public Object getAllStatus() {
        return null;
    }


    /**
     * o2: 买家查询名下订单 (概要)
     * @return Object
     * @author Han Li
     * Created at 2020/11/5 15:33
     * Modified at 2020/11/5 15:33
     */
    @ApiOperation(value = "买家查询名下订单 (概要)")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("orders")
    public Object getSimpleOrder(@RequestParam(required = false) String orderSn,
                                 @RequestParam(required = false) Integer state,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer pageSize) {
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
        return null;
    }
}
