package cn.edu.xmu.oomall.order.mapper;

import cn.edu.xmu.oomall.order.model.po.WeightFreightModelPo;

public interface WeightFreightModelPoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table weight_freight_model
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table weight_freight_model
     *
     * @mbg.generated
     */
    int insert(WeightFreightModelPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table weight_freight_model
     *
     * @mbg.generated
     */
    int insertSelective(WeightFreightModelPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table weight_freight_model
     *
     * @mbg.generated
     */
    WeightFreightModelPo selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table weight_freight_model
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(WeightFreightModelPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table weight_freight_model
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(WeightFreightModelPo record);
}