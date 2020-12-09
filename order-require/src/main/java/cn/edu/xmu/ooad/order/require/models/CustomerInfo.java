package cn.edu.xmu.ooad.order.require.models;

public class CustomerInfo {

    Long id;

    String username;

    Long realName;

    Byte state;

    public CustomerInfo(Long id, String username, Long realName, Byte state) {
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

    public Long getRealName() {
        return realName;
    }

    public void setRealName(Long realName) {
        this.realName = realName;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }
}
