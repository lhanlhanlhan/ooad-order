package cn.edu.xmu.ooad.order.model.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 新订单申请传值对象 (前端传入)
 *
 * @author Han Li
 * Created at 2020/11/5 15:31
 * Modified at 2020/11/5 15:31
 **/
@Data
@ApiModel("新订单申请传值对象")
public class OrderNewVo {
    @NotNull(message = "订单项目列表为空")
    @Size(min = 1, message = "订单项目列表至少要有 1 个订单项")
    private List<OrderItemVo> orderItems;

    @NotBlank(message = "姓名为空")
    @Size(max = 255, message = "姓名太长")
    private String consignee;

    @Positive(message = "地区码必须为正值")
    @NotNull(message = "地区码为空")
    private Long regionId;

    @NotBlank(message = "地址为空")
    @Size(max = 255, message = "地址太长")
    private String address;

    @NotBlank(message = "电话为空")
    @Size(max = 255, message = "电话太长")
    private String mobile;

    @Size(max = 1024, message = "留言太长")
    private String message;

    @Positive(message = "优惠券码必须为正值")
    private Long couponId;

    @Positive(message = "预售活动号码必须为正值")
    private Long presaleId;

    @Positive(message = "团购活动号码必须为正值")
    private Long grouponId;
}
