package cn.edu.xmu.ooad.order.freight.model.vo;

import cn.edu.xmu.ooad.order.freight.model.po.PieceFreightModelPo;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
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
    //后端数据，不用校验
    private Long id;

    @Positive(message = "地区 id 必须为正数")
    @NotNull(message = "地区 id 不能省略")
    private Long regionId;

    @Positive(message = "首件数量必须为正数")
    @NotNull(message = "首件不能省略")
    private Integer firstItem;

    @PositiveOrZero(message = "首件价格必须为正数或0")
    @NotNull(message = "首件价格不能省略")
    private Long firstItemPrice;

    @Positive(message = "额外件数必须为正数")
    @NotNull(message = "额外件数不能省略")
    private Integer additionalItems;

    @PositiveOrZero(message = "额外件数价格必须为正数或0")
    @NotNull(message = "额外件数价格不能省略")
    private Long additionalItemsPrice;

    // 后端数据，不用校验
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
