package cn.edu.xmu.ooad.other.required.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
	Long customerId;
	
}
