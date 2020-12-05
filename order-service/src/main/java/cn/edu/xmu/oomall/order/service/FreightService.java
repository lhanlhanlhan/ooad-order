package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.connector.service.CustomerService;
import cn.edu.xmu.oomall.order.connector.service.ShopService;
import cn.edu.xmu.oomall.order.dao.FreightDao;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.model.bo.FreightModel;
import cn.edu.xmu.oomall.order.model.po.FreightModelPo;
import cn.edu.xmu.oomall.order.model.po.PieceFreightModelPo;
import cn.edu.xmu.oomall.order.model.po.WeightFreightModelPo;
import cn.edu.xmu.oomall.order.model.vo.*;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import cn.edu.xmu.oomall.order.utils.Accessories;
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
    private CustomerService customerService;

    @Autowired
    private ShopService shopService;

    public APIReturnObject<?> calcFreight(Long regionId, List<OrderItemVo> orderItemList) {
        // 1. 获取所有商品明细 (联系商品模块) 及所有关联之运费模板
        List<Map<String, Object>> skuInfoList = new ArrayList<>(orderItemList.size());
        List<FreightModel> freightModelList = new ArrayList<>(orderItemList.size());
        for (OrderItemVo freightItem : orderItemList) {
            // 准备商品信息
            Long skuId = freightItem.getSkuId();
            Map<String, Object> skuInfo = shopService.getSkuInfo(skuId);
            if (skuInfo == null) {
                // 商品、订单模块数据库不一致
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
            // 准备运费模板信息
            Long modelId = (Long) skuInfo.get("freightId"); // 会不会未定义？未定义的话，这个字段应该为 0
            FreightModelPo modelPo = freightDao.getFreightModel(modelId);
            if (modelPo == null) {
                // 商品、订单模块数据库不一致
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
            FreightModel model = new FreightModel(modelPo);

            // 准备列表
            skuInfoList.add(skuInfo);
            freightModelList.add(model);
        }
        // 2. 用每个模板计算所有物品的最大运费
        long freight = 0;
        for (FreightModel model : freightModelList) {
            long subFreight = model.calcFreight(regionId, orderItemList, skuInfoList);
            if (subFreight == -1) {
                // 包含禁寄物品
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FREIGHT_REGION_FORBIDDEN);
            }
            if (subFreight > freight) {
                // 更新最大运费
                freight = subFreight;
            }
        }
        return new APIReturnObject<>(freight);
    }

    /**
     * 服务 f2：管理员定义商铺运费模板
     *
     * @param shopId             店铺 id
     * @param freightModelInfoVo 运费模板资料
     * @return APIReturnObject
     */
    public APIReturnObject<?> createShopGoodsFreightModel(Long shopId,
                                                          FreightModelInfoVo freightModelInfoVo) {
        //创建运费模板
        LocalDateTime nowTime = LocalDateTime.now();

        FreightModelPo freightModelPo = new FreightModelPo();
        freightModelPo.setShopId(shopId);
        freightModelPo.setName(freightModelInfoVo.getName());
        freightModelPo.setType(freightModelInfoVo.getType());
        freightModelPo.setUnit(freightModelInfoVo.getUnit());
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
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
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
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
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
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
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
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Modified by Han Li at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> cloneFreightModel(Long shopId, Long id) {
        APIReturnObject<FreightModelPo> mainTable = freightDao.getShopFreightModel(id, shopId);
        if (mainTable.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, mainTable.getCode(), mainTable.getErrMsg());
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

        // 获取主模板 id
        Long mainId = po.getId();

        // 再克隆分表
        Byte type = po.getType();
        if (type == 0) {
            APIReturnObject<List<PieceFreightModelPo>> pieceTable = freightDao.getPieceFreightModel(id, null);
            if (pieceTable.getCode() != ResponseCode.OK) {
                // 分表未查到，回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, pieceTable.getCode(), pieceTable.getErrMsg());
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
                piecePo.setGmtModified(nowTime);
                if (!insertPieceFreightModelPo(piecePo)) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
                }
            }
        } else {
            APIReturnObject<List<WeightFreightModelPo>> weightTable = freightDao.getWeightFreightModel(id, null);
            if (weightTable.getCode() != ResponseCode.OK) {
                // 分表未查到，回滚
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
                weightPo.setGmtModified(nowTime);
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
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> getFreightModelSimple(Long id, Long shopId) {
        APIReturnObject<FreightModelPo> poObj = freightDao.getShopFreightModel(id, shopId);
        if (poObj.getCode() != ResponseCode.OK) {
            return poObj;
        }
        // 构造 Vo 返回
        return new APIReturnObject<>(new FreightModelVo(poObj.getData()));
    }

    /**
     * 服务 f6：管理员修改店铺的运费模板
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Modified by Han Li at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> modifyShopFreightModel(Long shopId, Long id, FreightModelModifyVo freightModelModifyVo) {
        // 获取原来信息
        APIReturnObject<FreightModelPo> returnObject = freightDao.getShopFreightModel(id, shopId);
        if (returnObject.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        Long origShopId = returnObject.getData().getShopId();

        // 判断该商店是否拥有
        if (!origShopId.equals(shopId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }
        // 创建更新体
        FreightModelPo po = new FreightModelPo();
        po.setId(id);
        po.setName(freightModelModifyVo.getName());
        po.setUnit(freightModelModifyVo.getUnit());

        // 写入数据库
        int response;
        try {
            response = freightDao.updateFreightModel(po);
        } catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("Duplicate entry")) {
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FREIGHT_MODEL_NAME_SAME);
            } else {
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
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }


    /**
     * 服务 f7：管理员删除店铺的运费模板
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    @Transactional
    public APIReturnObject<?> deleteShopFreightModel(Long shopId, Long id) {
        // 获取原来信息
        APIReturnObject<FreightModelPo> returnObject = freightDao.getShopFreightModel(id, shopId);
        if (returnObject.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
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
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 服务 f8：店家或管理员为商铺定义默认运费模板
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Modified by Han Li at 25/11/2020 16:58
     */
    public APIReturnObject<?> defineDefaultFreightModel(Long shopId, Long id) {
        // 获取原来信息
        APIReturnObject<FreightModelPo> returnObject = freightDao.getShopFreightModel(id, shopId);
        if (returnObject.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        Long origShopId = returnObject.getData().getShopId();

        // 判断该商店是否拥有
        if (!origShopId.equals(shopId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
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
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 服务 f9：管理员定义重量模板明细
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> createWeightFreightModel(Long shopId, Long id, WeightFreightModelVo weightFreightModelVo) {
        APIReturnObject<FreightModelPo> object = freightDao.getShopFreightModel(id, shopId);
        if (object.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, object.getCode(), object.getErrMsg());
        }
        if (object.getData().getType() != 0) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }

        WeightFreightModelPo weightFreightModelPo = new WeightFreightModelPo();
//        if (freightDao.isConflictByRegionIdForWeight(weightFreightModelVo.getRegionId()) > 0) {
//            return new APIReturnObject<>(HttpStatus.CONFLICT, ResponseCode.REGION_SAME);
//        }

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
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<List<WeightFreightModelVo>> getWeightFreightModel(Long shopId, Long id) {
        APIReturnObject<FreightModelPo> object = freightDao.getShopFreightModel(id, shopId);
        if (object.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, object.getCode(), object.getErrMsg());
        }

        if (object.getData().getType() != 0) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }

        APIReturnObject<List<WeightFreightModelPo>> returnObject = freightDao.getWeightFreightModel(id, null);
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
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> createPieceFreightModel(Long shopId, Long id, PieceFreightModelVo pieceFreightModelVo) {
        APIReturnObject<FreightModelPo> object = freightDao.getShopFreightModel(id, shopId);
        if (object.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, object.getCode(), object.getErrMsg());
        }
        // 件数模板
        if (object.getData().getType() != 1) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }

        PieceFreightModelPo pieceFreightModelPo = new PieceFreightModelPo();
//        if (freightDao.isConflictByRegionIdForWeight(weightFreightModelVo.getRegionId()) > 0) {
//            return new APIReturnObject<>(HttpStatus.CONFLICT, ResponseCode.REGION_SAME);
//        }

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
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<List<PieceFreightModelVo>> getPieceFreightModel(Long shopId, Long id) {
        APIReturnObject<FreightModelPo> object = freightDao.getShopFreightModel(id, shopId);
        if (object.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, object.getCode(), object.getErrMsg());
        }

        if (object.getData().getType() != 1) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }

        APIReturnObject<List<PieceFreightModelPo>> returnObject = freightDao.getPieceFreightModel(id, null);
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
     * **内部方法** 校验重量模板明细是否属于店铺
     * @param shopId
     * @param detailId
     * @return
     */
    private boolean judgeWeightItemBelongs(Long shopId, Long detailId) {
        // 找小表
        APIReturnObject<List<WeightFreightModelPo>> judge = freightDao.getWeightFreightModel(null, detailId);
        if (judge.getCode() != ResponseCode.OK) {
            return false;
        }
        List<WeightFreightModelPo> judgePo = judge.getData();
        // 没找到
        if (judgePo.size() != 1) {
            return false;
        }
        // 得到大表 id，找大表
        Long id = judgePo.get(0).getFreightModelId();

        APIReturnObject<FreightModelPo> bigTable = freightDao.getShopFreightModel(id, shopId);
        if (bigTable.getCode() != ResponseCode.OK) {
            return false;
        }
        // 校验 shopId
        FreightModelPo freightModelPo = bigTable.getData();
        return freightModelPo.getShopId().equals(shopId);
    }

    /**
     * **内部方法** 校验件数模板明细是否属于店铺
     * @param shopId
     * @param detailId
     * @return
     */
    private boolean judgePieceItemBelongs(Long shopId, Long detailId) {
        // 找小表
        APIReturnObject<List<PieceFreightModelPo>> judge = freightDao.getPieceFreightModel(null, detailId);
        if (judge.getCode() != ResponseCode.OK) {
            return false;
        }
        List<PieceFreightModelPo> judgePo = judge.getData();
        // 没找到
        if (judgePo.size() != 1) {
            return false;
        }
        // 得到大表 id，找大表
        Long id = judgePo.get(0).getFreightModelId();

        APIReturnObject<FreightModelPo> bigTable = freightDao.getShopFreightModel(id, shopId);
        if (bigTable.getCode() != ResponseCode.OK) {
            return false;
        }
        // 校验 shopId
        FreightModelPo freightModelPo = bigTable.getData();
        return freightModelPo.getShopId().equals(shopId);
    }

    /**
     * 服务 f13：管理员修改重量模板明细
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> modifyWeightFreightModel(Long shopId, Long detailId, WeightFreightModelVo vo) {
        if (!judgeWeightItemBelongs(shopId, detailId)) {
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
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> deleteWeightFreightModel(Long shopId, Long detailId) {
        if (!judgeWeightItemBelongs(shopId, detailId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }

        try {
            int response = freightDao.deleteWeightFreightModel(detailId);
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
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> modifyPieceFreightModel(Long shopId, Long detailId, PieceFreightModelVo vo) {
        if (!judgePieceItemBelongs(shopId, detailId)) {
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
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> deletePieceFreightModel(Long shopId, Long detailId) {
        if (!judgePieceItemBelongs(shopId, detailId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }

        try {
            int response = freightDao.deletePieceFreightModel(detailId);
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
     * **内部方法**：将 weightFreightModelPo 从数据库中更新
     *
     * 返回：0 成功 1 重复 2 其他错误
     */
    private int updateWeightFreightModelPo(WeightFreightModelPo po) {
        try {
            po.setGmtModified(LocalDateTime.now());
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
     *
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
     *
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
