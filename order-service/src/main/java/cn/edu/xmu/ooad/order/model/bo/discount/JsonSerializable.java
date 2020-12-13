package cn.edu.xmu.ooad.order.model.bo.discount;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 标明该种优惠可以被序列化为 JSON 字符串
 */
public interface JsonSerializable {

    String toJsonString() throws JsonProcessingException;
}
