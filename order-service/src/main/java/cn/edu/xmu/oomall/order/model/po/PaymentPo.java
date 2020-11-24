package cn.edu.xmu.oomall.order.model.po;

import java.time.LocalDateTime;

public class PaymentPo {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column payment.id
     *
     * @mbg.generated
     */
    private Long id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column payment.amout
     *
     * @mbg.generated
     */
    private Long amout;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column payment.actual_amount
     *
     * @mbg.generated
     */
    private Long actualAmount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column payment.payment_pattern
     *
     * @mbg.generated
     */
    private Byte paymentPattern;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column payment.pay_time
     *
     * @mbg.generated
     */
    private LocalDateTime payTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column payment.pay_sn
     *
     * @mbg.generated
     */
    private String paySn;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column payment.begin_time
     *
     * @mbg.generated
     */
    private LocalDateTime beginTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column payment.end_time
     *
     * @mbg.generated
     */
    private LocalDateTime endTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column payment.order_id
     *
     * @mbg.generated
     */
    private Long orderId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column payment.state
     *
     * @mbg.generated
     */
    private Byte state;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column payment.gmt_created
     *
     * @mbg.generated
     */
    private LocalDateTime gmtCreated;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column payment.gmt_modified
     *
     * @mbg.generated
     */
    private LocalDateTime gmtModified;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column payment.id
     *
     * @return the value of payment.id
     *
     * @mbg.generated
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column payment.id
     *
     * @param id the value for payment.id
     *
     * @mbg.generated
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column payment.amout
     *
     * @return the value of payment.amout
     *
     * @mbg.generated
     */
    public Long getAmout() {
        return amout;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column payment.amout
     *
     * @param amout the value for payment.amout
     *
     * @mbg.generated
     */
    public void setAmout(Long amout) {
        this.amout = amout;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column payment.actual_amount
     *
     * @return the value of payment.actual_amount
     *
     * @mbg.generated
     */
    public Long getActualAmount() {
        return actualAmount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column payment.actual_amount
     *
     * @param actualAmount the value for payment.actual_amount
     *
     * @mbg.generated
     */
    public void setActualAmount(Long actualAmount) {
        this.actualAmount = actualAmount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column payment.payment_pattern
     *
     * @return the value of payment.payment_pattern
     *
     * @mbg.generated
     */
    public Byte getPaymentPattern() {
        return paymentPattern;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column payment.payment_pattern
     *
     * @param paymentPattern the value for payment.payment_pattern
     *
     * @mbg.generated
     */
    public void setPaymentPattern(Byte paymentPattern) {
        this.paymentPattern = paymentPattern;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column payment.pay_time
     *
     * @return the value of payment.pay_time
     *
     * @mbg.generated
     */
    public LocalDateTime getPayTime() {
        return payTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column payment.pay_time
     *
     * @param payTime the value for payment.pay_time
     *
     * @mbg.generated
     */
    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column payment.pay_sn
     *
     * @return the value of payment.pay_sn
     *
     * @mbg.generated
     */
    public String getPaySn() {
        return paySn;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column payment.pay_sn
     *
     * @param paySn the value for payment.pay_sn
     *
     * @mbg.generated
     */
    public void setPaySn(String paySn) {
        this.paySn = paySn == null ? null : paySn.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column payment.begin_time
     *
     * @return the value of payment.begin_time
     *
     * @mbg.generated
     */
    public LocalDateTime getBeginTime() {
        return beginTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column payment.begin_time
     *
     * @param beginTime the value for payment.begin_time
     *
     * @mbg.generated
     */
    public void setBeginTime(LocalDateTime beginTime) {
        this.beginTime = beginTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column payment.end_time
     *
     * @return the value of payment.end_time
     *
     * @mbg.generated
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column payment.end_time
     *
     * @param endTime the value for payment.end_time
     *
     * @mbg.generated
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column payment.order_id
     *
     * @return the value of payment.order_id
     *
     * @mbg.generated
     */
    public Long getOrderId() {
        return orderId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column payment.order_id
     *
     * @param orderId the value for payment.order_id
     *
     * @mbg.generated
     */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column payment.state
     *
     * @return the value of payment.state
     *
     * @mbg.generated
     */
    public Byte getState() {
        return state;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column payment.state
     *
     * @param state the value for payment.state
     *
     * @mbg.generated
     */
    public void setState(Byte state) {
        this.state = state;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column payment.gmt_created
     *
     * @return the value of payment.gmt_created
     *
     * @mbg.generated
     */
    public LocalDateTime getGmtCreated() {
        return gmtCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column payment.gmt_created
     *
     * @param gmtCreated the value for payment.gmt_created
     *
     * @mbg.generated
     */
    public void setGmtCreated(LocalDateTime gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column payment.gmt_modified
     *
     * @return the value of payment.gmt_modified
     *
     * @mbg.generated
     */
    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column payment.gmt_modified
     *
     * @param gmtModified the value for payment.gmt_modified
     *
     * @mbg.generated
     */
    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }
}