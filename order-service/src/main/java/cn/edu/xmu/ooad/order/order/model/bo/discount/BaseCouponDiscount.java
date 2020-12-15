package cn.edu.xmu.ooad.order.order.model.bo.discount;

import cn.edu.xmu.ooad.order.order.model.bo.order.OrderItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 优惠券/优惠活动优惠基类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseCouponDiscount implements Computable, JsonSerializable {

    // 百分点或金额 (分)
    protected long value;
    protected String className;
    protected BaseCouponLimitation couponLimitation;

    public BaseCouponDiscount(BaseCouponLimitation limitation, long value) {
        this.couponLimitation = limitation;
        this.value = value;
        this.className = this.getClass().getName();
    }

    public static BaseCouponDiscount getInstance(String jsonString) throws JsonProcessingException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonString);
        String className = root.get("className").asText();
        BaseCouponDiscount bc = (BaseCouponDiscount) Class.forName(className).getConstructor().newInstance();

        String limitation = root.get("couponLimitation").toString();
        BaseCouponLimitation bl = BaseCouponLimitation.getInstance(limitation);

        bc.setCouponLimitation(bl);
        bc.setValue(root.get("value").asLong());
        bc.setClassName(className);

        return bc;
    }

    public List<OrderItem> compute(List<OrderItem> orderItems) {
        if (!couponLimitation.pass(orderItems)) {
            for (OrderItem oi : orderItems) {
                oi.setCouponActId(null);
            }
            return orderItems;
        }

        calcAndSetDiscount(orderItems);

        return orderItems;
    }

    public abstract void calcAndSetDiscount(List<OrderItem> orderItems);

    @Override
    public String toJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
