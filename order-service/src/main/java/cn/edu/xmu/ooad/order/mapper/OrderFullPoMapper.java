package cn.edu.xmu.ooad.order.mapper;

import cn.edu.xmu.ooad.order.model.po.OrderFullPo;
import cn.edu.xmu.ooad.order.model.po.OrderFullPoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrderFullPoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table orders
     *
     * @mbg.generated
     */
    long countByExample(OrderFullPoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table orders
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table orders
     *
     * @mbg.generated
     */
    int insert(OrderFullPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table orders
     *
     * @mbg.generated
     */
    int insertSelective(OrderFullPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table orders
     *
     * @mbg.generated
     */
    List<OrderFullPo> selectByExample(OrderFullPoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table orders
     *
     * @mbg.generated
     */
    OrderFullPo selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table orders
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") OrderFullPo record, @Param("example") OrderFullPoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table orders
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") OrderFullPo record, @Param("example") OrderFullPoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table orders
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(OrderFullPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table orders
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(OrderFullPo record);
}