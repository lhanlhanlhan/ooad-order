package cn.edu.xmu.oomall.order.model.po;

import java.time.LocalDateTime;

public class OrderSimplePo {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.id
     *
     * @mbg.generated
     */
    private Long id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.customer_id
     *
     * @mbg.generated
     */
    private Long customerId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.shop_id
     *
     * @mbg.generated
     */
    private Long shopId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.order_sn
     *
     * @mbg.generated
     */
    private String orderSn;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.pid
     *
     * @mbg.generated
     */
    private Long pid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.consignee
     *
     * @mbg.generated
     */
    private String consignee;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.region_id
     *
     * @mbg.generated
     */
    private Long regionId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.address
     *
     * @mbg.generated
     */
    private String address;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.mobile
     *
     * @mbg.generated
     */
    private String mobile;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.message
     *
     * @mbg.generated
     */
    private String message;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.order_type
     *
     * @mbg.generated
     */
    private Byte orderType;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.freight_price
     *
     * @mbg.generated
     */
    private Long freightPrice;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.coupon_id
     *
     * @mbg.generated
     */
    private Long couponId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.coupon_activity_id
     *
     * @mbg.generated
     */
    private Long couponActivityId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.discount_price
     *
     * @mbg.generated
     */
    private Long discountPrice;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.origin_price
     *
     * @mbg.generated
     */
    private Long originPrice;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.presale_id
     *
     * @mbg.generated
     */
    private Long presaleId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.groupon_discount
     *
     * @mbg.generated
     */
    private Long grouponDiscount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.rebate_num
     *
     * @mbg.generated
     */
    private Integer rebateNum;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.confirm_time
     *
     * @mbg.generated
     */
    private LocalDateTime confirmTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.shipment_sn
     *
     * @mbg.generated
     */
    private String shipmentSn;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.state
     *
     * @mbg.generated
     */
    private Byte state;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.substate
     *
     * @mbg.generated
     */
    private Byte substate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.be_deleted
     *
     * @mbg.generated
     */
    private Byte beDeleted;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.gmt_created
     *
     * @mbg.generated
     */
    private LocalDateTime gmtCreated;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order.gmt_modified
     *
     * @mbg.generated
     */
    private LocalDateTime gmtModified;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.id
     *
     * @return the value of order.id
     *
     * @mbg.generated
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.id
     *
     * @param id the value for order.id
     *
     * @mbg.generated
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.customer_id
     *
     * @return the value of order.customer_id
     *
     * @mbg.generated
     */
    public Long getCustomerId() {
        return customerId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.customer_id
     *
     * @param customerId the value for order.customer_id
     *
     * @mbg.generated
     */
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.shop_id
     *
     * @return the value of order.shop_id
     *
     * @mbg.generated
     */
    public Long getShopId() {
        return shopId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.shop_id
     *
     * @param shopId the value for order.shop_id
     *
     * @mbg.generated
     */
    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.order_sn
     *
     * @return the value of order.order_sn
     *
     * @mbg.generated
     */
    public String getOrderSn() {
        return orderSn;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.order_sn
     *
     * @param orderSn the value for order.order_sn
     *
     * @mbg.generated
     */
    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn == null ? null : orderSn.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.pid
     *
     * @return the value of order.pid
     *
     * @mbg.generated
     */
    public Long getPid() {
        return pid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.pid
     *
     * @param pid the value for order.pid
     *
     * @mbg.generated
     */
    public void setPid(Long pid) {
        this.pid = pid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.consignee
     *
     * @return the value of order.consignee
     *
     * @mbg.generated
     */
    public String getConsignee() {
        return consignee;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.consignee
     *
     * @param consignee the value for order.consignee
     *
     * @mbg.generated
     */
    public void setConsignee(String consignee) {
        this.consignee = consignee == null ? null : consignee.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.region_id
     *
     * @return the value of order.region_id
     *
     * @mbg.generated
     */
    public Long getRegionId() {
        return regionId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.region_id
     *
     * @param regionId the value for order.region_id
     *
     * @mbg.generated
     */
    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.address
     *
     * @return the value of order.address
     *
     * @mbg.generated
     */
    public String getAddress() {
        return address;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.address
     *
     * @param address the value for order.address
     *
     * @mbg.generated
     */
    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.mobile
     *
     * @return the value of order.mobile
     *
     * @mbg.generated
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.mobile
     *
     * @param mobile the value for order.mobile
     *
     * @mbg.generated
     */
    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.message
     *
     * @return the value of order.message
     *
     * @mbg.generated
     */
    public String getMessage() {
        return message;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.message
     *
     * @param message the value for order.message
     *
     * @mbg.generated
     */
    public void setMessage(String message) {
        this.message = message == null ? null : message.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.order_type
     *
     * @return the value of order.order_type
     *
     * @mbg.generated
     */
    public Byte getOrderType() {
        return orderType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.order_type
     *
     * @param orderType the value for order.order_type
     *
     * @mbg.generated
     */
    public void setOrderType(Byte orderType) {
        this.orderType = orderType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.freight_price
     *
     * @return the value of order.freight_price
     *
     * @mbg.generated
     */
    public Long getFreightPrice() {
        return freightPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.freight_price
     *
     * @param freightPrice the value for order.freight_price
     *
     * @mbg.generated
     */
    public void setFreightPrice(Long freightPrice) {
        this.freightPrice = freightPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.coupon_id
     *
     * @return the value of order.coupon_id
     *
     * @mbg.generated
     */
    public Long getCouponId() {
        return couponId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.coupon_id
     *
     * @param couponId the value for order.coupon_id
     *
     * @mbg.generated
     */
    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.coupon_activity_id
     *
     * @return the value of order.coupon_activity_id
     *
     * @mbg.generated
     */
    public Long getCouponActivityId() {
        return couponActivityId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.coupon_activity_id
     *
     * @param couponActivityId the value for order.coupon_activity_id
     *
     * @mbg.generated
     */
    public void setCouponActivityId(Long couponActivityId) {
        this.couponActivityId = couponActivityId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.discount_price
     *
     * @return the value of order.discount_price
     *
     * @mbg.generated
     */
    public Long getDiscountPrice() {
        return discountPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.discount_price
     *
     * @param discountPrice the value for order.discount_price
     *
     * @mbg.generated
     */
    public void setDiscountPrice(Long discountPrice) {
        this.discountPrice = discountPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.origin_price
     *
     * @return the value of order.origin_price
     *
     * @mbg.generated
     */
    public Long getOriginPrice() {
        return originPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.origin_price
     *
     * @param originPrice the value for order.origin_price
     *
     * @mbg.generated
     */
    public void setOriginPrice(Long originPrice) {
        this.originPrice = originPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.presale_id
     *
     * @return the value of order.presale_id
     *
     * @mbg.generated
     */
    public Long getPresaleId() {
        return presaleId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.presale_id
     *
     * @param presaleId the value for order.presale_id
     *
     * @mbg.generated
     */
    public void setPresaleId(Long presaleId) {
        this.presaleId = presaleId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.groupon_discount
     *
     * @return the value of order.groupon_discount
     *
     * @mbg.generated
     */
    public Long getGrouponDiscount() {
        return grouponDiscount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.groupon_discount
     *
     * @param grouponDiscount the value for order.groupon_discount
     *
     * @mbg.generated
     */
    public void setGrouponDiscount(Long grouponDiscount) {
        this.grouponDiscount = grouponDiscount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.rebate_num
     *
     * @return the value of order.rebate_num
     *
     * @mbg.generated
     */
    public Integer getRebateNum() {
        return rebateNum;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.rebate_num
     *
     * @param rebateNum the value for order.rebate_num
     *
     * @mbg.generated
     */
    public void setRebateNum(Integer rebateNum) {
        this.rebateNum = rebateNum;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.confirm_time
     *
     * @return the value of order.confirm_time
     *
     * @mbg.generated
     */
    public LocalDateTime getConfirmTime() {
        return confirmTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.confirm_time
     *
     * @param confirmTime the value for order.confirm_time
     *
     * @mbg.generated
     */
    public void setConfirmTime(LocalDateTime confirmTime) {
        this.confirmTime = confirmTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.shipment_sn
     *
     * @return the value of order.shipment_sn
     *
     * @mbg.generated
     */
    public String getShipmentSn() {
        return shipmentSn;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.shipment_sn
     *
     * @param shipmentSn the value for order.shipment_sn
     *
     * @mbg.generated
     */
    public void setShipmentSn(String shipmentSn) {
        this.shipmentSn = shipmentSn == null ? null : shipmentSn.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.state
     *
     * @return the value of order.state
     *
     * @mbg.generated
     */
    public Byte getState() {
        return state;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.state
     *
     * @param state the value for order.state
     *
     * @mbg.generated
     */
    public void setState(Byte state) {
        this.state = state;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.substate
     *
     * @return the value of order.substate
     *
     * @mbg.generated
     */
    public Byte getSubstate() {
        return substate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.substate
     *
     * @param substate the value for order.substate
     *
     * @mbg.generated
     */
    public void setSubstate(Byte substate) {
        this.substate = substate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.be_deleted
     *
     * @return the value of order.be_deleted
     *
     * @mbg.generated
     */
    public Byte getBeDeleted() {
        return beDeleted;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.be_deleted
     *
     * @param beDeleted the value for order.be_deleted
     *
     * @mbg.generated
     */
    public void setBeDeleted(Byte beDeleted) {
        this.beDeleted = beDeleted;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.gmt_created
     *
     * @return the value of order.gmt_created
     *
     * @mbg.generated
     */
    public LocalDateTime getGmtCreated() {
        return gmtCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.gmt_created
     *
     * @param gmtCreated the value for order.gmt_created
     *
     * @mbg.generated
     */
    public void setGmtCreated(LocalDateTime gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order.gmt_modified
     *
     * @return the value of order.gmt_modified
     *
     * @mbg.generated
     */
    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order.gmt_modified
     *
     * @param gmtModified the value for order.gmt_modified
     *
     * @mbg.generated
     */
    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }
}