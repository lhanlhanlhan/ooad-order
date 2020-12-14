package cn.edu.xmu.ooad.other.required.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class OtherOrderItemComplex implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6921657369555237544L;
	//id
	Long orderItemId;
	Long skuId;
	Integer quantity;
	Long orderId;
	//返回实际支付的单价
	Long price;
	String skuName;
	//order 部分
	String orderSn;
	Long shopId;
	
	public OtherOrderItemComplex() {}
	public OtherOrderItemComplex(Long orderItemId,
	Long skuId,
	Integer quantity,
	Long orderId,
	Long price,
	String skuName,
	String orderSn,
	Long shopId) {
		setOrderItemId(orderItemId);
		setSkuId(skuId);
		setQuantity(quantity);
		setOrderId(orderId);
		setPrice(price);
		setSkuName(skuName);
		setOrderSn(orderSn);
		setShopId(shopId);
	}
}
