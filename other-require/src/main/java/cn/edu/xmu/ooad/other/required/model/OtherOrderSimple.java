package cn.edu.xmu.ooad.other.required.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherOrderSimple implements Serializable {

	private static final long serialVersionUID = 1164817179729653819L;
	List<OtherOrderItemSimple> orderItems;
	Long orderId;
	String orderSn;
	Long shopId;
	Long customerId;
}
