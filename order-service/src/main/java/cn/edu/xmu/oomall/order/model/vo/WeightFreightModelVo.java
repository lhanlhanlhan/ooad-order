package cn.edu.xmu.oomall.order.model.vo;

import cn.edu.xmu.oomall.order.model.po.WeightFreightModelPo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * * 管理员定义重量模板明细的运费模板资料的 Vo
 *
 * @author ：Chen Kechun
 * Created at 29/11/2020 17:01 下午
 * Modified by Chen Kechun at 29/11/2020 17:01下午
 */

@Data
@NoArgsConstructor
public class WeightFreightModelVo {
    private Long id;
    private Long firstWeight;
    private Long firstWeightFreight;
    private Long tenPrice;
    private Long fiftyPrice;
    private Long hundredPrice;
    private Long trihunPrice;
    private Long abovePrice;
    private Long regionId;
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
