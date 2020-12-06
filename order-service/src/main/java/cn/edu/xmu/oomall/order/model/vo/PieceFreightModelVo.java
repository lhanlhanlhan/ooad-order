package cn.edu.xmu.oomall.order.model.vo;

import cn.edu.xmu.oomall.order.model.po.PieceFreightModelPo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员定义件数模板明细的运费模板资料的 Vo (前段传入+后端返回)
 *
 * @author ：Chen Kechun
 * Created at 29/11/2020 17:01 下午
 * Modified by Chen Kechun at 29/11/2020 17:01下午
 */

@Data
@NoArgsConstructor
public class PieceFreightModelVo {
    private Long id;
    private Long regionId;
    private Integer firstItem;
    private Long firstItemPrice;
    private Integer additionalItems;
    private Long additionalItemsPrice;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public PieceFreightModelVo(PieceFreightModelPo po) {
        this.id = po.getId();
        this.regionId = po.getRegionId();
        this.firstItem = po.getFirstItems();
        this.firstItemPrice = po.getFirstItemsPrice();
        this.additionalItems = po.getAdditionalItems();
        this.additionalItemsPrice = po.getAdditionalItemsPrice();
        this.gmtCreate = po.getGmtCreate();
        this.gmtModified = po.getGmtModified();
    }
}
