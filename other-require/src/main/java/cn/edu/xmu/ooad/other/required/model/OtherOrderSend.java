package cn.edu.xmu.ooad.other.required.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtherOrderSend implements Serializable {
	Long orderItemId;
	Long shopId;
	Long regionId;
	String consignee;
	Integer quantity;
	String address;
	String mobile;
	Long customerId;
}
