package cn.edu.xmu.ooad.other.required.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class OtherSkuComplex implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6107462360495654596L;
	//sku部分
	Long skuId;
	Long spuId;
	String skuName;
	String skuSn;
	Long pirce;
	
	//spu部分
	Long shopId;
	
	public OtherSkuComplex() {}
	public OtherSkuComplex(
			Long skuId,
			Long spuId,
			String skuName,
			String skuSn,
			Long pirce,
			Long shopId
			) {
		setSkuId(skuId);
		setSpuId(spuId);
		setSkuName(skuName);
		setSkuSn(skuSn);
		setPirce(pirce);
		setShopId(shopId);
	}
	
}
