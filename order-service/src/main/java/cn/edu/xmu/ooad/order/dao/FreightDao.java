package cn.edu.xmu.ooad.order.dao;

import cn.edu.xmu.ooad.order.model.po.*;
import cn.edu.xmu.ooad.order.utils.ResponseCode;
import cn.edu.xmu.ooad.order.controller.FreightController;
import cn.edu.xmu.ooad.order.mapper.FreightModelPoMapper;
import cn.edu.xmu.ooad.order.mapper.PieceFreightModelPoMapper;
import cn.edu.xmu.ooad.order.mapper.WeightFreightModelPoMapper;
import cn.edu.xmu.oomall.order.model.po.*;
import cn.edu.xmu.ooad.order.utils.APIReturnObject;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 运费 Dao
 *
 * @author Chen Kechun
 * Created at 25/11/2020 4:41 下午
 * Modified by Chen Kechun at 25/11/2020 4:41 下午
 */
@Repository
public class FreightDao {
    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(FreightController.class);
    // 邱明规定的 Date Formatter
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");
    // Freight Model Po 的 Mapper
    @Autowired
    private FreightModelPoMapper freightModelPoMapper;
    // Piece Freight Model Po 的 Mapper
    @Autowired
    private PieceFreightModelPoMapper pieceFreightModelPoMapper;
    // Weight Freight Model Po 的 Mapper
    @Autowired
    private WeightFreightModelPoMapper weightFreightModelPoMapper;

    /**
     * 获取分页的运费模板概要列表
     *
     * @param name     模板名称
     * @param page     页码
     * @param pageSize 页大小
     * @return 分页的运费模板概要列表
     */
    public APIReturnObject<PageInfo<FreightModelPo>> getFreightModel(String name, int page, int pageSize, Long shopId) {
        APIReturnObject<List<FreightModelPo>> freightModelPos = getFreightModel(null, name, shopId);
        if (freightModelPos.getCode() != ResponseCode.OK) {
            return new APIReturnObject<>(freightModelPos.getCode(), freightModelPos.getErrMsg());
        }
        return new APIReturnObject<>(new PageInfo<>(freightModelPos.getData()));
    }

    /**
     * 获取不分页的运费模板概要列表
     *
     * @param name 模板名称
     * @return 不分页的运费模板概要列表
     */
    public APIReturnObject<List<FreightModelPo>> getFreightModel(Long id, String name, Long shopId) {
        // 创建 PoExample 对象，以实现多参数查询
        FreightModelPoExample freightModelPoExample = new FreightModelPoExample();
        // 将查询字段放入 Example 对象的 查询规则 (Criteria) 里面去
        FreightModelPoExample.Criteria criteria = freightModelPoExample.createCriteria();
        if (id != null) {
            criteria.andIdEqualTo(id);
        }
        if (name != null) {
            criteria.andNameEqualTo(name);
        }
        if (shopId != null) {
            criteria.andShopIdEqualTo(shopId);
        }
        List<FreightModelPo> freightModelPoList;
        try {
            freightModelPoList = freightModelPoMapper.selectByExample(freightModelPoExample);
        } catch (Exception e) {
            // 数据库 错误
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>(freightModelPoList);
    }

    /**
     * 获取运费模板 (单个)
     */
    public FreightModelPo getFreightModel(Long id) {
        try {
            // 存在就存在，不存在就数据库错误 (商品、订单服务器出现不一致)
            return freightModelPoMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            // 数据库 错误
            return null;
        }
    }

    /**
     * 获取店铺的运费模板 (单个)
     */
    public APIReturnObject<FreightModelPo> getShopFreightModel(Long id, Long shopId) {
        // 创建 PoExample 对象，以实现多参数查询
        FreightModelPoExample freightModelPoExample = new FreightModelPoExample();
        // 将查询字段放入 Example 对象的 查询规则 (Criteria) 里面去
        FreightModelPoExample.Criteria criteria = freightModelPoExample.createCriteria();
        criteria.andIdEqualTo(id);
        criteria.andShopIdEqualTo(shopId);

        List<FreightModelPo> freightModelPoList;
        try {
            freightModelPoList = freightModelPoMapper.selectByExample(freightModelPoExample);
            // 根据实际情况做取舍
            if (freightModelPoList.size() == 0) {
                // 返回不存在
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
            } else {
                return new APIReturnObject<>(freightModelPoList.get(0));
            }
        } catch (Exception e) {
            // 数据库 错误
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 根据模板ID/明细ID返回一个重量模板明细
     */
    public APIReturnObject<List<WeightFreightModelPo>> getWeightFreightModel(Long fId, Long id) {
        WeightFreightModelPoExample example = new WeightFreightModelPoExample();
        WeightFreightModelPoExample.Criteria criteria = example.createCriteria();
        if (id != null) {
            criteria.andIdEqualTo(id);
        }
        if (fId != null) {
            criteria.andFreightModelIdEqualTo(fId);
        }
        try {
            List<WeightFreightModelPo> poList = weightFreightModelPoMapper.selectByExample(example);
            return new APIReturnObject<>(poList);
        } catch (Exception e) {
            // 数据库 错误
            logger.error(e.getMessage());
            return new APIReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 根据模板ID/明细ID返回重量模板明细
     */
    public APIReturnObject<List<PieceFreightModelPo>> getPieceFreightModel(Long fId, Long id) {
        PieceFreightModelPoExample example = new PieceFreightModelPoExample();
        PieceFreightModelPoExample.Criteria criteria = example.createCriteria();
        if (id != null) {
            criteria.andIdEqualTo(id);
        }
        if (fId != null) {
            criteria.andFreightModelIdEqualTo(fId);
        }
        try {
            List<PieceFreightModelPo> poList = pieceFreightModelPoMapper.selectByExample(example);
            return new APIReturnObject<>(poList);
        } catch (Exception e) {
            // 数据库 错误
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 删除运费模板
     * @param id
     * @param type
     * @return
     */
    public int deleteFreightModel(Long id, byte type) {
        // 先删除主表的 model
        int ret = freightModelPoMapper.deleteByPrimaryKey(id);
        if (ret <= 0) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ret;
        }
        // 再删除分表的 model
        if (type == 1) {
            PieceFreightModelPoExample example = new PieceFreightModelPoExample();
            PieceFreightModelPoExample.Criteria criteria = example.createCriteria();
            criteria.andFreightModelIdEqualTo(id);
            ret = pieceFreightModelPoMapper.deleteByExample(example);
        } else {
            WeightFreightModelPoExample example = new WeightFreightModelPoExample();
            WeightFreightModelPoExample.Criteria criteria = example.createCriteria();
            criteria.andFreightModelIdEqualTo(id);
            ret = weightFreightModelPoMapper.deleteByExample(example);
        }
        return ret;
    }

    public int deleteWeightFreightModel(Long id) {
        return weightFreightModelPoMapper.deleteByPrimaryKey(id);
    }

    public int deletePieceFreightModel(Long id) {
        return pieceFreightModelPoMapper.deleteByPrimaryKey(id);
    }

    /**
     * 更新运费模板概要
     *
     * @param po 运费模板概要 Po
     */
    public int updateFreightModel(FreightModelPo po) {
        po.setGmtModified(LocalDateTime.now());
        return freightModelPoMapper.updateByPrimaryKeySelective(po);
    }

    /**
     * 插入运费模板概要
     *
     * @param po 运费模板概要 Po
     */
    public int addFreightModel(FreightModelPo po) {
        return freightModelPoMapper.insert(po);
    }

    /**
     * 插入重量运费模板
     *
     * @param po 重量运费模板 Po
     */
    public int addWeightFreightModel(WeightFreightModelPo po) {
        return weightFreightModelPoMapper.insert(po);
    }

    /**
     * 更新重量运费模板明细
     *
     * @param po 重量模板明细 Po
     */
    public int updateWeightFreightModel(WeightFreightModelPo po) {
        return weightFreightModelPoMapper.updateByPrimaryKeySelective(po);
    }

    /**
     * 更新运费模板概要
     *
     * @param po 运费模板概要 Po
     */
    public int updatePieceFreightModel(PieceFreightModelPo po) {
        return pieceFreightModelPoMapper.updateByPrimaryKeySelective(po);
    }

    /**
     * 插入件数运费模板
     *
     * @param po 件数运费模板 Po
     */
    public int addPieceFreightModel(PieceFreightModelPo po) {
        return pieceFreightModelPoMapper.insert(po);
    }

    /**
     * 内部调用：获取一个重量运费模板的某个地区的明细，没有明细就返回没有
     * @param modelId
     * @param regionId
     * @return
     */
    public WeightFreightModelPo getRegionWeightFreightModel(Long modelId, Long regionId) {
        WeightFreightModelPoExample example = new WeightFreightModelPoExample();
        WeightFreightModelPoExample.Criteria criteria = example.createCriteria();

        criteria.andFreightModelIdEqualTo(modelId);
        criteria.andRegionIdEqualTo(regionId);

        try {
            List<WeightFreightModelPo> poList = weightFreightModelPoMapper.selectByExample(example);
            // 如果返回空列表，就返回 null
            if (poList.size() != 1) {
                return null;
            }
            // 获取 List 的第一个值
            return poList.get(0);
        } catch (Exception e) {
            // 数据库 错误
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * 内部调用：获取一个件数运费模板的某个地区的明细，没有明细就返回没有
     * @param modelId
     * @param regionId
     * @return
     */
    public PieceFreightModelPo getRegionPieceFreightModel(Long modelId, Long regionId) {
        PieceFreightModelPoExample example = new PieceFreightModelPoExample();
        PieceFreightModelPoExample.Criteria criteria = example.createCriteria();

        criteria.andFreightModelIdEqualTo(modelId);
        criteria.andRegionIdEqualTo(regionId);

        try {
            List<PieceFreightModelPo> poList = pieceFreightModelPoMapper.selectByExample(example);
            // 如果返回空列表，就返回 null
            if (poList.size() != 1) {
                return null;
            }
            // 获取 List 的第一个值
            return poList.get(0);
        } catch (Exception e) {
            // 数据库 错误
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * 工具函數：列舉滿足商店 id、運費模板 id 的運費模板數量 (可以用來鑑定權限)
     * @param modelId
     * @param shopId
     * @return -1：查詢失敗；>=0：對應數量
     */
    public long countFreightModel(Long modelId, Long shopId, Byte type) {
        FreightModelPoExample example = new FreightModelPoExample();
        FreightModelPoExample.Criteria criteria = example.createCriteria();
        if (modelId == null && shopId == null) {
            return -1;
        }
        if (modelId != null) {
            criteria.andIdEqualTo(modelId);
        }
        if (shopId != null) {
            criteria.andShopIdEqualTo(shopId);
        }
        if (type != null) {
            criteria.andTypeEqualTo(type);
        }
        // 查詢數據庫
        long results;
        try {
            results = freightModelPoMapper.countByExample(example);
        } catch (Exception e) {
            logger.error(e.getMessage());
            // count 失敗
            return -1;
        }
        return results;
    }
}
