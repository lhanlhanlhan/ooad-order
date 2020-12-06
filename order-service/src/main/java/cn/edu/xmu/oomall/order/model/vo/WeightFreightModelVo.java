package cn.edu.xmu.oomall.order.model.vo;

import cn.edu.xmu.oomall.order.model.po.WeightFreightModelPo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

/**
 * 管理员定义重量模板明细的运费模板资料的 Vo (前端传入+后端返回)
 *
 * @author ：Chen Kechun
 * Created at 29/11/2020 17:01 下午
 * Modified by Chen Kechun at 29/11/2020 17:01下午
 */

@Data
@NoArgsConstructor
public class WeightFreightModelVo {
    // 后端数据，不用校验
    private Long id;

    @Positive(message = "首重必须为正数")
    @NotNull(message = "首重不能省略")
    private Long firstWeight;

    @PositiveOrZero(message = "首重价格必须为正数或0")
    @NotNull(message = "首重价格不能省略")
    private Long firstWeightFreight;

    @PositiveOrZero(message = "10kg 续重价格必须为正数或0")
    @NotNull(message = "10 kg 续重价格不能省略")
    private Long tenPrice;

    @PositiveOrZero(message = "50kg 续重价格必须为正数或0")
    @NotNull(message = "50 kg 续重价格不能省略")
    private Long fiftyPrice;

    @PositiveOrZero(message = "100kg 续重价格必须为正数或0")
    @NotNull(message = "100 kg 续重价格不能省略")
    private Long hundredPrice;

    @PositiveOrZero(message = "300kg 续重价格必须为正数或0")
    @NotNull(message = "300 kg 续重价格不能省略")
    private Long trihunPrice;

    @PositiveOrZero(message = "300kg+ 续重价格必须为正数或0")
    @NotNull(message = "300 kg+ 续重价格不能省略")
    private Long abovePrice;

    @Positive(message = "地区 id 必须为正数")
    @NotNull(message = "地区 id 不能省略")
    private Long regionId;

    // 后端数据，不用校验
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public WeightFreightModelVo(WeightFreightModelPo po) {
        this.id = po.getId();
        this.firstWeight = po.getFirstWeight();
        this.firstWeightFreight = po.getFirstWeightFreight();
        this.tenPrice = po.getTenPrice();
        this.fiftyPrice = po.getFiftyPrice();
        this.hundredPrice = po.getHundredPrice();
        this.trihunPrice = po.getTrihunPrice();
        this.abovePrice = po.getAbovePrice();
        this.regionId = po.getRegionId();
        this.gmtCreate = po.getGmtCreate();
        this.gmtModified = po.getGmtModified();
    }
}
