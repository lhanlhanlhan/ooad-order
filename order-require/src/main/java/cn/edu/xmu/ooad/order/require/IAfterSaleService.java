package cn.edu.xmu.ooad.order.require;

public interface IAfterSaleService {

    /**
     * TODO - 检查一张售后单是否可以创建支付单
     *
     * @param afterSaleId
     */
    boolean canAfterSaleCreatePayment(Long afterSaleId);

    /**
     * TODO - 检查售后单是否属于一间店铺
     *
     * @param afterSaleId
     */
    boolean isAfterSaleBelongsToShop(Long afterSaleId, Long shopId);

    /**
     * TODO - 检查售后单是否属于一个买家
     *
     * @param afterSaleId
     */
    boolean isAfterSaleBelongsToCustomer(Long afterSaleId, Long customerId);
}
