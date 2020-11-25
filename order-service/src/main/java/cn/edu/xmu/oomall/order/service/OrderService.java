package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.annotations.LoginUser;
import cn.edu.xmu.oomall.order.annotations.SimpleVoCreatable;
import cn.edu.xmu.oomall.order.dao.OrderDao;
import cn.edu.xmu.oomall.order.model.bo.Order;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePo;
import cn.edu.xmu.oomall.order.model.vo.OrderSimpleVo;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import cn.edu.xmu.oomall.order.utils.ResponseUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务
 *
 * @author Han Li
 * Created at 25/11/2020 8:53 上午
 * Modified by Han Li at 25/11/2020 8:53 上午
 */
@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    /**
     * 服务 o1：获取用户名下所有订单概要 (分页)
     *
     * @author Han Li
     * Created at 25/11/2020 16:58
     * Created by Han Li at 25/11/2020 16:58
     * @param orderSn 订单号
     * @param state 状态码
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param pageSize 页大小
     * @param customerId 用户 id
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     */
    public APIReturnObject<PageInfo<OrderSimpleVo>> getPagedCustomerOrders(String orderSn, Byte state,
                                                                           String beginTime, String endTime,
                                                                           int page, int pageSize,
                                                                           Long customerId) {
        PageHelper.startPage(page, pageSize);
        // 调用 Dao 层
        PageInfo<OrderSimplePo> orderSimplePos = orderDao.getSimpleOrders(orderSn, state, beginTime, endTime, page, pageSize, customerId);
        // 转为业务对象列表
        List<OrderSimpleVo> orders =
                orderSimplePos.getList().stream()
                        .map(Order::new)
                        .filter(Order::isAuthentic)
                        .map(Order::createSimpleVo)
                        .collect(Collectors.toList());
        // 用 PageInfo 封装
        PageInfo<OrderSimpleVo> returnObj = new PageInfo<>(orders);
        returnObj.setPages(orderSimplePos.getPages());
        returnObj.setPageNum(orderSimplePos.getPageNum());
        returnObj.setPageSize(orderSimplePos.getPageSize());
        returnObj.setTotal(orderSimplePos.getTotal());
        // 返回
        return new APIReturnObject<>(returnObj);
    }

    /**
     * 服务 o2：获取用户名下所有订单概要 (不分页)
     *
     * @author Han Li
     * Created at 25/11/2020 16:58
     * Created by Han Li at 25/11/2020 16:58
     * @param orderSn 订单号
     * @param state 状态码
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param customerId 用户 id
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     */
    public APIReturnObject<List<OrderSimpleVo>> getCustomerOrders(String orderSn, Byte state,
                                                                  String beginTime, String endTime,
                                                                  Long customerId) {
        // 调用 Dao 层
        List<OrderSimplePo> orderSimplePos = orderDao.getSimpleOrders(orderSn, state, beginTime, endTime, customerId);
        // 转为业务对象列表
        List<OrderSimpleVo> orderVos =
                orderSimplePos.stream()
                        .map(Order::new)
                        .filter(Order::isAuthentic)
                        .map(Order::createSimpleVo)
                        .collect(Collectors.toList());
        // 封装返回体
        return new APIReturnObject<>(orderVos);
    }
}
