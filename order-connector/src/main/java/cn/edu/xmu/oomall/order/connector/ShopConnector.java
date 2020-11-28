package cn.edu.xmu.oomall.order.connector;

import org.springframework.stereotype.Component;

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
        return buildShopInfo(1L, "XMU Shopping Centre", "2020-11-22T00:00:00", "2020-11-22T00:00:00");
    }

    /**
     * 创建商铺信息在订单模块的字典
     * @param id
     * @param name
     * @param gmtCreate
     * @param gmtModified
     * @return
     */
    private Map<String, Object> buildShopInfo(Long id, String name, String gmtCreate, String gmtModified) {
        Map<String, Object> shopInfo = new HashMap<>();
        shopInfo.put("id", id);
        shopInfo.put("name", name);
        shopInfo.put("gmtCreate", gmtCreate);
        shopInfo.put("gmtModified", gmtModified);
        return shopInfo;
    }
}
