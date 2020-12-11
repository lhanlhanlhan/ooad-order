package cn.edu.xmu.ooad.goods.require;

public interface IShareService {

    /**
     * 提供sku的id 以及分享id   判断该分享是否分享的是该sku
     */

    Boolean isSkuInShare(Long skuId, Long shareId);
}
