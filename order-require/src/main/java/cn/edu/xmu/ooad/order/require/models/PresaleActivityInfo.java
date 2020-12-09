package cn.edu.xmu.ooad.order.require.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PresaleActivityInfo implements Serializable {

    private Long id;

    private LocalDateTime payTime;

    private LocalDateTime endTime;

    private Byte state;

    private Long advancePayPrice;

    private Long restPayPrice;

    public PresaleActivityInfo(Long id, LocalDateTime payTime, LocalDateTime endTime, Byte state, Long advancePayPrice, Long restPayPrice) {
        this.id = id;
        this.payTime = payTime;
        this.endTime = endTime;
        this.state = state;
        this.advancePayPrice = advancePayPrice;
        this.restPayPrice = restPayPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public Long getAdvancePayPrice() {
        return advancePayPrice;
    }

    public void setAdvancePayPrice(Long advancePayPrice) {
        this.advancePayPrice = advancePayPrice;
    }

    public Long getRestPayPrice() {
        return restPayPrice;
    }

    public void setRestPayPrice(Long restPayPrice) {
        this.restPayPrice = restPayPrice;
    }
}
