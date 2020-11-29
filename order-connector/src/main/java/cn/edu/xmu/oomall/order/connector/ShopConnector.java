package cn.edu.xmu.oomall.order.connector;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 商铺服务连接器 (方便集成不同组提供的模块，提供高层封装)
 *
 * @author Han Li
 * Created at 28/11/2020 11:04 上午
 * Modified by Han Li at 28/11/2020 11:04 上午
 */
@Component
public class ShopConnector {

    /**
     * TODO - 从商品模块获取商铺资料
     *
     * @param shopId 商铺 Id
     * @return cn.edu.xmu.oomall.order.connector.model.UserInfo
     */
    public Map<String, Object> getShopInfo(Long shopId) {
        // 在这里把其他模块的 Model 转为统一的 Map
        Map<String, Object> shopInfo = new HashMap<>();
        shopInfo.put("id", shopId);
        shopInfo.put("name", "厦大百货");
        shopInfo.put("gmtCreate", LocalDateTime.now().toString());
        shopInfo.put("gmtModified", null);
        return shopInfo;
    }

    /**
     * TODO - 从商品模块获取商品资料
     *
     * @param skuId 商铺 Id
     * @return cn.edu.xmu.oomall.order.connector.model.UserInfo
     */
    public Map<String, Object> getSkuInfo(Long skuId) {
        // 在这里把其他模块的 Model 转为统一的 Map
        Map<String, Object> shopInfo = new HashMap<>();
        shopInfo.put("id", skuId);
        shopInfo.put("name", "Apple MacBook Pro 2020 13' Grey");
        shopInfo.put("price", 1324500L);
        return shopInfo;
    }

}
