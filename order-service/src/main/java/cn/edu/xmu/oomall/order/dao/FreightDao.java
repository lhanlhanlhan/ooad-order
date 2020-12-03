package cn.edu.xmu.oomall.order.dao;

import cn.edu.xmu.oomall.order.controller.FreightController;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.mapper.FreightModelPoMapper;
import cn.edu.xmu.oomall.order.mapper.OrderSimplePoMapper;
import cn.edu.xmu.oomall.order.mapper.PieceFreightModelPoMapper;
import cn.edu.xmu.oomall.order.mapper.WeightFreightModelPoMapper;
import cn.edu.xmu.oomall.order.model.po.*;
import cn.edu.xmu.oomall.order.model.vo.FreightModelModifyVo;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    // Freight Model Po 的 Mapper
    @Autowired
    private FreightModelPoMapper freightModelPoMapper;

    // Piece Freight Model Po 的 Mapper
    @Autowired
    private PieceFreightModelPoMapper pieceFreightModelPoMapper;

    // Weight Freight Model Po 的 Mapper
    @Autowired
    private WeightFreightModelPoMapper weightFreightModelPoMapper;

    // 邱明规定的 Date Formatter
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");

    /**
     * 获取分页的运费模板概要列表
     *
     * @param name 模板名称
     * @param page 页码
     * @param pageSize 页大小
     * @return 分页的运费模板概要列表
     */
    public APIReturnObject<PageInfo<FreightModelPo>> getFreightModel(String name, int page, int pageSize, Long shopId){
        APIReturnObject<List<FreightModelPo>> freightModelPos = getFreightModel(name,shopId);
        if (freightModelPos.getCode() != ResponseCode.OK){
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
    public APIReturnObject<List<FreightModelPo>> getFreightModel(String name, Long shopId){
        // 创建 PoExample 对象，以实现多参数查询
        FreightModelPoExample freightModelPoExample = new FreightModelPoExample();
        // 将查询字段放入 Example 对象的 查询规则 (Criteria) 里面去
        FreightModelPoExample.Criteria criteria=freightModelPoExample.createCriteria();
        if(name!=null){
            criteria.andNameEqualTo(name);
        }
        if(shopId!=null){
            criteria.andShopIdEqualTo(shopId);
        }
        List<FreightModelPo> freightModelPoList;
        try {
            freightModelPoList = freightModelPoMapper.selectByExample(freightModelPoExample);
        } catch (Exception e) {
            // 数据库 错误
            logger.info(e.getMessage());
            return new APIReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>(freightModelPoList);
    }


    /**
     * 通过店铺Id和模板Id获取一个运费模板概要
     *
     * @return 一个运费模板概要
     */
    public APIReturnObject<FreightModelPo> getFreightModelByShopIdAndId(Long shopId, Long id){
        // 创建 PoExample 对象，以实现多参数查询
        FreightModelPoExample freightModelPoExample = new FreightModelPoExample();
        // 将查询字段放入 Example 对象的 查询规则 (Criteria) 里面去
        FreightModelPoExample.Criteria criteria=freightModelPoExample.createCriteria();
        if(id!=null){
            criteria.andIdEqualTo(id);
        }
        if(shopId!=null){
            criteria.andShopIdEqualTo(shopId);
        }
        FreightModelPo freightModelPo;
        //查出来一定只有一个结果
        try {
            freightModelPo = freightModelPoMapper.selectByExample(freightModelPoExample).get(0);
        } catch (Exception e) {
            // 数据库 错误
            logger.info(e.getMessage());
            return new APIReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>(freightModelPo);
    }

    /**
     * 根据模板ID返回一个重量模板明细
     */
    public APIReturnObject<WeightFreightModelPo> getWeightFreightModel(Long fId, Long id){
        WeightFreightModelPoExample example=new WeightFreightModelPoExample();
        WeightFreightModelPoExample.Criteria criteria=example.createCriteria();
        if(id!=null){
            criteria.andIdEqualTo(id);
        }
        if(fId!=null){
            criteria.andFreightModelIdEqualTo(fId);
        }
        WeightFreightModelPo po;
        try {
             po= weightFreightModelPoMapper.selectByExample(example).get(0);
        } catch (Exception e) {
            // 数据库 错误
            logger.info(e.getMessage());
            return new APIReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>(po);
    }

    /**
     * 根据模板ID返回一个重量模板明细
     */
    public APIReturnObject<PieceFreightModelPo> getPieceFreightModel(Long fId, Long id){
        PieceFreightModelPoExample example=new PieceFreightModelPoExample();
        PieceFreightModelPoExample.Criteria criteria=example.createCriteria();
        if(id!=null){
            criteria.andIdEqualTo(id);
        }
        if(fId!=null){
            criteria.andFreightModelIdEqualTo(fId);
        }
        PieceFreightModelPo po;
        try {
            po= pieceFreightModelPoMapper.selectByExample(example).get(0);
        } catch (Exception e) {
            // 数据库 错误
            logger.info(e.getMessage());
            return new APIReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>(po);
    }

    /**
     * 检测是否与fright_model表中同shop的运费模板名重复
     *
     */
    public long isConflictByName(Long shopId, String name){
        FreightModelPoExample freightModelPoExample = new FreightModelPoExample();
        // 将查询字段放入 Example 对象的 查询规则 (Criteria) 里面去
        FreightModelPoExample.Criteria criteria=freightModelPoExample.createCriteria();
        if(shopId!=null){
            criteria.andShopIdEqualTo(shopId);
        }
        if(name!=null){
            criteria.andNameEqualTo(name);
        }
        return freightModelPoMapper.countByExample(freightModelPoExample);
    }

    /**
     * 检测是否与weight_fright_model表中地区ID重复
     *
     */
    public long isConflictByRegionIdForWeight(Long regionId){
        WeightFreightModelPoExample weightFreightModelPoExample=new WeightFreightModelPoExample();
        WeightFreightModelPoExample.Criteria criteria=weightFreightModelPoExample.createCriteria();
        if(regionId!=null){
            criteria.andRegionIdEqualTo(regionId);
        }
        return weightFreightModelPoMapper.countByExample(weightFreightModelPoExample);
    }

    /**
     * 检测是否与piece_fright_model表中地区ID重复
     *
     */
    public long isConflictByRegionIdForPiece(Long regionId){
        PieceFreightModelPoExample pieceFreightModelPoExample=new PieceFreightModelPoExample();
        PieceFreightModelPoExample.Criteria criteria=pieceFreightModelPoExample.createCriteria();
        if(regionId!=null){
            criteria.andRegionIdEqualTo(regionId);
        }
        return pieceFreightModelPoMapper.countByExample(pieceFreightModelPoExample);
    }


    public int deleteFreightModel(FreightModelPo po){
        return freightModelPoMapper.deleteByPrimaryKey(po.getId());
    }
    public int deleteWeightFreightModel(Long id){
        return weightFreightModelPoMapper.deleteByPrimaryKey(id);
    }
    public int deletePieceFreightModel(Long id){
        return pieceFreightModelPoMapper.deleteByPrimaryKey(id);
    }
    /**
     * 更新运费模板概要
     *
     * @param po 运费模板概要 Po
     */
    public int updateFreightModel(FreightModelPo po){return freightModelPoMapper.updateByPrimaryKey(po);}

    /**
     * 插入运费模板概要
     *
     * @param po 运费模板概要 Po
     */
    public int addFreightModel(FreightModelPo po){
        return freightModelPoMapper.insert(po);
    }

    /**
     * 插入重量运费模板
     *
     * @param po 重量运费模板 Po
     */
    public int addWeightFreightModel(WeightFreightModelPo po){
        return weightFreightModelPoMapper.insert(po);
    }

    /**
     * 更新重量运费模板明细
     *
     * @param po 重量模板明细 Po
     */
    public int updateWeightFreightModel(WeightFreightModelPo po){return weightFreightModelPoMapper.updateByPrimaryKey(po);}

    /**
     * 更新运费模板概要
     *
     * @param po 运费模板概要 Po
     */
    public int updatePieceFreightModel(PieceFreightModelPo po){return pieceFreightModelPoMapper.updateByPrimaryKey(po);}


    /**
     * 插入件数运费模板
     *
     * @param po 件数运费模板 Po
     */
    public int addPieceFreightModel(PieceFreightModelPo po){
        return pieceFreightModelPoMapper.insert(po);
    }
}
