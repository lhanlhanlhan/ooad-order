package cn.edu.xmu.ooad.other.required.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtherOrderSend {
	Long skuId;
	Long shopId;
	Long regionId;
	String consignee;
	Integer quantity;
	String address;
	String mobile;
}
