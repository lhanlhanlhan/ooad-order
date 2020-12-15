package cn.edu.xmu.ooad.order.order.model.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Positive;

/**
 * 更新订单信息 传值对象 (前端传入)
 *
 * @author Han Li
 * Created at 2020/11/5 15:48
 * Modified by Han Li at 2020/11/28 15:48
 **/
@Data
@ApiModel("买家更新订单信息传值对象")
public class OrderEditVo {

    private String consignee;
    @Positive(message = "地区 id 必须为正数")
    private Long regionId;
    private String address;
    private String mobile;
    private String message;
    private Boolean beDeleted;

}
