package cn.edu.xmu.ooad.other.required.model;

import lombok.Data;

@Data
public class OtherSku {
	Long skuId;
	String name;
	String imageUrl;
	Integer inventory;
	Integer originPrice;
	Integer price;
	Boolean disable;
	
	public OtherSku(){}
	public OtherSku(Long skuId,
	String name,
	String imageUrl,
	Integer inventory,
	Integer originPrice,
	Integer price,
	Boolean disable) {
		setSkuId(skuId);
		setName(name);
		setImageUrl(imageUrl);
		setInventory(inventory);
		setOriginPrice(originPrice);
		setPrice(price);
		setDisable(disable);
	}
}
