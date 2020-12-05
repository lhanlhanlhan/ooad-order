package cn.edu.xmu.oomall.order.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 苗新宇
 * Created at 03/12/2020 0:15
 * Modified by  苗新宇 at  03/12/2020 0:15
 */
@Data
@NoArgsConstructor
public class RefundAmountVo {
    private Long amount;
    public RefundAmountVo(Long amount){
        this.amount=amount;
    }
}
