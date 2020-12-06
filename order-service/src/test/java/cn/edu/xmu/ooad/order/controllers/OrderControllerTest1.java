package cn.edu.xmu.ooad.order.controllers;

import cn.edu.xmu.ooad.order.OrderServiceApplication;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 订单控制器测试 1
 *
 * @author Han Li
 * Created at 25/11/2020 8:40 上午
 * Modified by Han Li at 25/11/2020 8:40 上午
 */
@SpringBootTest(classes = OrderServiceApplication.class)    // 标识本类是一个SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerTest1 {

    @Autowired
    private MockMvc mvc;

    /**
     * t1-o1: 获取所有状态
     *
     * @author Han Li
     * Created at 25/11/2020 08:51
     * Created by Han Li at 25/11/2020 08:51
     */
    @Test
    public void getAllStates() throws Exception {
        String token = genTestToken();

        String responseString = this.mvc.perform(
                get("/order/orders/states")
                        .header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();

        String expectedResponse = "{\"errno\":0,\"data\":[{\"code\":0,\"name\":\"新订单\"},{\"code\":1,\"name\":\"待支付定金\"},{\"code\":2,\"name\":\"待支付\"},{\"code\":3,\"name\":\"待参团\"},{\"code\":4,\"name\":\"已支付定金\"},{\"code\":5,\"name\":\"待支付尾款\"},{\"code\":6,\"name\":\"已支付尾款\"},{\"code\":7,\"name\":\"已支付\"},{\"code\":8,\"name\":\"已参团\"},{\"code\":9,\"name\":\"团购未到达门槛\"},{\"code\":10,\"name\":\"已成团\"},{\"code\":11,\"name\":\"发货中\"},{\"code\":12,\"name\":\"到货\"},{\"code\":13,\"name\":\"已签收\"},{\"code\":14,\"name\":\"已签收\"},{\"code\":15,\"name\":\"订单中止\"},{\"code\":16,\"name\":\"订单中止\"},{\"code\":17,\"name\":\"订单取消\"},{\"code\":18,\"name\":\"售后单待发货\"}],\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectedResponse, responseString, true);

    }

    /**
     * t2-o1: 获取所有状态 - 未登入
     *
     * @author Han Li
     * Created at 25/11/2020 08:51
     * Created by Han Li at 25/11/2020 10:22
     */
    @Test
    public void getAllStatesNotLogon() throws Exception {
        String responseString = this.mvc.perform(
                get("/order/orders/states"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();

        String expectedResponse = "{\"errno\":401,\"errmsg\":\"需要登入才可使用\"}";
        JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * TODO - 测试 token 生成例程 (需要和其他模块沟通)
     *
     * @author 19720182203919 李涵
     * Created at 7/11/2020 21:54
     * @return 生成的 token
     */
    private String genTestToken() {
        // 这是一个乱写的 token (长度都是 271)
        return "xxyyeXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.haFuckHAJ0aGlzIGlzIGEgdG9rZW4iLCJhdWQiOiJNSU5JQVBQIiwidG9rZW5JZCI6IjIwMjAxMTA4MTA0MjQ5OUkyIiwiaXNzIjoiT09BRCIsImRlcGFydElkIjowLCJleHAiOjE2MDQ4MDMzNzAsInVzZXJJZCI6MSwiaWF0IjoxNjA0ODAzMzY5fQgg.12345678945621HAHAN_AXftnZLIvLW1gSHXQQtxixi";
    }
}
