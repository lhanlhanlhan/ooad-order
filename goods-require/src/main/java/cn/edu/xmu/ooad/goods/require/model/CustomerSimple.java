package cn.edu.xmu.ooad.goods.require.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class CustomerSimple implements Serializable {

    private Long id;
    private String user_name;
    private String real_name;

    public CustomerSimple(){}

    public CustomerSimple(Long id, String user_name, String real_name)
    {
        this.id = id;
        this.user_name = user_name;
        this.real_name = real_name;
    }
}
