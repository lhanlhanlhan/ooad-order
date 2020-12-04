package cn.edu.xmu.oomall.order.dao;

import cn.edu.xmu.oomall.order.controller.FreightController;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.mapper.FreightModelPoMapper;
import cn.edu.xmu.oomall.order.mapper.PieceFreightModelPoMapper;
import cn.edu.xmu.oomall.order.mapper.WeightFreightModelPoMapper;
import cn.edu.xmu.oomall.order.model.po.*;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
            logger.info(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>(freightModelPoList);
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
            logger.info(e.getMessage());
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
            logger.info(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 检测是否与fright_model表中同shop的运费模板名重复
     */
    public long isConflictByName(Long shopId, String name) {
        FreightModelPoExample freightModelPoExample = new FreightModelPoExample();
        // 将查询字段放入 Example 对象的 查询规则 (Criteria) 里面去
        FreightModelPoExample.Criteria criteria = freightModelPoExample.createCriteria();
        if (shopId != null) {
            criteria.andShopIdEqualTo(shopId);
        }
        if (name != null) {
            criteria.andNameEqualTo(name);
        }
        return freightModelPoMapper.countByExample(freightModelPoExample);
    }

    /**
     * 检测是否与weight_fright_model表中地区ID重复
     */
    public long isConflictByRegionIdForWeight(Long regionId) {
        WeightFreightModelPoExample weightFreightModelPoExample = new WeightFreightModelPoExample();
        WeightFreightModelPoExample.Criteria criteria = weightFreightModelPoExample.createCriteria();
        if (regionId != null) {
            criteria.andRegionIdEqualTo(regionId);
        }
        return weightFreightModelPoMapper.countByExample(weightFreightModelPoExample);
    }

    /**
     * 检测是否与piece_fright_model表中地区ID重复
     */
    public long isConflictByRegionIdForPiece(Long regionId) {
        PieceFreightModelPoExample pieceFreightModelPoExample = new PieceFreightModelPoExample();
        PieceFreightModelPoExample.Criteria criteria = pieceFreightModelPoExample.createCriteria();
        if (regionId != null) {
            criteria.andRegionIdEqualTo(regionId);
        }
        return pieceFreightModelPoMapper.countByExample(pieceFreightModelPoExample);
    }

    /**
     * 删除运费模板
     * @param id
     * @param type
     * @return
     */
    public int deleteFreightModel(Long id, Byte type) {
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
}
