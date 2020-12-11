package cn.edu.xmu.ooad.goods.require.model;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrderSimple implements Serializable {

    private Long customer_id;
    private Long sku_id;

    public OrderSimple(){}

    public OrderSimple(Long customer_id, Long sku_id)
    {
        this.customer_id = customer_id;
        this.sku_id = sku_id;
    }

}
