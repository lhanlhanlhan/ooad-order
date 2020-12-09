package cn.edu.xmu.ooad.order.require.models;

import java.io.Serializable;

public class CustomerInfo implements Serializable {

    Long id;

    String username;

    String realName;

    Byte state;

    public CustomerInfo(Long id, String username, String realName, Byte state) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.state = state;
    }

    public CustomerInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }
}
