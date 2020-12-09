package cn.edu.xmu.ooad.order.connector.service;

import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 商铺服务 (由【商品模块】提供)
 *
 * @author Han Li
 * Created at 28/11/2020 11:15 上午
 * Modified by Han Li at 28/11/2020 11:15 上午
 */
@Service
public class ShopService {

    /**
     * TODO - 从商品模块获取商铺资料
     *
     * @param shopId 商铺 Id
     * @return cn.edu.xmu.ooad.order.connector.model.UserInfo
     */
    public Map<String, Object> getShopInfo(Long shopId) {
        if (shopId == null) {
            // 在这里把其他模块的 Model 转为统一的 Map
            Map<String, Object> shopInfo = new HashMap<>();
            shopInfo.put("id", null);
            shopInfo.put("name", "廈門大學");
            shopInfo.put("gmtCreate", null);
            shopInfo.put("gmtModified", null);
            return shopInfo;
        }
        if (shopId == 1) {
            // 在这里把其他模块的 Model 转为统一的 Map
            Map<String, Object> shopInfo = new HashMap<>();
            shopInfo.put("id", shopId);
            shopInfo.put("name", "厦大思明百货");
            shopInfo.put("gmtCreate", LocalDateTime.now().toString());
            shopInfo.put("gmtModified", null);
            return shopInfo;
        } else {
            // 在这里把其他模块的 Model 转为统一的 Map
            Map<String, Object> shopInfo = new HashMap<>();
            shopInfo.put("id", shopId);
            shopInfo.put("name", "厦大翔安百货");
            shopInfo.put("gmtCreate", LocalDateTime.now().toString());
            shopInfo.put("gmtModified", null);
            return shopInfo;
        }
    }

    /**
     * TODO - 从商品模块获取商品资料
     *
     * @param skuId 商铺 Id
     * @return cn.edu.xmu.ooad.order.connector.model.UserInfo
     */
    public SkuInfo getSkuInfo(Long skuId) {
        if (skuId == 1) {
            // 在这里把其他模块的 Model 转为统一的 Map
            return new SkuInfo(skuId, "Apple MacBook Pro 2020 13' Grey", 1324500L, 1L, 1280L, 1L);
        } else {
            // 在这里把其他模块的 Model 转为统一的 Map
            return new SkuInfo(skuId, "Air Jordan 1 Red-White", 262800L, 2L, 2230L, 2L);
        }
    }

    /**
     * TODO - 扣库存
     *
     * @param skuId 商品 Id
     * @param count 商品 购入数量
     * @return 0：成功；1：商品不够
     */
    public int decreaseStock(Long skuId, Integer count) {
        return 0;
    }
}
