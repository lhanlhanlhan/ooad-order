package cn.edu.xmu.ooad.other.required;

import java.util.List;

import cn.edu.xmu.ooad.other.required.model.OtherSku;
import cn.edu.xmu.ooad.other.required.model.OtherSkuComplex;
import cn.edu.xmu.ooad.other.required.model.OtherSkuSimple;
import cn.edu.xmu.ooad.other.required.model.OtherSpu;
import cn.edu.xmu.ooad.other.required.model.OtherSpuSimple;

public interface IOtherGoodsService {
	/**根据skuid获取简单的sku信息，若资源无效或不存在，返回空
	 * @author chenqw
	 * @param skuId
	 * @return SkuSimple or null
	 */
	public OtherSkuSimple getSkuBySkuId(Long skuId);
	
	/**根据skuid获取包括spu信息在内的sku信息，若资源无效或不存在，返回空
	 * @author chenqw
	 * @param skuId
	 * @return SkuSimple or null
	 */
	public OtherSkuComplex getSkuWithSpuInfoBySkuId(Long skuId);
	
	/**根据spuid获取全部sku，若资源无效或不存在，返回空
	 * 
	 * @param spuId
	 * @return
	 */
	public OtherSpuSimple getSkusBySpuId(Long spuId);
	/**根据skuid获取详细的sku信息，若资源无效或不存在，返回空
	 * @author chenqw
	 * @param skuId
	 * @return
	 */
	public OtherSku getSkuCompleteBySkuId(Long skuId);
	/**根据shopId查询所有的spu及其包含的sku
	 * @param shopId
	 * @return
	 */
	public List<OtherSpu> getSpusbyShopId(Long shopId);
	public OtherSpu getSpubySpuId(Long SpuId);
}
