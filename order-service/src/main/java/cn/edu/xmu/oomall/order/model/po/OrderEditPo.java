package cn.edu.xmu.oomall.order.model.po;

import lombok.Data;

/**
 * 修改订单的 Po 对象
 *
 * @author Han Li
 * Created at 28/11/2020 3:44 下午
 * Modified by Han Li at 28/11/2020 3:44 下午
 */
@Data
public class OrderEditPo {

    private Long id;
    private String signature;
    private Byte orderType;

    // 下列字段都是可被更新的字段
    private String consignee;
    private Long regionId;
    private String address;
    private String mobile;
    private String message;
    private Boolean beDeleted;
    private Byte state;

    private String shipmentSn; // 發貨單號。。。

}
