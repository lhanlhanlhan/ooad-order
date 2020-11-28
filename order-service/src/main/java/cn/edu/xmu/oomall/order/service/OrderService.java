package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.dao.OrderDao;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.model.bo.Order;
import cn.edu.xmu.oomall.order.model.po.OrderEditPo;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePo;
import cn.edu.xmu.oomall.order.model.vo.OrderEditVo;
import cn.edu.xmu.oomall.order.model.vo.OrderSimpleVo;
import cn.edu.xmu.oomall.order.model.vo.OrderVo;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    /**
     * 服务 o1：获取用户名下所有订单概要
     *
     * @param orderSn    订单号
     * @param state      状态码
     * @param beginTime  开始时间
     * @param endTime    结束时间
     * @param page       页码
     * @param pageSize   页大小
     * @param customerId 用户 id
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Han Li
     * Created at 25/11/2020 16:58
     * Created by Han Li at 25/11/2020 16:58
     */
    public APIReturnObject<?> getCustomerOrders(String orderSn, Byte state,
                                                String beginTime, String endTime,
                                                Integer page, Integer pageSize,
                                                Long customerId) {
        List<OrderSimpleVo> orders;
        Map<String, Object> returnObj = new HashMap<>();
        // 需要分页
        if (page != null && pageSize != null) {
            PageHelper.startPage(page, pageSize);
            // 调用 Dao 层
            APIReturnObject<PageInfo<OrderSimplePo>> returnObject = orderDao.getSimpleOrders(orderSn, state, beginTime, endTime, page, pageSize, customerId);
            if (returnObject.getCode() != ResponseCode.OK) {
                return returnObject;
            }
            PageInfo<OrderSimplePo> orderSimplePos = returnObject.getData();
            // 转为业务对象列表
            orders = orderSimplePos.getList().stream()
                    .map(Order::new)
                    .filter(Order::isAuthentic)
                    .map(Order::createSimpleVo)
                    .collect(Collectors.toList());
            // 用 Map 封装
            returnObj.put("page", orderSimplePos.getPageNum());
            returnObj.put("pageSize", orderSimplePos.getPageSize());
            returnObj.put("total", orderSimplePos.getTotal());
            returnObj.put("pages", orderSimplePos.getPages());
        }
        // 不必分页
        else {
            // 调用 Dao 层
            APIReturnObject<List<OrderSimplePo>> returnObject = orderDao.getSimpleOrders(orderSn, state, beginTime, endTime, customerId);
            if (returnObject.getCode() != ResponseCode.OK) {
                return returnObject;
            }
            List<OrderSimplePo> orderSimplePos = returnObject.getData();
            // 转为业务对象列表
            orders = orderSimplePos.stream()
                    .map(Order::new)
                    .filter(Order::isAuthentic)
                    .map(Order::createSimpleVo)
                    .collect(Collectors.toList());
            // 用 Map 封装
            returnObj.put("page", 1);
            returnObj.put("pageSize", orders.size());
            returnObj.put("total", orders.size());
            returnObj.put("pages", 1);
        }
        // 返回【标准返回】
        returnObj.put("list", orders);
        return new APIReturnObject<>(returnObj);
    }

    /**
     * 服务 o2：获取用户名下订单完整信息
     *
     * @author Han Li
     * Created at 26/11/2020 11:15
     * Created by Han Li at 26/11/2020 11:15
     * @param id 订单 id
     * @param customerId 用户 id
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<java.util.Map<java.lang.String,java.lang.Object>>
     */
    public APIReturnObject<?> getOrder(Long id, Long customerId) {
        // 获取订单 Bo、Vo
        APIReturnObject<Order> returnObject = orderDao.getOrder(id, customerId);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(returnObject.getCode(), returnObject.getErrMsg());
        }
        Order order = returnObject.getData();
        OrderVo vo = order.createVo();

        // 补充 Vo 的 Customer 信息：联系其他模块
        Map<String, Object> customer = userService.getCustomerInfo(id);
        vo.setCustomer(customer);
        // 补充 Vo 的 Shop 信息：联系商品模块
        Long shopId = order.getShopId();
        Map<String, Object> shop = shopService.getShopInfo(shopId);
        vo.setShop(shop);

        // 封装并返回【标准返回】
        return new APIReturnObject<>(vo);
    }


    /**
     * 服务 o3：买家修改订单信息
     *
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
     * @param id 订单号
     * @param customerId 消费者号
     * @param orderEditVo 修改信息对象
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<?>
     */
    @Transactional // 涉及到写操作的是一个事务
    public APIReturnObject<?> buyerModifyOrder(Long id, Long customerId, OrderEditVo orderEditVo) {
        // 查询订单，检查所有者、是否修改过、本来地址是否与新地址的地区一致
        APIReturnObject<OrderSimpleVo> returnObject = orderDao.getSimpleOrder(id, customerId);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(returnObject.getCode(), returnObject.getErrMsg());
        }

        // TODO - 检查是否修改过
        // TODO - 检查本来地址、新地址的地区一致性

        // 修改信息前的准备，mask 掉一些字段
        orderEditVo.setMessage(null);
        orderEditVo.setBeDeleted(null);

        // 修改信息
        return orderDao.modifyOrder(id, orderEditVo);
    }

    /**
     * 服务 o4：买家删掉订单
     *
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
     * @param id 订单号
     * @param customerId 消费者号
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<?>
     */
    @Transactional // 涉及到写操作的是一个事务
    public APIReturnObject<?> buyerDelOrder(Long id, Long customerId) {
        // 查询订单，检查所有者
        APIReturnObject<OrderSimpleVo> returnObject = orderDao.getSimpleOrder(id, customerId);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(returnObject.getCode(), returnObject.getErrMsg());
        }

        // 删除订单
        OrderEditVo delVo = new OrderEditVo();
        delVo.setBeDeleted(true);
        return orderDao.modifyOrder(id, delVo);
    }
}
