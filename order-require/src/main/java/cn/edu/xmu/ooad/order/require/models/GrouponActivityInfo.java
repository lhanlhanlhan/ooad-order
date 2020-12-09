package cn.edu.xmu.ooad.order.require.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class GrouponActivityInfo implements Serializable {

    private Long id;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private Byte state;

    private String strategy;

    public GrouponActivityInfo(Long id, LocalDateTime beginTime, LocalDateTime endTime, Byte state, String strategy) {
        this.id = id;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.state = state;
        this.strategy = strategy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(LocalDateTime beginTime) {
        this.beginTime = beginTime;
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

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
