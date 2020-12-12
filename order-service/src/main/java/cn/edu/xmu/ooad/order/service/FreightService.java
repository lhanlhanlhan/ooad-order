package cn.edu.xmu.ooad.order.service;

import cn.edu.xmu.ooad.order.connector.service.ShopService;
import cn.edu.xmu.ooad.order.dao.FreightDao;
import cn.edu.xmu.ooad.order.model.bo.FreightModel;
import cn.edu.xmu.ooad.order.model.po.FreightModelPo;
import cn.edu.xmu.ooad.order.model.po.PieceFreightModelPo;
import cn.edu.xmu.ooad.order.model.po.WeightFreightModelPo;
import cn.edu.xmu.ooad.order.model.vo.*;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import cn.edu.xmu.ooad.order.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.utils.Accessories;
import cn.edu.xmu.ooad.order.utils.ResponseCode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 运费服务
 *
 * @author Chen Kechun
 * Created at 1/12/2020 20:57
 * Modified by Chen Kechun at 1/12/2020 20:57
 */
@Service
public class FreightService {
    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private FreightDao freightDao;

    @Autowired
    private ShopService shopService;

    /**
     * 服務 f1: 計算運費
     *
     * @param regionId      地區id
     * @param orderItemList 商品 list
     */
    public APIReturnObject<?> calcFreight(Long regionId, List<FreightOrderItemVo> orderItemList) {
        // 1. 获取所有商品明细 (联系商品模块) 及所有关联之运费模板
        List<SkuInfo> skuInfoList = new ArrayList<>(orderItemList.size());
        List<FreightModel> freightModelList = new ArrayList<>(orderItemList.size());
        for (FreightOrderItemVo freightItem : orderItemList) {
            // 准备商品信息 (希望商品模块帮我们缓存了 ～)
            Long skuId = freightItem.getSkuId();
            SkuInfo skuInfo = shopService.getSkuInfo(skuId);
            if (skuInfo == null) {
                // 商品、订单模块数据库不一致
                logger.error("计算运费、准备商品资料时，检测到商品、订单模块数据库不一致! skuId=" + skuId);
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
            // 准备运费模板信息
            Long modelId = skuInfo.getId(); // 会不会未定义？未定义的话，这个字段应该为 0
            FreightModel model = freightDao.getFreightModel(modelId);
            if (model == null) {
                // 商品、订单模块数据库不一致
                logger.error("计算运费、抽取运费模板时，检测到运费模板未定义! skuId=" + skuId);
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST, "賣家沒有定義商品的運費模板！");
            }

            // 准备列表
            skuInfoList.add(skuInfo);
            freightModelList.add(model);
        }
        // 2. 用每个模板计算所有物品的最大运费
        AtomicReference<Boolean> calcSucceeded = new AtomicReference<>(true);
        Optional<Long> freight = freightModelList.parallelStream().map(model -> {
            long subFreight = model.calcFreight(regionId, orderItemList, skuInfoList);
            if (subFreight == -1) {
                // 包含禁寄物品
                calcSucceeded.set(false);
            }
            return subFreight;
        }).max(Long::compareTo);

        if (calcSucceeded.get() && freight.isPresent()) {
            return new APIReturnObject<>(freight);
        } else {
            // 包含禁寄物品
            return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FREIGHT_REGION_FORBIDDEN);
        }
    }

    /**
     * 服务 f2：管理员定义商铺运费模板
     *
     * @param shopId            店铺 id
     * @param freightModelNewVo 运费模板资料
     * @return APIReturnObject
     */
    @Transactional
    public APIReturnObject<?> createShopGoodsFreightModel(Long shopId,
                                                          FreightModelNewVo freightModelNewVo) {
        //创建运费模板
        LocalDateTime nowTime = LocalDateTime.now();

        FreightModelPo freightModelPo = new FreightModelPo();
        freightModelPo.setShopId(shopId);
        freightModelPo.setName(freightModelNewVo.getName());
        freightModelPo.setType(freightModelNewVo.getType());
        freightModelPo.setUnit(freightModelNewVo.getUnit());
        freightModelPo.setDefaultModel((byte) 0); // 非默认运费模板
        freightModelPo.setGmtCreate(nowTime);
        freightModelPo.setGmtModified(nowTime);

        int res = insertFreightModelPo(freightModelPo);
        switch (res) {
            case 1:
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FREIGHT_MODEL_NAME_SAME);
            case 2:
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            default:
                return new APIReturnObject<>();
        }
    }

    /**
     * 服务 f3：获得店铺中商品的运费模板
     *
     * @param name     模板名称
     * @param page     页码
     * @param pageSize 页大小
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Modified by Han Li at 25/11/2020 16:58
     */
    public APIReturnObject<?> getShopGoodsFreightModel(Long shopId, String name, Integer page, Integer pageSize) {
        List<FreightModelSimpleVo> freightModelSampleVos;
        Map<String, Object> returnObj = new HashMap<>();
        if (page != null && pageSize != null) {
            PageHelper.startPage(page, pageSize);
            APIReturnObject<PageInfo<FreightModelPo>> returnObject = freightDao.getFreightModel(name, page, pageSize, shopId);
            if (returnObject.getCode() != ResponseCode.OK) {
                return returnObject;
            }
            PageInfo<FreightModelPo> freightModelPoPageInfo = returnObject.getData();
            freightModelSampleVos = freightModelPoPageInfo.getList().stream()
                    .map(FreightModelSimpleVo::new)
                    .collect(Collectors.toList());
            returnObj.put("page", freightModelPoPageInfo.getPageNum());
            returnObj.put("pageSize", freightModelPoPageInfo.getPageSize());
            returnObj.put("total", freightModelPoPageInfo.getTotal());
            returnObj.put("pages", freightModelPoPageInfo.getPages());
        } else {
            APIReturnObject<List<FreightModelPo>> returnObject = freightDao.getFreightModel(null, name, shopId);
            if (returnObject.getCode() != ResponseCode.OK) {
                return returnObject;
            }
            List<FreightModelPo> freightModelPoPageInfo = returnObject.getData();
            freightModelSampleVos = freightModelPoPageInfo.stream()
                    .map(FreightModelSimpleVo::new)
                    .collect(Collectors.toList());
            returnObj.put("page", 1);
            returnObj.put("pageSize", freightModelSampleVos.size());
            returnObj.put("total", freightModelSampleVos.size());
            returnObj.put("pages", 1);
        }

        returnObj.put("list", freightModelSampleVos);
        return new APIReturnObject<>(returnObj);
    }

    /**
     * 服务 f4：管理员克隆店铺中的运费模板
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Modified by Han Li at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> cloneFreightModel(Long shopId, Long id) {
        // 取出原本的運費模板
        APIReturnObject<FreightModelPo> mainTable = freightDao.getShopFreightModel(id, shopId);
        if (mainTable.getCode() != ResponseCode.OK) {
            // 找不到或資料庫錯誤
            return mainTable;
        }

        LocalDateTime nowTime = LocalDateTime.now();
        // 先克隆主表
        FreightModelPo po = mainTable.getData();
        // 直接改 Po，再存过一遍
        po.setId(null); // 将 id 改为空
        po.setGmtCreate(nowTime);
        po.setGmtModified(nowTime);
        po.setName(po.getName() + "-" + Accessories.genSerialNumber());
        int res = insertFreightModelPo(po);
        switch (res) {
            case 1:
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FREIGHT_MODEL_NAME_SAME);
            case 2:
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 获取克隆後主模板 id
        Long mainId = po.getId();

        // 再克隆分表
        Byte type = po.getType();
        if (type == 0) {
            APIReturnObject<List<PieceFreightModelPo>> pieceTable = freightDao.getPieceFreightModels(id, null);
            if (pieceTable.getCode() != ResponseCode.OK) {
                // 分表未查到，回滚
                logger.error("克隆PieceFreightModelPo時數據庫錯誤");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return pieceTable;
            }
            // 如果还没定义明细，就直接返回好了
            List<PieceFreightModelPo> pieceList = pieceTable.getData();
            if (pieceList.size() == 0) {
                return mainTable;
            }
            // 克隆所有明细
            for (PieceFreightModelPo piecePo : pieceList) {
                piecePo.setId(null);
                piecePo.setFreightModelId(mainId);
                piecePo.setGmtCreate(nowTime);
                if (!insertPieceFreightModelPo(piecePo)) {
                    // 無法克隆明細
                    logger.error("克隆訂單明細時出錯，插入數據庫失敗");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
                }
            }
        } else {
            APIReturnObject<List<WeightFreightModelPo>> weightTable = freightDao.getWeightFreightModels(id, null);
            if (weightTable.getCode() != ResponseCode.OK) {
                // 分表未查到，回滚
                logger.error("克隆WeightFreightModel時數據庫錯誤");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, weightTable.getCode(), weightTable.getErrMsg());
            }
            // 如果还没定义明细，就直接返回好了
            List<WeightFreightModelPo> weightList = weightTable.getData();
            if (weightList.size() == 0) {
                return mainTable;
            }
            // 克隆所有明细
            for (WeightFreightModelPo weightPo : weightList) {
                weightPo.setId(null);
                weightPo.setFreightModelId(mainId);
                weightPo.setGmtCreate(nowTime);
                if (!insertWeightFreightModelPo(weightPo)) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
                }
            }
        }

        // 返回改动过的主表
        return mainTable;
    }

    /**
     * 服务 f5：获得运费模板概要
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> getFreightModelSimple(Long id, Long shopId) {
        APIReturnObject<FreightModelPo> poObj = freightDao.getShopFreightModel(id, shopId);
        if (poObj.getCode() != ResponseCode.OK) {
            // 未能找到
            return poObj;
        }
        // 构造 Vo 返回
        return new APIReturnObject<>(new FreightModelVo(poObj.getData()));
    }

    /**
     * 服务 f6：管理员修改店铺的运费模板
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Modified by Han Li at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> modifyShopFreightModel(Long shopId, Long id, FreightModelEditVo freightModelEditVo) {
        // 看看是不是屬於本店
        long belongs = freightDao.countFreightModel(id, shopId, null);
        if (belongs == 0) { // 不存在
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        } else if (belongs == -1) { // 數據庫
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        // 看看是否要改名
        String name = freightModelEditVo.getName();
        if (name != null && name.equals("")) {
            name = null;
        }

        // 创建更新体
        FreightModelPo po = new FreightModelPo();
        po.setId(id);
        po.setName(name);
        po.setUnit(freightModelEditVo.getUnit());

        // 写入数据库
        int response;
        try {
            response = freightDao.updateFreightModel(po);
        } catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("Duplicate entry")) {
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FREIGHT_MODEL_NAME_SAME);
            } else {
                logger.error(e.getMessage());
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 返回
        if (response > 0) {
            return new APIReturnObject<>();
        } else {
            logger.error("更新失败！freightModelId=" + id);
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 服务 f7：管理员删除店铺的运费模板
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> deleteShopFreightModel(Long shopId, Long id) {
        // 用老方法获取原来信息，因為要看看是屬於那一種運費模板
        APIReturnObject<FreightModelPo> returnObject = freightDao.getShopFreightModel(id, shopId);
        if (returnObject.getCode() != ResponseCode.OK) {
            return returnObject;
        }
        FreightModelPo po = returnObject.getData();
        Long origShopId = po.getShopId();
        Byte type = po.getType();
        // 判断该商店是否拥有
        if (!origShopId.equals(shopId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }

        // 将删除写入数据库
        int response;
        try {
            response = freightDao.deleteFreightModel(id, type);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 返回
        if (response >= 0) {
            return new APIReturnObject<>();
        } else {
            logger.error("删除运费模板失败！modelId=" + id);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 服务 f8：店家或管理员为商铺定义默认运费模板
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Modified by Han Li at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> defineDefaultFreightModel(Long shopId, Long id) {
        // 看看是不是屬於本店
        long belongs = freightDao.countFreightModel(id, shopId, null);
        if (belongs == 0) { // 不存在
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        } else if (belongs == -1) { // 數據庫
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 创建更新体
        FreightModelPo po = new FreightModelPo();
        po.setId(id);
        po.setDefaultModel((byte) 1);

        // 写入数据库
        int response;
        try {
            response = freightDao.updateFreightModel(po);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 返回
        if (response > 0) {
            return new APIReturnObject<>();
        } else {
            logger.error("定义默认运费模板失败！id=" + id);
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 服务 f9：管理员定义重量模板明细
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> createWeightFreightModel(Long shopId, Long id, WeightFreightModelVo weightFreightModelVo) {
        // 看看是不是屬於本店、是不是重量模板
        long belongs = freightDao.countFreightModel(id, shopId, (byte) 0);
        if (belongs == 0) { // 不存在
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        } else if (belongs == -1) { // 數據庫
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        WeightFreightModelPo weightFreightModelPo = new WeightFreightModelPo();

        LocalDateTime nowTime = LocalDateTime.now();
        weightFreightModelPo.setAbovePrice(weightFreightModelVo.getAbovePrice());
        weightFreightModelPo.setFiftyPrice(weightFreightModelVo.getFiftyPrice());
        weightFreightModelPo.setFirstWeight(weightFreightModelVo.getFirstWeight());
        weightFreightModelPo.setFirstWeightFreight(weightFreightModelVo.getFirstWeightFreight());
        weightFreightModelPo.setRegionId(weightFreightModelVo.getRegionId());
        weightFreightModelPo.setFreightModelId(id);
        weightFreightModelPo.setHundredPrice(weightFreightModelVo.getHundredPrice());
        weightFreightModelPo.setTenPrice(weightFreightModelVo.getTenPrice());
        weightFreightModelPo.setTrihunPrice(weightFreightModelVo.getTrihunPrice());
        weightFreightModelPo.setGmtCreate(nowTime);
        weightFreightModelPo.setGmtModified(nowTime);

        // 写入数据库
        int response;
        try {
            response = freightDao.addWeightFreightModel(weightFreightModelPo);
        } catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("Duplicate entry")) {
                return new APIReturnObject<>(HttpStatus.CONFLICT, ResponseCode.REGION_SAME);
            } else {
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

//        if (!insertWeightFreightModelPo(weightFreightModelPo)) {
//            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
//        }
        // 返回
        if (response > 0) {
            return new APIReturnObject<>(new WeightFreightModelVo(weightFreightModelPo));
        } else {
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 服务 f10：管理员查询重量模板明细
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<List<WeightFreightModelVo>> getWeightFreightModel(Long shopId, Long id) {
        // 看看是不是屬於本店、是不是重量模板
        long belongs = freightDao.countFreightModel(id, shopId, (byte) 0);
        if (belongs == 0) { // 不存在
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        } else if (belongs == -1) { // 數據庫
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        APIReturnObject<List<WeightFreightModelPo>> returnObject = freightDao.getWeightFreightModels(id, null);
        if (returnObject.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }

        // 全部给我变成 vo
        List<WeightFreightModelVo> voList = returnObject.getData().stream()
                .map(WeightFreightModelVo::new)
                .collect(Collectors.toList());

        return new APIReturnObject<>(voList);
    }

    /**
     * 服务 f11：管理员定义件数模板明细
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> createPieceFreightModel(Long shopId, Long id, PieceFreightModelVo pieceFreightModelVo) {
        // 看看是不是屬於本店、是不是件數模板
        long belongs = freightDao.countFreightModel(id, shopId, (byte) 1);
        if (belongs == 0) { // 不存在
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        } else if (belongs == -1) { // 數據庫
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        PieceFreightModelPo pieceFreightModelPo = new PieceFreightModelPo();

        LocalDateTime nowTime = LocalDateTime.now();
        pieceFreightModelPo.setAdditionalItems(pieceFreightModelVo.getAdditionalItems());
        pieceFreightModelPo.setAdditionalItemsPrice(pieceFreightModelVo.getAdditionalItemsPrice());
        pieceFreightModelPo.setFirstItems(pieceFreightModelVo.getAdditionalItems());
        pieceFreightModelPo.setFirstItemsPrice(pieceFreightModelVo.getAdditionalItemsPrice());
        pieceFreightModelPo.setFreightModelId(id);
        pieceFreightModelPo.setRegionId(pieceFreightModelVo.getRegionId());
        pieceFreightModelPo.setGmtCreate(nowTime);
        pieceFreightModelPo.setGmtModified(nowTime);

        // 写入数据库
        int response;
        try {
            response = freightDao.addPieceFreightModel(pieceFreightModelPo);
        } catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("Duplicate entry")) {
                return new APIReturnObject<>(HttpStatus.CONFLICT, ResponseCode.REGION_SAME);
            } else {
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

//        if (!insertWeightFreightModelPo(weightFreightModelPo)) {
//            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
//        }
        // 返回
        if (response > 0) {
            return new APIReturnObject<>(new PieceFreightModelVo(pieceFreightModelPo));
        } else {
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 服务 f12：管理员查询件数模板明细
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<List<PieceFreightModelVo>> getPieceFreightModel(Long shopId, Long id) {
        // 看看是不是屬於本店、是不是件數模板
        long belongs = freightDao.countFreightModel(id, shopId, (byte) 1);
        if (belongs == 0) { // 不存在
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        } else if (belongs == -1) { // 數據庫
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        APIReturnObject<List<PieceFreightModelPo>> returnObject = freightDao.getPieceFreightModels(id, null);
        if (returnObject.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        // 全部给我变成 vo
        List<PieceFreightModelVo> voList = returnObject.getData().stream()
                .map(PieceFreightModelVo::new)
                .collect(Collectors.toList());

        return new APIReturnObject<>(voList);
    }

    /**
     * 服务 f13：管理员修改重量模板明细
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> modifyWeightFreightModel(Long shopId, Long detailId, WeightFreightModelVo vo) {
        // 鑑定
        if (weightModelItemNotBelongs(shopId, detailId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }

        WeightFreightModelPo po = new WeightFreightModelPo();
        po.setId(detailId);
        po.setTrihunPrice(vo.getTrihunPrice());
        po.setTenPrice(vo.getTenPrice());
        po.setHundredPrice(vo.getHundredPrice());
        po.setRegionId(vo.getRegionId());
        po.setFirstWeightFreight(vo.getFirstWeightFreight());
        po.setFirstWeight(vo.getFirstWeight());
        po.setFiftyPrice(vo.getFiftyPrice());

        int res = updateWeightFreightModelPo(po);
        switch (res) {
            case 1:
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.REGION_SAME);
            case 2:
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            default:
                return new APIReturnObject<>();
        }
    }

    /**
     * 服务 f14：店家或管理员删掉重量运费模板明细
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> deleteWeightFreightModel(Long shopId, Long detailId) {
        if (weightModelItemNotBelongs(shopId, detailId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }

        try {
            int response = freightDao.deleteWeightFreightModelRule(detailId);
            if (response <= 0) {
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>();
    }

    /**
     * 服务 f15：管理员修改件数模板明细
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> modifyPieceFreightModel(Long shopId, Long detailId, PieceFreightModelVo vo) {
        if (pieceModelItemNotBelongs(shopId, detailId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }

        PieceFreightModelPo po = new PieceFreightModelPo();
        po.setId(detailId);
        po.setRegionId(vo.getRegionId());
        po.setFirstItems(vo.getFirstItem());
        po.setFirstItemsPrice(vo.getFirstItemPrice());
        po.setAdditionalItems(vo.getAdditionalItems());
        po.setAdditionalItemsPrice(vo.getAdditionalItemsPrice());

        int res = updatePieceFreightModelPo(po);
        switch (res) {
            case 1:
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.REGION_SAME);
            case 2:
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            default:
                return new APIReturnObject<>();
        }
    }

    /**
     * 服务 f16：店家或管理员删掉件数运费模板明细
     *
     * @return cn.edu.xmu.ooad.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> deletePieceFreightModel(Long shopId, Long detailId) {
        if (pieceModelItemNotBelongs(shopId, detailId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }

        try {
            int response = freightDao.deletePieceFreightModelRule(detailId);
            if (response <= 0) {
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>();
    }


    /*
    內部方法
     */

    /**
     * **内部方法** 校验重量模板明细是否属于店铺
     *
     * @param shopId   店鋪id
     * @param detailId 明細id
     */
    private boolean weightModelItemNotBelongs(Long shopId, Long detailId) {
        // 找小表
        APIReturnObject<List<WeightFreightModelPo>> judge = freightDao.getWeightFreightModels(null, detailId);
        if (judge.getCode() != ResponseCode.OK) {
            return true;
        }
        List<WeightFreightModelPo> judgePo = judge.getData();
        // 没找到
        if (judgePo.size() != 1) {
            return true;
        }
        // 得到大表 id，找大表
        Long id = judgePo.get(0).getFreightModelId();

        // 看看是不是屬於本店、是不是重量模板
        long belongs = freightDao.countFreightModel(id, shopId, (byte) 0);
        // 數據庫
        if (belongs == 0) { // 不存在
            return true;
        } else return belongs == -1;
    }

    /**
     * **内部方法** 校验件數模板明细是否属于店铺
     *
     * @param shopId   店鋪id
     * @param detailId 明細id
     */
    private boolean pieceModelItemNotBelongs(Long shopId, Long detailId) {
        // 找小表
        APIReturnObject<List<PieceFreightModelPo>> judge = freightDao.getPieceFreightModels(null, detailId);
        if (judge.getCode() != ResponseCode.OK) {
            return true;
        }
        List<PieceFreightModelPo> judgePo = judge.getData();
        // 没找到
        if (judgePo.size() != 1) {
            return true;
        }
        // 得到大表 id，找大表
        Long id = judgePo.get(0).getFreightModelId();

        // 看看是不是屬於本店、是不是件數模板
        long belongs = freightDao.countFreightModel(id, shopId, (byte) 1);
        // 數據庫
        if (belongs == 0) { // 不存在
            return true;
        } else return belongs == -1;
    }

    /**
     * **内部方法**：将 weightFreightModelPo 从数据库中更新
     * <p>
     * 返回：0 成功 1 重复 2 其他错误
     */
    private int updateWeightFreightModelPo(WeightFreightModelPo po) {
        try {
            int response = freightDao.updateWeightFreightModel(po);
            return response > 0 ? 0 : 2;
        } catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("Duplicate entry")) {
                return 1;
            } else {
                return 2;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 2;
        }
    }

    /**
     * **内部方法**：将 pieceFreightModelPo 从数据库中更新
     * <p>
     * 返回：0 成功 1 重复 2 其他错误
     */
    private int updatePieceFreightModelPo(PieceFreightModelPo po) {
        try {
            po.setGmtModified(LocalDateTime.now());
            int response = freightDao.updatePieceFreightModel(po);
            return response > 0 ? 0 : 2;
        } catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("Duplicate entry")) {
                return 1;
            } else {
                return 2;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 2;
        }
    }

    /**
     * **内部方法**：将 freightModelPo 插入数据库中
     * <p>
     * 返回：0 成功 1 重复 2 其他错误
     */
    private int insertFreightModelPo(FreightModelPo po) {
        try {
            int response = freightDao.addFreightModel(po);
            return response > 0 ? 0 : 2;
        } catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("Duplicate entry")) {
                return 1;
            } else {
                return 2;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 2;
        }
    }

    /**
     * **内部方法**：将 weightFreightModelPo 插入数据库中
     */
    private boolean insertWeightFreightModelPo(WeightFreightModelPo po) {
        try {
            int response = freightDao.addWeightFreightModel(po);
            return response > 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * **内部方法**：将 weightFreightModelPo 插入数据库中
     */
    private boolean insertPieceFreightModelPo(PieceFreightModelPo po) {
        try {
            int response = freightDao.addPieceFreightModel(po);
            return response > 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
