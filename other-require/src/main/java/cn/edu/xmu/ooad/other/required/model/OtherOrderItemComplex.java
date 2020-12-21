package cn.edu.xmu.ooad.other.required.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherOrderItemComplex implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6921657369555237544L;
	//id
	Long orderItemId;
	Long customerId;
	Long skuId;
	Integer quantity;
	//实际支付总金额
	Long payment;
	//商品价格结算单价
	Long skuPrice;
	String skuName;
	String orderSn;
	Long shopId;
	
}
