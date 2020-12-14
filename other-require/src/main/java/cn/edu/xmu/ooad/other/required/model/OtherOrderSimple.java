package cn.edu.xmu.ooad.other.required.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class OtherOrderSimple implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1164817179729653819L;
	List<OtherOrderItemSimple> orderItems;
	Long orderId;
	String orderSn;
	Long shopId;
	
	/** 默认构造函数 不做任何操作
	 * 
	 */
	public OtherOrderSimple() {}
	/**通过orderItemId获取全部orderItems和订单相关信息，
	 * 即orderItemId->orderItem->orderId->order。
	 * 
	 * @param orderItems
	 * @param orderId
	 * @param orderSn
	 * @param shopId
	 */
	public OtherOrderSimple(List<OtherOrderItemSimple> orderItems, Long orderId,String orderSn,Long shopId) {
		setOrderItems(orderItems);
		setOrderId(orderId);
		setOrderSn(orderSn);
		setShopId(shopId);
	}
}
