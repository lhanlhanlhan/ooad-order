package cn.edu.xmu.ooad.other.required.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**sku集合及spu相关的内容，
 * 
 * @author chenqw
 *
 */
@Data
public class OtherSpuSimple implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5596867377128037070L;
	List<OtherSkuSimple> skus;
	Long shopId;
	Long spuId;
}
