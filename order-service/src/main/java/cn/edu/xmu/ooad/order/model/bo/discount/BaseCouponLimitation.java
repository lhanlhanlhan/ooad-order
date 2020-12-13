package cn.edu.xmu.ooad.order.model.bo.discount;

import cn.edu.xmu.ooad.order.model.bo.OrderItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 优惠券限制基类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseCouponLimitation {

    // 件数或金额 (分)
    protected long value;
    protected String className;

    public BaseCouponLimitation(long value) {
        this.value = value;
        this.className = this.getClass().getName();
    }

    public static BaseCouponLimitation getInstance(String jsonString) throws JsonProcessingException, ClassNotFoundException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonString);
        String className = root.get("className").asText();
        return (BaseCouponLimitation) mapper.readValue(jsonString, Class.forName(className));
    }

    public abstract boolean pass(List<OrderItem> orderItems);
}
