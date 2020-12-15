package cn.edu.xmu.ooad.order.centre.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FreightCalcItem implements Serializable {
    private Long skuId;
    private Integer count;
}
