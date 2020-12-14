package cn.edu.xmu.ooad.other.required.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class OtherOrderItemSimple implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6965060482691403162L;
	//id
	Long orderItemId;
	Long skuId;
	Integer quantity;
	Long orderId;
	//返回实际支付的单价
	Long price;
	String skuName;
	
	OtherOrderItemSimple(){}
	/**
	 * 构造函数
	 * @param id ：orderitemid
	 * @param skuId
	 * @param quantity
	 * @param shopId
	 * @param finalPrice ：实际支付时的单价
	 */
	OtherOrderItemSimple(Long id,Long skuId, Integer quantity, Long price, String skuName){
		setOrderItemId(id);
		setSkuId(skuId);
		setPrice(price);
		setQuantity(quantity);
		setSkuName(skuName);
	}
}
