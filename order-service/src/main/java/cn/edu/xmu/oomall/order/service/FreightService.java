package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.connector.service.CustomerService;
import cn.edu.xmu.oomall.order.connector.service.ShopService;
import cn.edu.xmu.oomall.order.dao.FreightDao;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        FreightModelPo freightModelPo = new FreightModelPo();
        freightModelPo.setShopId(shopId);
        freightModelPo.setName(freightModelInfoVo.getName());
        freightModelPo.setType(freightModelInfoVo.getType());
        freightModelPo.setUnit(freightModelInfoVo.getUnit());
        freightModelPo.setGmtCreate(LocalDateTime.now());
        freightModelPo.setGmtModified(LocalDateTime.now());
        if (!insertFreightModelPo(freightModelPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>(freightModelPo);
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
     * Created by Chen Kechun at 25/11/2020 16:58
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
            APIReturnObject<List<FreightModelPo>> returnObject = freightDao.getFreightModel(name, shopId);
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
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> cloneFreightModel(Long shopId, Long id) {
        APIReturnObject<FreightModelPo> returnObject = freightDao.getFreightModelByShopIdAndId(shopId, id);
        if (returnObject.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        FreightModelPo po = returnObject.getData();
        po.setGmtCreate(LocalDateTime.now());
        po.setGmtModified(LocalDateTime.now());
        po.setName(po.getName() + Accessories.genSerialNumber());
        if (!insertFreightModelPo(po)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        return returnObject;
    }

    /**
     * 服务 f5：获得运费模板概要
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<FreightModelPo> getFreightModelSimple(Long id) {
        APIReturnObject<FreightModelPo> returnObject = freightDao.getFreightModelByShopIdAndId(null, id);
        return returnObject;
    }

    /**
     * 服务 f6：管理员修改店铺的运费模板
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> modifyShopFreightModel(Long shopId, Long id, FreightModelModifyVo freightModelModifyVo) {
        APIReturnObject<FreightModelPo> returnObject = freightDao.getFreightModelByShopIdAndId(shopId, id);
        if (returnObject.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        FreightModelPo po = returnObject.getData();
        String name = freightModelModifyVo.getName();
        if (freightDao.isConflictByName(shopId, name) != 0) {
            return new APIReturnObject<>(HttpStatus.CONFLICT, ResponseCode.FREIGHT_MODEL_NAME_SAME);
        } else {
            po.setName(name);
            po.setUnit(freightModelModifyVo.getUnit());
            if (!updateFreightModelPo(po)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        }
        return new APIReturnObject<>();
    }


    /**
     * 服务 f7：管理员修改店铺的运费模板
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> deleteShopFreightModel(Long shopId, Long id) {
        APIReturnObject<FreightModelPo> returnObject = freightDao.getFreightModelByShopIdAndId(shopId, id);
        if (returnObject.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        FreightModelPo po = returnObject.getData();
        if (!deleteFreightModelPo(po)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        APIReturnObject<WeightFreightModelPo> weight = freightDao.getWeightFreightModel(id, null);
        APIReturnObject<PieceFreightModelPo> piece = freightDao.getPieceFreightModel(id, null);
        deleteWeightFreightModelPo(weight.getData().getId());
        deletePieceFreightModelPo(piece.getData().getId());
        return new APIReturnObject<>();
    }

    /**
     * 服务 f8：店家或管理员为商铺定义默认运费模板
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> defineDefaultFreightModel(Long shopId, Long id) {
        APIReturnObject<FreightModelPo> returnObject = freightDao.getFreightModelByShopIdAndId(shopId, id);
        if (returnObject.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        FreightModelPo po = returnObject.getData();
        po.setDefaultModel((byte) 1);
        if (!updateFreightModelPo(po)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>();
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
        APIReturnObject<FreightModelPo> object = freightDao.getFreightModelByShopIdAndId(shopId, id);
        if (object.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, object.getCode(), object.getErrMsg());
        }
        WeightFreightModelPo weightFreightModelPo = new WeightFreightModelPo();
        if (freightDao.isConflictByRegionIdForWeight(weightFreightModelVo.getRegionId()) > 0) {
            return new APIReturnObject<>(HttpStatus.CONFLICT, ResponseCode.REGION_SAME);
        }
        weightFreightModelPo.setAbovePrice(weightFreightModelVo.getAbovePrice());
        weightFreightModelPo.setFiftyPrice(weightFreightModelVo.getFiftyPrice());
        weightFreightModelPo.setFirstWeight(weightFreightModelVo.getFirstWeight());
        weightFreightModelPo.setFirstWeightFreight(weightFreightModelVo.getFirstWeightFreight());
        weightFreightModelPo.setRegionId(weightFreightModelVo.getRegionId());
        weightFreightModelPo.setFreightModelId(id);
        weightFreightModelPo.setHundredPrice(weightFreightModelVo.getHundredPrice());
        weightFreightModelPo.setTenPrice(weightFreightModelVo.getTenPrice());
        weightFreightModelPo.setTrihunPrice(weightFreightModelVo.getTrihunPrice());
        weightFreightModelPo.setGmtCreate(LocalDateTime.now());
        weightFreightModelPo.setGmtModified(LocalDateTime.now());
        if (!insertWeightFreightModelPo(weightFreightModelPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>(weightFreightModelPo);
    }

    /**
     * 服务 f10：管理员查询重量模板明细
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<WeightFreightModelPo> getWeightFreightModel(Long shopId, Long id) {
        APIReturnObject<FreightModelPo> object = freightDao.getFreightModelByShopIdAndId(shopId, id);
        if (object.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, object.getCode(), object.getErrMsg());
        }
        APIReturnObject<WeightFreightModelPo> returnObject = freightDao.getWeightFreightModel(id, null);
        if (returnObject.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        return returnObject;
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
        APIReturnObject<FreightModelPo> object = freightDao.getFreightModelByShopIdAndId(shopId, id);
        if (object.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, object.getCode(), object.getErrMsg());
        }
        PieceFreightModelPo pieceFreightModelPo = new PieceFreightModelPo();
        if (freightDao.isConflictByRegionIdForPiece(pieceFreightModelVo.getRegionId()) > 0) {
            return new APIReturnObject<>(HttpStatus.CONFLICT, ResponseCode.REGION_SAME);
        }
        pieceFreightModelPo.setAdditionalItems(pieceFreightModelVo.getAdditionalItems());
        pieceFreightModelPo.setAdditionalItemsPrice(pieceFreightModelVo.getAdditionalItemsPrice());
        pieceFreightModelPo.setFirstItems(pieceFreightModelVo.getAdditionalItems());
        pieceFreightModelPo.setFirstItemsPrice(pieceFreightModelVo.getAdditionalItemsPrice());
        pieceFreightModelPo.setFreightModelId(id);
        pieceFreightModelPo.setRegionId(pieceFreightModelVo.getRegionId());
        pieceFreightModelPo.setGmtCreate(LocalDateTime.now());
        pieceFreightModelPo.setGmtModified(LocalDateTime.now());
        if (!insertPieceFreightModelPo(pieceFreightModelPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>(pieceFreightModelPo);
    }

    /**
     * 服务 f12：管理员查询件数模板明细
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<PieceFreightModelPo> getPieceFreightModel(Long shopId, Long id) {
        APIReturnObject<FreightModelPo> object = freightDao.getFreightModelByShopIdAndId(shopId, id);
        if (object.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, object.getCode(), object.getErrMsg());
        }
        APIReturnObject<PieceFreightModelPo> returnObject = freightDao.getPieceFreightModel(id, null);
        if (returnObject.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        return returnObject;
    }

    /**
     * 服务 f13：管理员修改重量模板明细
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> modifyWeightFreightModel(Long shopId, Long id, WeightFreightModelVo vo) {
        APIReturnObject<FreightModelPo> judge = freightDao.getFreightModelByShopIdAndId(shopId, id);
        if (judge.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, judge.getCode(), judge.getErrMsg());
        }
        APIReturnObject<WeightFreightModelPo> object = getWeightFreightModel(shopId, id);
        WeightFreightModelPo po = object.getData();
        if (freightDao.isConflictByRegionIdForWeight(vo.getRegionId()) > 0 && !vo.getRegionId().equals(po.getRegionId())) {
            return new APIReturnObject<>(HttpStatus.CONFLICT, ResponseCode.REGION_SAME);
        }
        po.setTrihunPrice(vo.getTrihunPrice());
        po.setTenPrice(vo.getTenPrice());
        po.setHundredPrice(vo.getHundredPrice());
        po.setRegionId(vo.getRegionId());
        po.setFirstWeightFreight(vo.getFirstWeightFreight());
        po.setFirstWeight(vo.getFirstWeight());
        po.setFiftyPrice(vo.getFiftyPrice());
        if (!updateWeightFreightModelPo(po)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>();
    }

    /**
     * 服务 f14：店家或管理员删掉重量运费模板明细
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> deleteWeightFreightModel(Long shopId, Long id) {
        APIReturnObject<WeightFreightModelPo> object = freightDao.getWeightFreightModel(null, id);
        WeightFreightModelPo po = object.getData();
        APIReturnObject<FreightModelPo> judge = freightDao.getFreightModelByShopIdAndId(shopId, po.getFreightModelId());
        if (judge.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, judge.getCode(), judge.getErrMsg());
        }
        if (!deleteWeightFreightModelPo(id)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
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
    public APIReturnObject<?> modifyPieceFreightModel(Long shopId, Long id, PieceFreightModelVo vo) {
        APIReturnObject<FreightModelPo> judge = freightDao.getFreightModelByShopIdAndId(shopId, id);
        if (judge.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, judge.getCode(), judge.getErrMsg());
        }
        APIReturnObject<PieceFreightModelPo> object = getPieceFreightModel(shopId, id);
        PieceFreightModelPo po = object.getData();
        if (freightDao.isConflictByRegionIdForPiece(vo.getRegionId()) > 0 && !vo.getRegionId().equals(po.getRegionId())) {
            return new APIReturnObject<>(HttpStatus.CONFLICT, ResponseCode.REGION_SAME);
        }
        po.setRegionId(vo.getRegionId());
        po.setFirstItems(vo.getFirstItem());
        po.setFirstItemsPrice(vo.getFirstItemPrice());
        po.setAdditionalItems(vo.getAdditionalItems());
        po.setAdditionalItemsPrice(vo.getAdditionalItemsPrice());
        if (!updatePieceFreightModelPo(po)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>();
    }

    /**
     * 服务 f16：店家或管理员删掉件数运费模板明细
     *
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Chen Kechun
     * Created at 25/11/2020 16:58
     * Created by Chen Kechun at 25/11/2020 16:58
     */
    public APIReturnObject<?> deletePieceFreightModel(Long shopId, Long id) {
        APIReturnObject<PieceFreightModelPo> object = freightDao.getPieceFreightModel(null, id);
        PieceFreightModelPo po = object.getData();
        APIReturnObject<FreightModelPo> judge = freightDao.getFreightModelByShopIdAndId(shopId, po.getFreightModelId());
        if (judge.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, judge.getCode(), judge.getErrMsg());
        }
        if (!deletePieceFreightModelPo(id)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>();
    }


    /**
     * **内部方法**：将 freightModelPo 从数据库中删除
     */
    public boolean deleteFreightModelPo(FreightModelPo po) {
        try {
            int response = freightDao.deleteFreightModel(po);
            return response > 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * **内部方法**：将 freightModelPo 从数据库中删除
     */
    public boolean deleteWeightFreightModelPo(Long id) {
        try {
            int response = freightDao.deleteWeightFreightModel(id);
            return response > 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * **内部方法**：将 freightModelPo 从数据库中删除
     */
    public boolean deletePieceFreightModelPo(Long id) {
        try {
            int response = freightDao.deletePieceFreightModel(id);
            return response > 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * **内部方法**：将 freightModelPo 从数据库中更新
     */
    private boolean updateFreightModelPo(FreightModelPo po) {
        try {
            po.setGmtModified(LocalDateTime.now());
            int response = freightDao.updateFreightModel(po);
            return response > 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * **内部方法**：将 weightFreightModelPo 从数据库中更新
     */
    private boolean updateWeightFreightModelPo(WeightFreightModelPo po) {
        try {
            po.setGmtModified(LocalDateTime.now());
            int response = freightDao.updateWeightFreightModel(po);
            return response > 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * **内部方法**：将 pieceFreightModelPo 从数据库中更新
     */
    private boolean updatePieceFreightModelPo(PieceFreightModelPo po) {
        try {
            po.setGmtModified(LocalDateTime.now());
            int response = freightDao.updatePieceFreightModel(po);
            return response > 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * **内部方法**：将 freightModelPo 插入数据库中
     */
    private boolean insertFreightModelPo(FreightModelPo po) {
        try {
            int response = freightDao.addFreightModel(po);
            return response > 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
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
