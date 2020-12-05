package cn.edu.xmu.oomall.order.mapper;

import cn.edu.xmu.oomall.order.model.po.FreightModelPo;
import cn.edu.xmu.oomall.order.model.po.FreightModelPoExample;
import java.util.List;

public interface FreightModelPoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table freight_model
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table freight_model
     *
     * @mbg.generated
     */
    int insert(FreightModelPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table freight_model
     *
     * @mbg.generated
     */
    int insertSelective(FreightModelPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table freight_model
     *
     * @mbg.generated
     */
    List<FreightModelPo> selectByExample(FreightModelPoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table freight_model
     *
     * @mbg.generated
     */
    FreightModelPo selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table freight_model
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(FreightModelPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table freight_model
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(FreightModelPo record);
}