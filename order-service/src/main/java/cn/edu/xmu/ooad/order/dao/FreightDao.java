package cn.edu.xmu.ooad.order.dao;

import cn.edu.xmu.ooad.order.annotations.RedisOptimized;
import cn.edu.xmu.ooad.order.controller.FreightController;
import cn.edu.xmu.ooad.order.mapper.FreightModelPoMapper;
import cn.edu.xmu.ooad.order.mapper.PieceFreightModelPoMapper;
import cn.edu.xmu.ooad.order.mapper.WeightFreightModelPoMapper;
import cn.edu.xmu.ooad.order.model.bo.FreightModel;
import cn.edu.xmu.ooad.order.model.bo.PieceFreightModelRule;
import cn.edu.xmu.ooad.order.model.bo.WeightFreightModelRule;
import cn.edu.xmu.ooad.order.model.po.*;
import cn.edu.xmu.ooad.order.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.utils.RedisUtils;
import cn.edu.xmu.ooad.order.utils.ResponseCode;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static cn.edu.xmu.ooad.order.utils.Accessories.addRandomTime;

/**
 * 运费 Dao
 *
 * @author Chen Kechun
 * Created at 25/11/2020 4:41 下午
 * Modified by Han Li at 6/12/2020 8:27 下午
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

    // Redis 工具
    @Autowired
    private RedisUtils redisUtils;

    // 配置文件中的运费模板失效时间
    @Value("${orders.freight-model.redis-expire}")
    private long freightModelRedisTimeout;

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
    @RedisOptimized
    public FreightModel getFreightModel(Long id) {
        String key = "fm_" + id;
        FreightModel freightModel = redisUtils.get(key, FreightModel.class);
        if (null != freightModel) {
            if (logger.isDebugEnabled()) {
                logger.debug("getFreightModel: hit redis cache, key = " + key);
            }
            return freightModel;
        }
        // 未命中，找数据库，得到大表
        FreightModelPo fmPo;
        try {
            fmPo = freightModelPoMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.error("数据库错误：" + e.getMessage());
            return null;
        }
        // 取出来的值存入 Redis，空值也存，防止击穿
        if (fmPo != null) {
            FreightModel fm = FreightModel.create(fmPo);
            redisUtils.set(key, fm, addRandomTime(freightModelRedisTimeout));
            return fm;
        } else {
            redisUtils.set(key, null, addRandomTime(freightModelRedisTimeout));
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
     * 根据模板ID/明细ID返回重量模板明细
     */
    public APIReturnObject<List<WeightFreightModelPo>> getWeightFreightModels(Long fId, Long id) {
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
    public APIReturnObject<List<PieceFreightModelPo>> getPieceFreightModels(Long fId, Long id) {
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
     *
     * @param id
     * @param type
     * @return
     */
    @RedisOptimized
    public int deleteFreightModel(Long id, byte type) {
        // 先删除主表的 model
        int ret = freightModelPoMapper.deleteByPrimaryKey(id);
        if (ret <= 0) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ret;
        }
        // 删除缓存 (如可能)
        redisUtils.del("fm_" + id);
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
        // TODO - 模板明细的缓存耗子喂汁吧 去操您妈咯
        return ret;
    }

    @RedisOptimized
    public int deleteWeightFreightModelRule(Long id) {
        // 根据 id 获取缓存号
        String invKey = "iw_" + id;
        String modelRuleKey = redisUtils.get(invKey, String.class);
        if (modelRuleKey != null) {
            // 删除缓存及反向缓存 (如可能)
            redisUtils.del(modelRuleKey, modelRuleKey);
        }
        return weightFreightModelPoMapper.deleteByPrimaryKey(id);
    }

    @RedisOptimized
    public int deletePieceFreightModelRule(Long id) {
        // 根据 id 获取缓存号
        String invKey = "ip_" + id;
        String modelRuleKey = redisUtils.get(invKey, String.class);
        if (modelRuleKey != null) {
            // 删除缓存及反向缓存 (如可能)
            redisUtils.del(modelRuleKey, modelRuleKey);
        }
        return pieceFreightModelPoMapper.deleteByPrimaryKey(id);
    }

    /**
     * 更新运费模板概要
     *
     * @param po 运费模板概要 Po
     */
    @RedisOptimized
    public int updateFreightModel(FreightModelPo po) {
        po.setGmtModified(LocalDateTime.now());
        int res = freightModelPoMapper.updateByPrimaryKeySelective(po);
        if (res != 1) {
            return res;
        }
        // 删除缓存 (如有)
        String key = "fm_" + po.getId();
        redisUtils.del(key);
        return 1;
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
    @RedisOptimized
    public int updateWeightFreightModel(WeightFreightModelPo po) {
        po.setGmtModified(LocalDateTime.now());
        int res = weightFreightModelPoMapper.updateByPrimaryKeySelective(po);
        if (res != 1) {
            return res;
        }
        // 删除缓存 (如有)
        String key = "wf_" + po.getId();
        redisUtils.del(key);
        return 1;
    }

    /**
     * 更新运费模板概要
     *
     * @param po 运费模板概要 Po
     */
    @RedisOptimized
    public int updatePieceFreightModel(PieceFreightModelPo po) {
        po.setGmtModified(LocalDateTime.now());
        int res = pieceFreightModelPoMapper.updateByPrimaryKeySelective(po);
        if (res != 1) {
            return res;
        }
        // 删除缓存 (如有)
        String key = "pf_" + po.getId();
        redisUtils.del(key);
        return 1;
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
     *
     * @param modelId
     * @param regionId
     * @return
     */
    @RedisOptimized
    public WeightFreightModelRule getWeightFreightModelRule(Long modelId, Long regionId) {
        String key = "wf_" + modelId + "_" + regionId;
        WeightFreightModelRule weightFreightModelRule = redisUtils.get(key, WeightFreightModelRule.class);
        if (null != weightFreightModelRule) {
            if (logger.isDebugEnabled()) {
                logger.debug("getWeightFreightModelRule: hit redis cache, key = " + key);
            }
            return weightFreightModelRule;
        }
        // 未命中，找数据库
        WeightFreightModelPoExample example = new WeightFreightModelPoExample();
        WeightFreightModelPoExample.Criteria criteria = example.createCriteria();

        criteria.andFreightModelIdEqualTo(modelId);
        criteria.andRegionIdEqualTo(regionId);

        WeightFreightModelPo po = null;
        try {
            List<WeightFreightModelPo> poList = weightFreightModelPoMapper.selectByExample(example);
            // 如果返回空列表，就返回 null
            if (poList.size() == 1) {
                // 获取 List 的第一个值
                po = poList.get(0);
            }
        } catch (Exception e) {
            // 数据库 错误
            logger.error(e.getMessage());
            return null;
        }

        // 取出来的值存入 Redis，空值也存，防止击穿
        if (po != null) {
            WeightFreightModelRule fm = new WeightFreightModelRule(po);
            redisUtils.set(key, fm, addRandomTime(freightModelRedisTimeout));
            // 建立反向 hash，方便从 weight model 的 id 获取其母版号及地区
            String invertedKey = "iw_" + fm.getId(); // Index of Weight model
            redisUtils.set(invertedKey, key, addRandomTime(freightModelRedisTimeout));
            return fm;
        } else {
            redisUtils.set(key, null, addRandomTime(freightModelRedisTimeout));
            return null;
        }
    }

    /**
     * 内部调用：获取一个件数运费模板的某个地区的明细，没有明细就返回没有
     *
     * @param modelId
     * @param regionId
     * @return
     */
    @RedisOptimized
    public PieceFreightModelRule getPieceFreightModelRule(Long modelId, Long regionId) {
        String key = "pf_" + modelId + "_" + regionId;
        PieceFreightModelRule pieceFreightModelRule = redisUtils.get(key, PieceFreightModelRule.class);
        if (null != pieceFreightModelRule) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPieceFreightModelRule: hit redis cache, key = " + key);
            }
            return pieceFreightModelRule;
        }
        // 未命中，找数据库
        PieceFreightModelPoExample example = new PieceFreightModelPoExample();
        PieceFreightModelPoExample.Criteria criteria = example.createCriteria();

        criteria.andFreightModelIdEqualTo(modelId);
        criteria.andRegionIdEqualTo(regionId);

        PieceFreightModelPo po = null;
        try {
            List<PieceFreightModelPo> poList = pieceFreightModelPoMapper.selectByExample(example);
            // 如果返回空列表，就返回 null
            if (poList.size() == 1) {
                // 获取 List 的第一个值
                po = poList.get(0);
            }
        } catch (Exception e) {
            // 数据库 错误
            logger.error(e.getMessage());
            return null;
        }

        // 取出来的值存入 Redis，空值也存，防止击穿
        if (po != null) {
            PieceFreightModelRule fm = new PieceFreightModelRule(po);
            redisUtils.set(key, fm, addRandomTime(freightModelRedisTimeout));
            // 建立反向 hash，方便从 weight model 的 id 获取其母版号及地区
            String invertedKey = "ip_" + fm.getId(); // Index of Piece Model
            redisUtils.set(invertedKey, key, addRandomTime(freightModelRedisTimeout));
            return fm;
        } else {
            redisUtils.set(key, null, addRandomTime(freightModelRedisTimeout));
            return null;
        }
    }

    /**
     * 工具函數：列舉滿足商店 id、運費模板 id 的運費模板數量 (可以用來鑑定權限)
     *
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
