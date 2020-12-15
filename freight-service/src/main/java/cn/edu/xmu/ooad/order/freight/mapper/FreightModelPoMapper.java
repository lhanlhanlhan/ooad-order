package cn.edu.xmu.ooad.order.freight.mapper;

import cn.edu.xmu.ooad.order.freight.model.po.FreightModelPo;
import cn.edu.xmu.ooad.order.freight.model.po.FreightModelPoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FreightModelPoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table freight_model
     *
     * @mbg.generated
     */
    long countByExample(FreightModelPoExample example);

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
    int updateByExampleSelective(@Param("record") FreightModelPo record, @Param("example") FreightModelPoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table freight_model
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") FreightModelPo record, @Param("example") FreightModelPoExample example);

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