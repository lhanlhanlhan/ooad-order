package cn.edu.xmu.oomall.order.mapper;

import cn.edu.xmu.oomall.order.model.po.PieceFreightModelPo;

public interface PieceFreightModelPoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    int insert(PieceFreightModelPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    int insertSelective(PieceFreightModelPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    PieceFreightModelPo selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(PieceFreightModelPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(PieceFreightModelPo record);
}