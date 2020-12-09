package cn.edu.xmu.ooad.order.require.models;

public class FlashSaleItemInfo {

    private Long id;

    private Long saleId;

    private Long skuId;

    private Long price;

    private Long quantity;

    public FlashSaleItemInfo(Long id, Long saleId, Long skuId, Long price, Long quantity) {
        this.id = id;
        this.saleId = saleId;
        this.skuId = skuId;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
