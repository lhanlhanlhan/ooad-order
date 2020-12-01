package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.connector.ShopConnector;
import cn.edu.xmu.oomall.order.exceptions.NotInStockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 商铺服务 (功能由【商品模块】提供，这里负责把它们提供的数据进行来到订单模块后的封装)
 *
 * @author Han Li
 * Created at 28/11/2020 11:15 上午
 * Modified by Han Li at 28/11/2020 11:15 上午
 */
@Service
public class ShopService {

    @Autowired
    private ShopConnector shopConnector;

    /**
     * TODO - 获取商铺资料
     *
     * @param shopId 商铺 Id
     * @return cn.edu.xmu.oomall.order.connector.model.UserInfo
     */
    public Map<String, Object> getShopInfo(Long shopId) {
        return shopConnector.getShopInfo(shopId);
    }

    /**
     * TODO - 获取商品资料
     *
     * @param skuId 商品 Sku Id
     * @return cn.edu.xmu.oomall.order.connector.model.UserInfo
     */
    public Map<String, Object> getSkuInfo(Long skuId) {
        return shopConnector.getSkuInfo(skuId);
    }

    /**
     * TODO - 扣库存
     *
     * @param skuId 商品 Id
     * @param count 商品 购入数量
     * @return 0：成功；1：商品不够
     */
    public int decreaseStock(Long skuId, Long count) {
        return 0;
    }
}
