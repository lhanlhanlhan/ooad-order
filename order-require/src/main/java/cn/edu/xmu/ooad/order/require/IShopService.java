package cn.edu.xmu.ooad.order.require;

import cn.edu.xmu.ooad.order.require.models.ShopInfo;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;

public interface IShopService {

    /**
     * 从商品模块获取商铺资料
     *
     * @param shopId 商铺 Id
     * @return cn.edu.xmu.ooad.order.require.models.ShopInfo
     */
    ShopInfo getShopInfo(Long shopId);


    /**
     * 从商品模块获取商品资料
     *
     * @param skuId 商品 Id
     * @return cn.edu.xmu.ooad.order.require.models.SkuInfo
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * 商品模块数据库扣库存
     *
     * @param skuId 商品 Id
     * @param count 商品 购入数量
     * @return 0：成功；1：商品不够
     */
    int decreaseStock(Long skuId, Integer count);
}
