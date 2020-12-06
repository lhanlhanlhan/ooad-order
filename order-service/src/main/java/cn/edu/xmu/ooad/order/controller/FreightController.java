package cn.edu.xmu.ooad.order.controller;

import cn.edu.xmu.ooad.order.annotations.AdminShop;
import cn.edu.xmu.ooad.order.annotations.LoginUser;
import cn.edu.xmu.ooad.order.aspects.InspectAdmin;
import cn.edu.xmu.ooad.order.aspects.InspectCustomer;
import cn.edu.xmu.ooad.order.model.vo.*;
import cn.edu.xmu.ooad.order.service.FreightService;
import cn.edu.xmu.ooad.order.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.utils.ResponseCode;
import cn.edu.xmu.ooad.order.utils.ResponseUtils;
import cn.edu.xmu.oomall.order.model.vo.*;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运费控制器类
 *
 * @author Chen Kechun
 * Created at 2020/11/5 15:23
 * Modified by Chen Kechun at 2020/11/25 17:06 下午
 **/
@Api(value = "运费服务", tags = "freight")
@RestController
@RequestMapping(value = "/freight", produces = "application/json;charset=UTF-8")
public class FreightController {

    private static final Logger logger = LoggerFactory.getLogger(FreightController.class);

    // 运费服务
    @Autowired
    private FreightService freightService;

    /**
     * f1: 买家用运费模板计算一批订单商品的运费
     *
     * @param rid        地区id
     * @param items      订单商品订货详情
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 1/12/2020 19:45
     * Modified by Chen Kechun at 1/12/2020 19:45
     */
    @ApiOperation(value = "买家用运费模板计算一批订单商品的运费")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "rid", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "items", required = true, dataType = "Array[Object]", paramType = "body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectCustomer
    @PostMapping("region/{rid}/price")
    public Object getFreightPriceByModel(@PathVariable Long rid,
                                         @Validated @RequestBody List<OrderItemVo> items) {
        if (logger.isDebugEnabled()) {
            logger.debug("post region/{rid}/price; rid=" + rid + " items=" + items);
        }
        // 先判断输入是否有误
        if (items == null || items.size() == 0) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.BAD_REQUEST));
        }
        // 调用服务层
        return ResponseUtils.make(freightService.calcFreight(rid, items));
    }

    /**
     * f2: 管理员定义商铺运费模板 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 15:45
     * Modified by Han Li at 4/12/2020 15:45
     */
    @ApiOperation(value = "管理员定义商铺运费模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "freightModelInfo", required = true, dataType = "Object", paramType = "body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @PostMapping("shops/{id}/freightmodels")
    public Object createFreightModel(@PathVariable Long id,
                                     @Validated @RequestBody FreightModelNewVo freightModelNewVo,
                                     @LoginUser Long adminId,
                                     @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("post shops/{id}/freightmodels; id=" + id + " vo=" + freightModelNewVo + " adminId=" + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(id)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.createShopGoodsFreightModel(id, freightModelNewVo));
    }

    /**
     * f3: 获得店铺中商品的运费模板 [Done]
     *
     * @param name     模板名称
     * @param page     页码
     * @param pageSize 每页数目
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 1/12/2020 19:45
     * Created by Chen Kechun at 1/12/2020 19:45
     */
    @ApiOperation(value = "获得店铺中商品的运费模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //管理员登录
    @GetMapping("shops/{id}/freightmodels")
    public Object getFreightModel(@PathVariable Long id,                     //店铺id
                                  @RequestParam(required = false) String name,
                                  @RequestParam(required = false) Integer page,
                                  @RequestParam(required = false) Integer pageSize,
                                  @LoginUser Long adminId,
                                  @AdminShop Long adminShopId) {

        if (logger.isDebugEnabled()) {
            logger.debug("get shops/{id}/freightmodels; id = " + id + " name = " + name + " page = " + page +
                    " pageSize = " + pageSize + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(id)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.getShopGoodsFreightModel(id, name, page, pageSize));
    }

    /**
     * f4: 管理员克隆店铺的运费模板 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 19:45
     * Created by Chen Kechun at 2/12/2020 19:45
     */
    @ApiOperation(value = "管理员克隆店铺的运费模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @PostMapping("shops/{shopId}/freightmodels/{id}/clone")
    public Object cloneFreightModel(@PathVariable Long shopId,
                                    @PathVariable Long id,
                                    @LoginUser Long adminId,
                                    @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("post shops/{shopId}/freightmodels/{id}/clone; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.cloneFreightModel(shopId, id));
    }

    /**
     * f5: 获得运费模板概要 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 19:45
     * Created by Chen Kechun at 2/12/2020 19:45
     */
    @ApiOperation(value = "获得运费模板概要")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @GetMapping("freightmodels/{id}")
    // TODO - 主语是谁？商家还是平台管理员？
    public Object getFreightModelSimple(@PathVariable Long id,
                                        @LoginUser Long adminId,
                                        @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get freightmodels/{id};id = " + id + " adminId = " + adminId);
        }
        return ResponseUtils.make(freightService.getFreightModelSimple(id, adminShopId));
    }

    /**
     * f6: 管理员修改店铺的运费模板 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 23:45
     * Created by Chen Kechun at 2/12/2020 23:45
     */
    @ApiOperation(value = "管理员修改店铺的运费模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "freightModelInfo", required = true, dataType = "Object", paramType = "body")
    })
    @ApiResponses({
            @ApiResponse(code = 802, message = "运费模板名重复"),
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @PutMapping("shops/{shopId}/freightmodels/{id}")
    public Object modifyShopFreightModel(@PathVariable Long shopId,
                                         @PathVariable Long id,
                                         @Validated @RequestBody FreightModelEditVo freightModelEditVo,
                                         @LoginUser Long adminId,
                                         @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put shops/{shopId}/freightmodels/{id}; shopId = " + shopId + " id=" + id + " adminId = " + adminId + "vo=" + freightModelEditVo);
        }
        // 判断是不是所有属性都为空值
        if (freightModelEditVo.getName() == null &&
            freightModelEditVo.getUnit() == null) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.BAD_REQUEST));
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.modifyShopFreightModel(shopId, id, freightModelEditVo));
    }

    /**
     * f7: 删除运费模板，[Done]
     * 需同步删除与商品的
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 19:45
     * Created by Chen Kechun at 2/12/2020 19:45
     */
    //TODO - 需同步删除与商品的
    @ApiOperation(value = "删除运费模板，需同步删除与商品的")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @DeleteMapping("shops/{shopId}/freightmodels/{id}")
    public Object deleteFreightModel(@PathVariable Long shopId,
                                     @PathVariable Long id,
                                     @LoginUser Long adminId,
                                     @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("delete shops/{shopId}/freightmodels/{id}; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.deleteShopFreightModel(shopId, id));
    }

    /**
     * f8: 店家或管理员为商铺定义默认运费模板 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 19:45
     * Created by Chen Kechun at 2/12/2020 19:45
     */
    @ApiOperation(value = "店家或管理员为商铺定义默认运费模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @PostMapping("shops/{shopId}/freightmodels/{id}/default")
    // TODO 到底freight_models还是freightmodels？？？
    public Object defineDefaultFreightModel(@PathVariable Long shopId,
                                            @PathVariable Long id,
                                            @LoginUser Long adminId,
                                            @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("delete shops/{shopId}/freightmodels/{id}; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.defineDefaultFreightModel(shopId, id));
    }

    /**
     * f9: 管理员定义重量模板明细 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 19:45
     * Created by Chen Kechun at 2/12/2020 19:45
     */
    @ApiOperation(value = "管理员定义重量模板明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "freightModelInfo", required = true, dataType = "Object", paramType = "body")
    })
    @ApiResponses({
            @ApiResponse(code = 803, message = "运费模板中该地区已定义"),
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @PostMapping("shops/{shopId}/freightmodels/{id}/weightItems")
    public Object defineWeightFreightModle(@PathVariable Long shopId,
                                           @PathVariable Long id,
                                           @RequestBody WeightFreightModelVo weightFreightModelVo,
                                           @LoginUser Long adminId,
                                           @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put shops/{shopId}/freightmodels/{id}/weightItems;shopId = " + shopId + " id=" + id + " adminId = " + adminId + "vo=" + weightFreightModelVo);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.createWeightFreightModel(shopId, id, weightFreightModelVo));
    }

    /**
     * f10: 管理员查询重量运费模板明细 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 19:45
     * Created by Chen Kechun at 2/12/2020 19:45
     */
    @ApiOperation(value = "管理员查询重量运费模板明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @GetMapping("shops/{shopId}/freightmodels/{id}/weightItems")
    public Object getWeightFreightModel(@PathVariable Long shopId,
                                        @PathVariable Long id,
                                        @LoginUser Long adminId,
                                        @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get shops/{shopId}/freightmodels/{id}/weightItems; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.getWeightFreightModel(shopId, id));
    }

    /**
     * f11: 管理员定义件数模板明细 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 19:45
     * Created by Chen Kechun at 2/12/2020 19:45
     */
    @ApiOperation(value = "管理员定义件数模板明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "freightModelInfo", required = true, dataType = "Object", paramType = "body")
    })
    @ApiResponses({
            @ApiResponse(code = 803, message = "运费模板中该地区已定义"),
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @PostMapping("shops/{shopId}/freightmodels/{id}/pieceItems")
    public Object definePieceFreightModle(@PathVariable Long shopId,
                                          @PathVariable Long id,
                                          @RequestBody PieceFreightModelVo pieceFreightModelVo,
                                          @LoginUser Long adminId,
                                          @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put shops/{shopId}/freightmodels/{id};shopId = " + shopId + " id=" + id + " adminId = " + adminId + "vo=" + pieceFreightModelVo);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.createPieceFreightModel(shopId, id, pieceFreightModelVo));
    }

    /**
     * f12: 管理员查询件数运费模板明细 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 19:45
     * Created by Chen Kechun at 2/12/2020 19:45
     */
    @ApiOperation(value = "管理员查询件数运费模板明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @GetMapping("shops/{shopId}/freightmodels/{id}/pieceItems")
    public Object getPieceFreightModel(@PathVariable Long shopId,
                                       @PathVariable Long id,
                                       @LoginUser Long adminId,
                                       @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get shops/{shopId}/freightmodels/{id}/pieceItems; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.getPieceFreightModel(shopId, id));
    }

    /**
     * f13: 管理员修改重量运费模板明细 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 19:45
     * Created by Chen Kechun at 2/12/2020 19:45
     */
    @ApiOperation(value = "管理员修改重量模板明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "freightModelInfo", required = true, dataType = "Object", paramType = "body")
    })
    @ApiResponses({
            @ApiResponse(code = 803, message = "运费模板中该地区已定义"),
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @PutMapping("shops/{shopId}/weightItems/{id}")
    public Object modifyWeightFreightModel(@PathVariable Long shopId,
                                           @PathVariable Long id,
                                           @RequestBody WeightFreightModelVo weightFreightModelVo,
                                           @LoginUser Long adminId,
                                           @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put shops/{shopId}/weightItems/{id}; shopId = " + shopId + " id=" + id + " adminId = " + adminId + "vo=" + weightFreightModelVo);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.modifyWeightFreightModel(shopId, id, weightFreightModelVo));
    }

    /**
     * f14: 店家或管理删掉重量运费模压明细 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 19:45
     * Created by Chen Kechun at 2/12/2020 19:45
     */
    @ApiOperation(value = "店家或管理删掉重量运费模压明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @DeleteMapping("shops/{shopId}/weightItems/{id}")
    public Object deleteWeightFreightModel(@PathVariable Long shopId,
                                           @PathVariable Long id,
                                           @LoginUser Long adminId,
                                           @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("delete shops/{shopId}/weightItems/{id}; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.deleteWeightFreightModel(shopId, id));
    }


    /**
     * f15: 管理员修改件数运费模板明细 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 19:45
     * Created by Chen Kechun at 2/12/2020 19:45
     */
    @ApiOperation(value = "管理员修改件数模板明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "freightModelInfo", required = true, dataType = "Object", paramType = "body")
    })
    @ApiResponses({
            @ApiResponse(code = 803, message = "运费模板中该地区已定义"),
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @PutMapping("shops/{shopId}/pieceItems/{id}")
    public Object modifyPieceFreightModel(@PathVariable Long shopId,
                                          @PathVariable Long id,
                                          @RequestBody PieceFreightModelVo vo,
                                          @LoginUser Long adminId,
                                          @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put shops/{shopId}/freightmodels/{id};shopId = " + shopId + " id=" + id + " adminId = " + adminId + "vo=" + vo);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.modifyPieceFreightModel(shopId, id, vo));
    }

    /**
     * f16: 店家或管理删掉件数运费模压明细 [Done]
     *
     * @return java.lang.Object
     * @author Chen Kechun
     * Created at 2/12/2020 19:45
     * Created by Chen Kechun at 2/12/2020 19:45
     */
    @ApiOperation(value = "店家或管理删掉件数运费模板明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @InspectAdmin //登录
    @DeleteMapping("shops/{shopId}/pieceItems/{id}")
    public Object deletePieceFreightModel(@PathVariable Long shopId,
                                          @PathVariable Long id,
                                          @LoginUser Long adminId,
                                          @AdminShop Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("delete shops/{shopId}/pieceItems/{id}; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST));
        }
        return ResponseUtils.make(freightService.deletePieceFreightModel(shopId, id));
    }
}
