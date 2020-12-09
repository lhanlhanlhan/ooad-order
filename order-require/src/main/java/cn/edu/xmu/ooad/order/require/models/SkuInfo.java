package cn.edu.xmu.ooad.order.require.models;

import java.io.Serializable;

public class SkuInfo implements Serializable {

    private Long id;

    private String name; // 可能要到两个表查

    private Long price;

    private Long weight;

    private Long shopId; // 可能要到两个表查

    private Long freightId; // 可能要到两个表查

    public SkuInfo(Long id, String name, Long price, Long weight, Long shopId, Long freightId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.weight = weight;
        this.shopId = shopId;
        this.freightId = freightId;
    }

    public SkuInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getFreightId() {
        return freightId;
    }

    public void setFreightId(Long freightId) {
        this.freightId = freightId;
    }
}
