package cn.edu.xmu.oomall.order.connector.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * 售后服务 (功能由【其他模块】提供)
 *
 * @author Han Li
 * Created at 5/12/2020 5:41 下午
 * Modified by Han Li at 5/12/2020 5:41 下午
 */
@Service
public class AfterSaleService {

    /**
     * TODO - 检查一张售后单是否可以创建支付单
     * @param afterSaleId
     * @return
     */
    public boolean canAfterSaleCreatePayment(Long afterSaleId) {
        return true;
    }

    /**
     * TODO - 检查售后单是否属于一间店铺
     * @param afterSaleId
     * @return
     */
    public boolean isAfterSaleBelongsToShop(Long afterSaleId, Long shopId) {
        return true;
    }

    /**
     * TODO - 检查售后单是否属于一个买家
     * @param afterSaleId
     * @return
     */
    public boolean isAfterSaleBelongsToCustomer(Long afterSaleId, Long customerId) {
        return true;
    }
}
