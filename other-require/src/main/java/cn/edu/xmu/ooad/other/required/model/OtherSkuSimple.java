package cn.edu.xmu.ooad.other.required.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class OtherSkuSimple implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6050705991352804495L;
	Long skuId;
	Long spuId;
	String skuName;
	String skuSn;
	Long pirce;
	public OtherSkuSimple() {};
	public OtherSkuSimple(Long id,Long spuId,String skuName,Long price) {
		setSkuId(skuId);
		setSpuId(spuId);
		setSkuName(skuName);
		setPirce(price);
	}
}
