package cn.edu.xmu.ooad.order.model.bo;

import cn.edu.xmu.ooad.order.connector.service.ShopService;
import cn.edu.xmu.ooad.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.enums.OrderType;
import cn.edu.xmu.ooad.order.model.po.OrderItemPo;
import cn.edu.xmu.ooad.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.model.po.OrderSimplePo;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import cn.edu.xmu.ooad.order.utils.Accessories;
import cn.edu.xmu.ooad.order.utils.SpringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Han Li
 * Created at 11/12/2020 10:09 上午
 * Modified by Han Li at 11/12/2020 10:09 上午
 */
public class NormalOrder extends Order {
    /**
     * 创建完整业务对象
     *
     * @param orderPo Po 对象
     * @author Han Li
     * Created at 26/11/2020 15:31
     * Created by Han Li at 26/11/2020 15:31
     */
    public NormalOrder(OrderPo orderPo) {
        super(orderPo);
    }

    /**
     * 创建概要业务对象
     *
     * @param orderSimplePo SimplePo 对象
     */
    public NormalOrder(OrderSimplePo orderSimplePo) {
        super(orderSimplePo);
    }

    /**
     * 支付成功后调用分单，分成若干个完整订单【每个订单内含 orderItemList，可以直接插入】
     *
     * @return 如果分单成功，返回 List；如果无需分单，返回 null
     */
    @Override
    public List<Order> splitToOrders() {
        if (this.getShopId() != null) {
            return null;
        }
        // 获取 ShopService
        ShopService shopService = SpringUtils.getBean(ShopService.class);
        // 获取所有店铺的订购商品
        Map<Long, List<OrderItem>> shopsItemLists = new HashMap<>();
        Map<Long, Long> shopsOrigPriceList = new HashMap<>();
        Map<Long, Long> shopsDiscountList = new HashMap<>();
        Map<Long, Long> shopsFreightList = new HashMap<>();
        // TODO - 运费平摊我还不是很会也，就按照每个店铺付款金额平摊吧。
        // 获取单位运费
        long unitFreight = this.getFreightPrice() / (this.getOriginPrice() - this.getDiscountPrice());
        for (OrderItem item : this.orderItemList) {
            // 获取商品的信息
            Long skuId = item.getSkuId();
            SkuInfo skuInfo = shopService.getSkuInfo(skuId);
            Long shopId = skuInfo.getShopId();

            // 检查这个店铺的订购列表有没有创建，没有的话，就新建并放入一个该店铺的新列表
            List<OrderItem> thisShopItems = shopsItemLists.computeIfAbsent(shopId, k -> new LinkedList<>());
            Long thisShopOrigPrice = shopsOrigPriceList.get(shopId);
            Long thisShopDiscount = shopsDiscountList.get(shopId);
            long thisShopFreight;
            if (thisShopOrigPrice == null) {
                thisShopOrigPrice = 0L;
                thisShopDiscount = 0L;
            }
            // 商品放入店铺购买列表中，计算价格
            thisShopItems.add(item);
            thisShopOrigPrice += item.getPrice() * item.getQuantity();
            thisShopDiscount += item.getDiscount();
            thisShopFreight = unitFreight * (thisShopOrigPrice - thisShopDiscount);
            // 并记录实付金额
            shopsOrigPriceList.put(shopId, thisShopOrigPrice);
            shopsDiscountList.put(shopId, thisShopDiscount);
            shopsFreightList.put(shopId, thisShopFreight);
        }
        // 现在，已经获得在各店铺购买的物品，及各店铺获得的 money，可以分单
        LocalDateTime nowTime = LocalDateTime.now();
        ArrayList<Order> orderList = new ArrayList<>(shopsItemLists.size());
        shopsItemLists.forEach((shopId, shopOrderItems) -> {
            // 把 shopOrderItems 中的各对象转为有 OrderItemPo 的 OrderItem
            List<OrderItem> orderItems = shopOrderItems
                    .stream()
                    .map(item -> {
                        OrderItemPo orderItemPo = new OrderItemPo();
                        // 设置 po 的各项目
                        orderItemPo.setId(item.getId()); // 是將orderItem軟連結過去
                        orderItemPo.setGoodsSkuId(item.getSkuId());
                        orderItemPo.setQuantity(item.getQuantity());
                        orderItemPo.setPrice(item.getPrice());
                        orderItemPo.setDiscount(item.getDiscount());
                        orderItemPo.setCouponActivityId(item.getCouponActId());
                        orderItemPo.setBeShareId(item.getBeSharedId());
                        orderItemPo.setName(item.getName()); // 下单时的名字
                        orderItemPo.setGmtCreate(nowTime);
                        return orderItemPo;
                    })
                    .map(OrderItem::new)
                    .collect(Collectors.toList());

            // 生成一笔 OrderPo
            OrderPo orderPo = new OrderPo();
            // 可以从父订单拿到的资料
            orderPo.setCustomerId(getCustomerId());
            orderPo.setRegionId(getRegionId());
            orderPo.setAddress(getAddress());
            orderPo.setMobile(getMobile());
            orderPo.setMessage(getMessage());
            orderPo.setConsignee(getConsignee());
            // 本商店订单对应的资料
            orderPo.setPid(this.getId()); // 父訂單號
            orderPo.setShopId(shopId); // 本商店 Id
            orderPo.setOriginPrice(shopsOrigPriceList.get(shopId));
            orderPo.setDiscountPrice(shopsDiscountList.get(shopId));
            orderPo.setFreightPrice(shopsFreightList.get(shopId)); // TODO - 运费怎么平摊？
            // 订单种类为普通订单，订单状态为已支付 (已支付才能分单)
            orderPo.setOrderType(OrderType.NORMAL.getCode());
            orderPo.setState(OrderStatus.PAID.getCode()); // 普通订单没有 subState
            orderPo.setGmtCreate(nowTime);
            orderPo.setOrderSn(Accessories.genSerialNumber());

            // 生成一笔 Order
            NormalOrder order = new NormalOrder(orderPo);
            // 無語，差點忘記把 item 放進去了媽的
            order.setOrderItemList(orderItems);
            orderList.add(order);
        });

        return orderList;
    }

    /**
     * 判断该订单可否被支付
     */
    @Override
    public boolean canPay() {
        return this.getState() == OrderStatus.PENDING_PAY;
    }

    /**
     * 判断订单是否可被修改
     */
    @Override
    public boolean canModify() {
        // 只有「未发货」才能让客户修改
        return this.getState() == OrderStatus.PAID;
    }

    /**
     * 判断该订单是否可被删除
     */
    @Override
    public boolean canDelete() {
        OrderStatus status = this.getState();
        // 订单状态非法，不给删除
        if (status == null) {
            return false;
        }
        // 只有已签收 or 已取消 or 已退款 or 订单终止 or 预售终止的才让删除
        switch (status) {
            case SIGNED:
            case REFUNDED:
            case TERMINATED:
            case PRE_SALE_TERMINATED:
            case CANCELLED:
                return true;
            default:
                return false;
        }
    }

    /**
     * 判断该 订单 是否可被客户取消
     */
    @Override
    public boolean canCustomerCancel() {
        OrderStatus status = this.getState();
        // 订单状态非法，不给不给取消
        if (status == null) {
            return false;
        }

        // 只有未支付的才能被客户取消
        return status == OrderStatus.PENDING_PAY;
    }

    /**
     * 判断该 订单 是否可被商户取消
     */
    @Override
    public boolean canShopCancel() {
        OrderStatus status = this.getState();
        // 订单状态非法，不给取消
        if (status == null) {
            return false;
        }
        // 只有未支付的才能被商户取消
        return status == OrderStatus.PENDING_PAY;
    }

    /**
     * 判断该 订单 是否可被签收
     */
    @Override
    public boolean canSign() {
        OrderStatus status = this.getState();
        // 订单状态非法，不给签收
        if (status == null) {
            return false;
        }
        // 只有订单状态为「已到货」的可以签收
        return status == OrderStatus.REACHED;
    }

    /**
     * 判断该 订单 是否可被发货
     */
    @Override
    public boolean canDeliver() {
        OrderStatus status = this.getState();
        // 订单状态非法，不给发货
        if (status == null) {
            return false;
        }
        return status == OrderStatus.PAID;
    }
}