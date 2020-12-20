package cn.edu.xmu.ooad.order.freight.controller;

import cn.edu.xmu.ooad.annotation.Audit;
import cn.edu.xmu.ooad.annotation.Depart;
import cn.edu.xmu.ooad.annotation.LoginUser;
import cn.edu.xmu.ooad.order.centre.model.FreightCalcItem;
import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.centre.utils.ResponseUtils;
import cn.edu.xmu.ooad.order.freight.model.vo.*;
import cn.edu.xmu.ooad.order.freight.service.FreightService;
import cn.edu.xmu.ooad.order.require.IShopService;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // 商品服务
    @DubboReference(check = false)
    private IShopService iShopService;

    /**
     * f1: 买家用运费模板计算一批订单商品的运费
     *
     * @param rid   地区id
     * @param items 订单商品订货详情
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
    @Audit
    @PostMapping("region/{rid}/price")
    public Object getFreightPriceByModel(@PathVariable Long rid,
                                         @Validated @RequestBody List<FreightOrderItemVo> items) {
        if (logger.isDebugEnabled()) {
            logger.debug("post region/{rid}/price; rid=" + rid + " items=" + items);
        }
        // 先判断输入是否有误
        if (items == null || items.size() == 0) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID));
        }
        // 在运算运费前，要提前获得商品模块 sku 信息，否则重复获取 sku 信息
        Map<Long, SkuInfo> skuInfoMap = new HashMap<>(items.size());
        List<FreightCalcItem> freightCalcItems = new ArrayList<>(items.size());
        for (FreightOrderItemVo orderItemVo : items) {
            SkuInfo skuInfo;
            try {
                skuInfo = iShopService.getSkuInfo(orderItemVo.getSkuId());
            } catch (Exception e) {
                logger.error("联系商品模块失败，错误：" + e.getMessage());
                return ResponseUtils.make(new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR, "无法联系商品模块"));
            }
            if (skuInfo == null) {
                return ResponseUtils.make(new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST, "查无商品"));
            }
            skuInfoMap.put(orderItemVo.getSkuId(), skuInfo);
            freightCalcItems.add(orderItemVo.toCalcItem());
        }

        // 计算运费
        int freight = (int) freightService.calcFreight(rid, freightCalcItems, skuInfoMap);
        switch (freight) {
            case -3: // 含禁止物品
                return ResponseUtils.make(new APIReturnObject<>(ResponseCode.REGION_NOT_REACH));
            case -2: // 运费模板 id 未定义
                return ResponseUtils.make(new APIReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST, "未定义的运费模板 id"));
            case -1: // 失败
                return ResponseUtils.make(new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR, "计算运费不成功"));
            default: // OK
                return ResponseUtils.make(new APIReturnObject<>(HttpStatus.CREATED, ResponseCode.OK, freight));
        }
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
    @Audit //登录
    @PostMapping("shops/{id}/freightmodels")
    public Object createFreightModel(@PathVariable Long id,
                                     @Validated @RequestBody FreightModelNewVo freightModelNewVo,
                                     @LoginUser Long adminId,
                                     @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("post shops/{id}/freightmodels; id=" + id + " vo=" + freightModelNewVo + " adminId=" + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(id)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //管理员登录
    @GetMapping("shops/{id}/freightmodels")
    public Object getFreightModel(@PathVariable Long id,                     //店铺id
                                  @RequestParam(required = false) String name,
                                  @RequestParam(required = false, defaultValue = "1") Integer page,
                                  @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                  @LoginUser Long adminId,
                                  @Depart Long adminShopId) {

        if (logger.isDebugEnabled()) {
            logger.debug("get shops/{id}/freightmodels; id = " + id + " name = " + name + " page = " + page +
                    " pageSize = " + pageSize + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(id)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //登录
    @PostMapping("shops/{shopId}/freightmodels/{id}/clone")
    public Object cloneFreightModel(@PathVariable Long shopId,
                                    @PathVariable Long id,
                                    @LoginUser Long adminId,
                                    @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("post shops/{shopId}/freightmodels/{id}/clone; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //登录
    @GetMapping("shops/{shopId}/freightmodels/{id}")
    public Object getFreightModelSimple(@PathVariable Long id,
                                        @PathVariable Long shopId,
                                        @LoginUser Long adminId,
                                        @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get freightmodels/{id};id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //登录
    @PutMapping("shops/{shopId}/freightmodels/{id}")
    public Object modifyShopFreightModel(@PathVariable Long shopId,
                                         @PathVariable Long id,
                                         @Validated @RequestBody FreightModelEditVo freightModelEditVo,
                                         @LoginUser Long adminId,
                                         @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put shops/{shopId}/freightmodels/{id}; shopId = " + shopId + " id=" + id + " adminId = " + adminId + "vo=" + freightModelEditVo);
        }
        // 判断是不是所有属性都为空值
        if (freightModelEditVo.getName() == null &&
                freightModelEditVo.getUnit() == null) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID));
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @ApiOperation(value = "删除运费模板，需同步删除与商品的")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @Audit //登录
    @DeleteMapping("shops/{shopId}/freightmodels/{id}")
    public Object deleteFreightModel(@PathVariable Long shopId,
                                     @PathVariable Long id,
                                     @LoginUser Long adminId,
                                     @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("delete shops/{shopId}/freightmodels/{id}; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //登录
    @PostMapping("shops/{shopId}/freightmodels/{id}/default")
    public Object defineDefaultFreightModel(@PathVariable Long shopId,
                                            @PathVariable Long id,
                                            @LoginUser Long adminId,
                                            @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("delete shops/{shopId}/freightmodels/{id}; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //登录
    @PostMapping("shops/{shopId}/freightmodels/{id}/weightItems")
    public Object defineWeightFreightModle(@PathVariable Long shopId,
                                           @PathVariable Long id,
                                           @RequestBody WeightFreightModelVo weightFreightModelVo,
                                           @LoginUser Long adminId,
                                           @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put shops/{shopId}/freightmodels/{id}/weightItems;shopId = " + shopId + " id=" + id + " adminId = " + adminId + "vo=" + weightFreightModelVo);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //登录
    @GetMapping("shops/{shopId}/freightmodels/{id}/weightItems")
    public Object getWeightFreightModel(@PathVariable Long shopId,
                                        @PathVariable Long id,
                                        @LoginUser Long adminId,
                                        @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get shops/{shopId}/freightmodels/{id}/weightItems; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //登录
    @PostMapping("shops/{shopId}/freightmodels/{id}/pieceItems")
    public Object definePieceFreightModle(@PathVariable Long shopId,
                                          @PathVariable Long id,
                                          @RequestBody PieceFreightModelVo pieceFreightModelVo,
                                          @LoginUser Long adminId,
                                          @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put shops/{shopId}/freightmodels/{id};shopId = " + shopId + " id=" + id + " adminId = " + adminId + "vo=" + pieceFreightModelVo);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //登录
    @GetMapping("shops/{shopId}/freightmodels/{id}/pieceItems")
    public Object getPieceFreightModel(@PathVariable Long shopId,
                                       @PathVariable Long id,
                                       @LoginUser Long adminId,
                                       @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("get shops/{shopId}/freightmodels/{id}/pieceItems; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //登录
    @PutMapping("shops/{shopId}/weightItems/{id}")
    public Object modifyWeightFreightModel(@PathVariable Long shopId,
                                           @PathVariable Long id,
                                           @RequestBody WeightFreightModelVo weightFreightModelVo,
                                           @LoginUser Long adminId,
                                           @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put shops/{shopId}/weightItems/{id}; shopId = " + shopId + " id=" + id + " adminId = " + adminId + "vo=" + weightFreightModelVo);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //登录
    @DeleteMapping("shops/{shopId}/weightItems/{id}")
    public Object deleteWeightFreightModel(@PathVariable Long shopId,
                                           @PathVariable Long id,
                                           @LoginUser Long adminId,
                                           @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("delete shops/{shopId}/weightItems/{id}; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //登录
    @PutMapping("shops/{shopId}/pieceItems/{id}")
    public Object modifyPieceFreightModel(@PathVariable Long shopId,
                                          @PathVariable Long id,
                                          @RequestBody PieceFreightModelVo vo,
                                          @LoginUser Long adminId,
                                          @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("put shops/{shopId}/freightmodels/{id};shopId = " + shopId + " id=" + id + " adminId = " + adminId + "vo=" + vo);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
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
    @Audit //登录
    @DeleteMapping("shops/{shopId}/pieceItems/{id}")
    public Object deletePieceFreightModel(@PathVariable Long shopId,
                                          @PathVariable Long id,
                                          @LoginUser Long adminId,
                                          @Depart Long adminShopId) {
        if (logger.isDebugEnabled()) {
            logger.debug("delete shops/{shopId}/pieceItems/{id}; shopId = " + shopId + " id = " + id + " adminId = " + adminId);
        }
        if (adminShopId != 0 && !adminShopId.equals(shopId)) {
            return ResponseUtils.make(new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE));
        }
        return ResponseUtils.make(freightService.deletePieceFreightModel(shopId, id));
    }
}
