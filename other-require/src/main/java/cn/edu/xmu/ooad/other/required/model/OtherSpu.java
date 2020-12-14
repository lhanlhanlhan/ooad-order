package cn.edu.xmu.ooad.other.required.model;

import java.util.List;

import lombok.Data;

@Data
public class OtherSpu {
	Long spuId;
	Long shopId;
	List<OtherSku> skus;
	
	public OtherSpu() {}
	public OtherSpu(Long spuId,
	Long shopId,
	List<OtherSku> skus) {
		setShopId(shopId);
		setSpuId(spuId);
		setSkus(skus);
	}
}
