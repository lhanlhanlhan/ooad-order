package cn.edu.xmu.ooad.other.required.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherSkuSimple implements Serializable{

	private static final long serialVersionUID = -6050705991352804495L;
	Long id;
	String skuSn;
	String name;
	String imageUrl;
	Long inventory;
	Long originalPrice;
	Long price;
	Boolean disable;
	
}
