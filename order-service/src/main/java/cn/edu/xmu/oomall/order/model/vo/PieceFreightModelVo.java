package cn.edu.xmu.oomall.order.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * * 管理员定义件数模板明细的运费模板资料的 Vo
 *
 * @author ：Chen Kechun
 * Created at 29/11/2020 17:01 下午
 * Modified by Chen Kechun at 29/11/2020 17:01下午
 */

@Data
@AllArgsConstructor
public class PieceFreightModelVo {
    private Long regionId;
    private Integer firstItem;
    private Long firstItemPrice;
    private Integer additionalItems;
    private Long additionalItemsPrice;
}
