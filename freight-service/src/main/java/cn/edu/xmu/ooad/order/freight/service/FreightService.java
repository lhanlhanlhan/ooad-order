package cn.edu.xmu.ooad.order.freight.service;

import cn.edu.xmu.ooad.order.centre.model.FreightCalcItem;
import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.centre.utils.Accessories;
import cn.edu.xmu.ooad.order.freight.dao.FreightDao;
import cn.edu.xmu.ooad.order.freight.model.bo.freight.FreightModel;
import cn.edu.xmu.ooad.order.freight.model.bo.freight.impl.PieceFreightModel;
import cn.edu.xmu.ooad.order.freight.model.bo.freight.impl.WeightFreightModel;
import cn.edu.xmu.ooad.order.freight.model.po.FreightModelPo;
import cn.edu.xmu.ooad.order.freight.model.po.PieceFreightModelPo;
import cn.edu.xmu.ooad.order.freight.model.po.WeightFreightModelPo;
import cn.edu.xmu.ooad.order.freight.model.vo.*;
import cn.edu.xmu.ooad.order.require.IShopService;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import cn.edu.xmu.ooad.util.ResponseCode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.dubbo.config.annotation.DubboReference;
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
    private static final Logger logger = LoggerFactory.getLogger(FreightService.class);

    @Autowired
    private FreightDao freightDao;

    @DubboReference(check = false)
    private IShopService iShopService;

    /**
     * 服務 f1: 計算運費
     *
     * @param regionId      地區id
     * @param orderItemList 商品 list
     * @return -1：失败；-2：运费模板 id 未定义；-3：包含禁寄物品
     */
    public long calcFreight(Long regionId, List<FreightCalcItem> orderItemList, Map<Long, SkuInfo> skuInfoMap) {
        // 1. 获取所有商品明细 (联系商品模块) 及所有关联之运费模板
        List<SkuInfo> skuInfoList = new ArrayList<>(orderItemList.size());
        List<FreightModel> freightModelList = new ArrayList<>(orderItemList.size());
        for (FreightCalcItem freightItem : orderItemList) {
            // 准备商品信息
            Long skuId = freightItem.getSkuId();
            SkuInfo skuInfo = skuInfoMap.get(skuId);
            // 准备运费模板信息
            Long modelId = skuInfo.getFreightId(); // 会不会未定义？未定义的话，这个字段应该为 0
            FreightModel model = freightDao.getFreightModel(modelId);
            if (model == null) {
                // 商品未定义运费模板，就获取商铺的默认模板
                model = freightDao.getShopDefaultFreightModel(skuInfo.getShopId());
                if (model == null) {
                    // 商铺的默认模板未定义，平台运费模板
                    model = freightDao.getShopDefaultFreightModel(0L);
                    if (model == null) {
                        // 严重错误
                        logger.error("平台未定义默认运费模板！请联系管理员。");
                        return -1;
                    }
                }
            }
            // 准备列表
            skuInfoList.add(skuInfo);
            freightModelList.add(model);
        }
        // 2. 用每个模板计算所有物品的最大运费
        Optional<Long> freight = freightModelList
                .stream()
                .map(model -> model.calcFreight(regionId, orderItemList, skuInfoList))
                .max(Long::compareTo);

        if (freight.isPresent()) {
            Long fr = freight.get();
            return fr == -1 ? -3 : fr;
        } else {
            // 包含禁寄物品
            return -3;
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
        LocalDateTime nowTime = Accessories.secondTime(LocalDateTime.now());

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
                return new APIReturnObject<>(ResponseCode.FREIGHTNAME_SAME);
            case 2:
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            default:
                return new APIReturnObject<>(HttpStatus.CREATED, ResponseCode.OK, new FreightModelVo(freightModelPo));
        }
    }

    /**
     * 服务 f3：获得店铺中商品的运费模板
     *
     * @param name     模板名称
     * @param page     页码
     * @param pageSize 页大小
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Modified by Han Li at 25/11/2020 16:58
     */
    public APIReturnObject<?> getShopGoodsFreightModel(Long shopId, String name, Integer page, Integer pageSize) {
        List<FreightModelSimpleVo> freightModelSampleVos;
        Map<String, Object> returnObj = new HashMap<>();
        PageHelper.startPage(page, pageSize);
        PageInfo<FreightModelPo> freightModelPoPageInfo = freightDao.getFreightModel(name, page, pageSize, shopId);
        if (freightModelPoPageInfo == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        freightModelSampleVos = freightModelPoPageInfo.getList().stream()
                .map(FreightModelSimpleVo::new)
                .collect(Collectors.toList());
        returnObj.put("page", freightModelPoPageInfo.getPageNum());
        returnObj.put("pageSize", freightModelPoPageInfo.getPageSize());
        returnObj.put("total", freightModelPoPageInfo.getTotal());
        returnObj.put("pages", freightModelPoPageInfo.getPages());
        returnObj.put("list", freightModelSampleVos);
        return new APIReturnObject<>(returnObj);
    }

    /**
     * 服务 f4：管理员克隆店铺中的运费模板
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Modified by Han Li at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> cloneFreightModel(Long shopId, Long id) {
        // 取出原本的運費模板
        FreightModel mainTable = freightDao.getFreightModel(id);
        if (mainTable == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (!shopId.equals(mainTable.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        LocalDateTime nowTime = Accessories.secondTime(LocalDateTime.now());
        // 先克隆主表
        FreightModelPo po = mainTable.toPo();
        // 直接改 Po，再存过一遍
        po.setId(null); // 将 id 改为空
        po.setGmtCreate(nowTime);
        po.setGmtModified(nowTime);
        po.setName(po.getName() + "-" + Accessories.genSerialNumber());
        int res = insertFreightModelPo(po);
        switch (res) {
            case 1:
                return new APIReturnObject<>(ResponseCode.FREIGHTNAME_SAME);
            case 2:
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 获取克隆後主模板 id
        Long mainId = po.getId();

        // 再克隆分表
        if (mainTable instanceof PieceFreightModel) {
            List<PieceFreightModelPo> pieceList = freightDao.getPieceFreightModels(id);
            if (pieceList == null) {
                // 分表未查到，回滚
                logger.error("克隆PieceFreightModelPo時數據庫錯誤");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            // 如果还没定义明细，就直接返回好了
            if (pieceList.size() == 0) {
                return new APIReturnObject<>(HttpStatus.CREATED, ResponseCode.OK, new FreightModelVo(po));
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
            List<WeightFreightModelPo> weightList = freightDao.getWeightFreightModels(id);
            if (weightList == null) {
                // 分表未查到，回滚
                logger.error("克隆WeightFreightModel時數據庫錯誤");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            // 如果还没定义明细，就直接返回好了
            if (weightList.size() == 0) {
                return new APIReturnObject<>(HttpStatus.CREATED, ResponseCode.OK, new FreightModelVo(po));
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
        return new APIReturnObject<>(HttpStatus.CREATED, ResponseCode.OK, new FreightModelVo(po));
    }

    /**
     * 服务 f5：获得运费模板概要
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> getFreightModelSimple(Long id, Long shopId) {
        FreightModel model = freightDao.getFreightModel(id);
        if (model == null) {
            // 未能找到
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 检查是不是自己的
        if (model.getShopId() != null && !shopId.equals(model.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 构造 Vo 返回
        return new APIReturnObject<>(new FreightModelVo(model));
    }

    /**
     * 服务 f6：管理员修改店铺的运费模板
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Modified by Han Li at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> modifyShopFreightModel(Long shopId, Long id, FreightModelEditVo freightModelEditVo) {
        // 看看是不是屬於本店
        FreightModel model = freightDao.getFreightModel(id);
        if (model == null) {
            // 未能找到
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (model.getShopId() == null || !shopId.equals(model.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 看看是否要改名
        String name = freightModelEditVo.getName();
        if (name != null && name.equals("")) {
            name = null;
        }
        // 来自 CaiXinLu 的用例：和原本的运费模板重名，也算重名 [19/12/2020]
        if (name != null && name.equals(model.getName())) {
            logger.info("新的运费模板名和之前的相同，也算重名");
            return new APIReturnObject<>(ResponseCode.FREIGHTNAME_SAME);
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
                return new APIReturnObject<>(ResponseCode.FREIGHTNAME_SAME);
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
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> deleteShopFreightModel(Long shopId, Long id) {
        // 用老方法获取原来信息，因為要看看是屬於那一種運費模板
        FreightModel model = freightDao.getFreightModel(id);
        if (model == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        Long origShopId = model.getShopId();
        Byte type = model.getType();
        // 判断该商店是否拥有
        if (!origShopId.equals(shopId)) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
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
            // 同步删除商品模块那边的 [省省吧 我lui了]
            int delStatus = 0;
//            try {
//                delStatus = iShopService.deleteFreightModel(id, shopId);
//            } catch (Exception e) {
//                logger.error("商品模块无法联系 " + e.getMessage());
//                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
//            }
            if (0 != delStatus) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("商品模块那边删除运费模板失败！modelId=" + id);
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
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
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Modified by Han Li at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> defineDefaultFreightModel(Long shopId, Long id) {
        // 看看是不是屬於本店
        FreightModel model = freightDao.getFreightModel(id);
        if (model == null) {
            // 未能找到
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (model.getShopId() == null || !shopId.equals(model.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 取消原先的 default model
        freightDao.cancelDefaultModel(shopId);

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
            return new APIReturnObject<>(HttpStatus.CREATED, ResponseCode.OK);
        } else {
            logger.error("定义默认运费模板失败！id=" + id);
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 服务 f9：管理员定义重量模板明细
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> createWeightFreightModel(Long shopId, Long id, WeightFreightModelVo weightFreightModelVo) {
        // 看看是不是屬於本店
        FreightModel model = freightDao.getFreightModel(id);
        if (model == null) {
            // 未能找到
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (model.getShopId() == null || !shopId.equals(model.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // Ming Qiu : 不去查看是否是重量模板了~ [21/12/2020: 沒有衝突就可以]
//        if (model.getType() == null || model.getType() != 0) {
//            return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID, "该模板不是重量模板！不能定义明细！");
//        }

        WeightFreightModelPo weightFreightModelPo = new WeightFreightModelPo();

        LocalDateTime nowTime = Accessories.secondTime(LocalDateTime.now());
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
                return new APIReturnObject<>(ResponseCode.REGION_SAME);
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
            return new APIReturnObject<>(HttpStatus.CREATED, ResponseCode.OK, new WeightFreightModelVo(weightFreightModelPo));
        } else {
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 服务 f10：管理员查询重量模板明细
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<List<WeightFreightModelVo>> getWeightFreightModel(Long shopId, Long id) {
        // 看看是不是屬於本店
        FreightModel model = freightDao.getFreightModel(id);
        if (model == null) {
            // 未能找到
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (model.getShopId() == null || !shopId.equals(model.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 是不是重量模板
        if (model.getType() == null || model.getType() != 0) {
            return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID, "该模板不是重量模板！");
        }

        List<WeightFreightModelPo> returnObject = freightDao.getWeightFreightModels(id);
        if (returnObject == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        // 全部给我变成 vo
        List<WeightFreightModelVo> voList = returnObject
                .stream()
                .map(WeightFreightModelVo::new)
                .collect(Collectors.toList());

        return new APIReturnObject<>(voList);
    }

    /**
     * 服务 f11：管理员定义件数模板明细
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> createPieceFreightModel(Long shopId, Long id, PieceFreightModelVo pieceFreightModelVo) {
        // 看看是不是屬於本店
        FreightModel model = freightDao.getFreightModel(id);
        if (model == null) {
            // 未能找到
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (model.getShopId() == null || !shopId.equals(model.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // Ming Qiu : 不去查看是否是件數模板了~ [21/12/2020: 沒有衝突就可以]
//        if (model.getType() == null || model.getType() != 1) {
//            return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID, "该模板不是件数模板！不能定义明细！");
//        }

        PieceFreightModelPo pieceFreightModelPo = new PieceFreightModelPo();

        LocalDateTime nowTime = Accessories.secondTime(LocalDateTime.now());
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
                return new APIReturnObject<>(ResponseCode.REGION_SAME);
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
            return new APIReturnObject<>(HttpStatus.CREATED, ResponseCode.OK, new PieceFreightModelVo(pieceFreightModelPo));
        } else {
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 服务 f12：管理员查询件数模板明细
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<List<PieceFreightModelVo>> getPieceFreightModel(Long shopId, Long id) {
        // 看看是不是屬於本店
        FreightModel model = freightDao.getFreightModel(id);
        if (model == null) {
            // 未能找到
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (model.getShopId() == null || !shopId.equals(model.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 是不是件数模板
        if (model.getType() == null || model.getType() != 1) {
            return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID, "该模板不是件数模板！");
        }

        List<PieceFreightModelPo> returnObject = freightDao.getPieceFreightModels(id);
        if (returnObject == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 全部给我变成 vo
        List<PieceFreightModelVo> voList = returnObject
                .stream()
                .map(PieceFreightModelVo::new)
                .collect(Collectors.toList());

        return new APIReturnObject<>(voList);
    }

    /**
     * 服务 f13：管理员修改重量模板明细
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> modifyWeightFreightModel(Long shopId, Long detailId, WeightFreightModelVo vo) {
        // 鑑定
        int belongs = weightModelItemBelongs(shopId, detailId);
        switch (belongs) {
            case 1:
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
            case 2:
                return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
            case 3:
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID, "这模板并非重量模板");
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
                return new APIReturnObject<>(ResponseCode.REGION_SAME);
            case 2:
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            default:
                return new APIReturnObject<>();
        }
    }

    /**
     * 服务 f14：店家或管理员删掉重量运费模板明细
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> deleteWeightFreightModel(Long shopId, Long detailId) {
        // 鑑定
        int belongs = weightModelItemBelongs(shopId, detailId);
        switch (belongs) {
            case 1:
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
            case 2:
                return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
            case 3:
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID, "这模板并非重量模板");
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
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> modifyPieceFreightModel(Long shopId, Long detailId, PieceFreightModelVo vo) {
        // 鑑定
        int belongs = pieceModelItemBelongs(shopId, detailId);
        switch (belongs) {
            case 1:
                logger.debug("不存在该pieceItem: " + detailId);
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
            case 2:
                logger.debug("该pieceItem不属于商铺: " + detailId);
                return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
            case 3:
                logger.debug("这模板并非件数模板: " + detailId);
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID, "这模板并非件数模板");
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
                return new APIReturnObject<>(ResponseCode.REGION_SAME);
            case 2:
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            default:
                return new APIReturnObject<>();
        }
    }

    /**
     * 服务 f16：店家或管理员删掉件数运费模板明细
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> deletePieceFreightModel(Long shopId, Long detailId) {
        // 鑑定
        int belongs = pieceModelItemBelongs(shopId, detailId);
        switch (belongs) {
            case 1:
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
            case 2:
                return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
            case 3:
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID, "这模板并非件数模板");
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
     * **内部方法** 校验重量模板明细是否属于店铺，是否存在
     *
     * @param shopId   店鋪id
     * @param detailId 明細id
     * @return 0：属于，1：不存在；2：存在但不属于；3：存在但不是重量模板
     */
    private int weightModelItemBelongs(Long shopId, Long detailId) {
        // 找小表
        WeightFreightModelPo po = freightDao.getWeightFreightModelById(detailId);
        if (po == null) {
            return 1;
        }
        // 得到大表 id，找大表
        Long fmId = po.getFreightModelId();

        // 看看是不是屬於本店及是不是重量模板
        FreightModel fmPo = freightDao.getFreightModel(fmId);
        if (fmPo == null) { // 不存在
            return 1;
        }
        if (!shopId.equals(fmPo.getShopId())) {
            return 2;
        }
        if (!(fmPo instanceof WeightFreightModel)) {
            return 3;
        }
        return 0;
    }

    /**
     * **内部方法** 校验件数模板明细是否属于店铺，是否存在
     *
     * @param shopId   店鋪id
     * @param detailId 明細id
     * @return 0：属于，1：不存在；2：存在但不属于；3：存在但不是件数模板
     */
    private int pieceModelItemBelongs(Long shopId, Long detailId) {
        // 找小表
        PieceFreightModelPo po = freightDao.getPieceFreightModelById(detailId);
        if (po == null) {
            return 1;
        }
        // 得到大表 id，找大表
        Long fmId = po.getFreightModelId();

        // 看看是不是屬於本店及是不是重量模板
        FreightModel fmPo = freightDao.getFreightModel(fmId);
        if (fmPo == null) { // 不存在
            return 1;
        }
        if (!shopId.equals(fmPo.getShopId())) {
            return 2;
        }
        if (!(fmPo instanceof PieceFreightModel)) {
            return 3;
        }
        return 0;
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
            po.setGmtModified(Accessories.secondTime(LocalDateTime.now()));
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
