package cn.edu.xmu.ooad.order.model.bo;

import cn.edu.xmu.ooad.order.connector.service.ShopService;
import cn.edu.xmu.ooad.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.enums.OrderType;
import cn.edu.xmu.ooad.order.interfaces.SimpleVoCreatable;
import cn.edu.xmu.ooad.order.interfaces.VoCreatable;
import cn.edu.xmu.ooad.order.model.po.OrderItemPo;
import cn.edu.xmu.ooad.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.model.po.OrderSimplePo;
import cn.edu.xmu.ooad.order.model.vo.OrderSimpleVo;
import cn.edu.xmu.ooad.order.model.vo.OrderVo;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import cn.edu.xmu.ooad.order.utils.Accessories;
import cn.edu.xmu.ooad.order.utils.SpringUtils;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单业务对象
 *
 * @author Han Li
 * Created at 25/11/2020 4:44 下午
 * Modified by Han Li at 25/11/2020 4:44 下午
 */
@Data
public class Order implements VoCreatable, SimpleVoCreatable {

    private List<OrderItem> orderItemList;

    // 概要业务对象 【代理】
    private OrderSimplePo orderSimplePo = null;
    // 完整业务对象 【代理】
    private OrderPo orderPo = null;

    /**
     * 创建概要业务对象
     *
     * @param orderSimplePo SimplePo 对象
     */
    public Order(OrderSimplePo orderSimplePo) {
        this.orderSimplePo = orderSimplePo;
    }

    /**
     * 创建完整业务对象
     *
     * @param orderPo Po 对象
     * @author Han Li
     * Created at 26/11/2020 15:31
     * Created by Han Li at 26/11/2020 15:31
     */
    public Order(OrderPo orderPo) {
        this.orderPo = orderPo;

        // 把 orderItemList 中的每个 Item 转换成 bo 对象 (如有)
        if (orderPo.getOrderItemList() != null) {
            this.orderItemList =
                    orderPo.getOrderItemList()
                            .stream()
                            .map(OrderItem::new)
                            .collect(Collectors.toList());
        }
    }

    /**
     * 创建概要 Vo 对象
     *
     * @return 概要 Vo 对象
     */
    @Override
    public OrderSimpleVo createSimpleVo() {
        return new OrderSimpleVo(this);
    }

    /**
     * 创建 Vo 对象
     *
     * @return Vo 对象
     */
    @Override
    public OrderVo createVo() {
        return new OrderVo(this);
    }

    /**
     * 支付成功后调用分单，分成若干个完整订单【每个订单内含 orderItemList，可以直接插入】
     *
     * @return 如果分单成功，返回 List；如果无需分单，返回 null
     */
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
            Order order = new Order(orderPo);
            // 無語，差點忘記把 item 放進去了媽的
            order.setOrderItemList(orderItems);
            orderList.add(order);
        });

        return orderList;
    }


    /*
    订单状态的一些判定
     */

    /**
     * 判断该订单可否被支付
     */
    public boolean canPay() {
        if (this.getOrderType() == OrderType.PRE_SALE) {
            // 预售订单，只能主状态为待支付 && 从状态为待支付首/尾款的才可以支付
            return this.getState() == OrderStatus.PENDING_PAY.getCode() &&
                    (this.getSubstate() == OrderStatus.PENDING_DEPOSIT.getCode() ||
                            this.getSubstate() == OrderStatus.PENDING_REM_BALANCE.getCode());
        } else {
            // 普通/团购订单，主状态为待支付即可支付
            return this.getState() == OrderStatus.PENDING_PAY.getCode();
        }
    }

    /**
     * 判断订单是否可被修改
     */
    public boolean canModify() {
        // 订单状态为空，不给修改
        if (this.getState() == null) {
            return false;
        }
        OrderStatus status = OrderStatus.getByCode(this.getState());
        // 订单状态非法，不给修改
        if (status == null) {
            return false;
        }
        // 只有「未发货」才能让客户修改
        return status == OrderStatus.PAID;
    }

    /**
     * 判断该订单是否可被删除
     */
    public boolean canDelete() {
        // 订单状态为空，不给删除
        if (this.getState() == null) {
            return false;
        }
        OrderStatus status = OrderStatus.getByCode(this.getState());
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
    public boolean canCustomerCancel() {
        // 订单状态为空，不给取消
        if (this.getState() == null) {
            return false;
        }
        OrderStatus status = OrderStatus.getByCode(this.getState());
        // 订单状态非法，不给不给取消
        if (status == null) {
            return false;
        }

        // 只有未支付的才能被客户取消
        return status == OrderStatus.PENDING_PAY;
        /*
        switch (status) {
            case PENDING_DEPOSIT:
            case PENDING_PAY:
            case PENDING_GROUP:
            case DEPOSIT_PAID:
            case PENDING_REM_BALANCE:
                return true;
            default:
                return false;
        }
         */
    }

    /**
     * 判断该 订单 是否可被商户取消
     */
    public boolean canShopCancel() {
        // 订单状态为空，不给取消
        if (this.getState() == null) {
            return false;
        }
        OrderStatus status = OrderStatus.getByCode(this.getState());
        // 订单状态非法，不给取消
        if (status == null) {
            return false;
        }
        // 只有未支付的才能被商户取消
        return status == OrderStatus.PENDING_PAY;
        /*
        switch (status) {
            case PENDING_DEPOSIT:
            case PENDING_PAY:
            case PENDING_GROUP:
                return true;
            default:
                return false;
        }
        */
    }

    /**
     * 判断该 订单 是否可被签收
     */
    public boolean canSign() {
        // 订单状态为空，不给签收
        if (this.getState() == null) {
            return false;
        }
        OrderStatus status = OrderStatus.getByCode(this.getState());
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
    public boolean canDeliver() {
        // 订单状态为空，不给发货
        if (this.getState() == null) {
            return false;
        }
        OrderStatus status = OrderStatus.getByCode(this.getState());
        // 订单状态非法，不给发货
        if (status == null) {
            return false;
        }
        // 只有已支付的普通/预售订单，以及「已成团」的团购订单，才能被发货
        if (status == OrderStatus.PAID) {
            OrderType type = getOrderType();
            switch (type) {
                case NORMAL:
                case PRE_SALE:
                    return true;
                default:
                    // 团购订单，还需要已成团
                    return getSubstate() == OrderStatus.GROUPED.getCode();
            }
        } else {
            return false;
        }
        /*
        switch (status) {
            case REM_BALANCE_PAID:
            case PAID:
            case GROUP_FORMED:
                return true;
            default:
                return false;
        }
        */
    }

    /**
     * 判断该 订单 是否可被从团购转为普通订单
     */
    public boolean canCustomerChangeFromGrouponToNormal() {
        // 订单状态为空，不给转换
        if (this.getState() == null) {
            return false;
        }
        OrderStatus subState = OrderStatus.getByCode(this.getSubstate());
        // 订单状态非法，不给转换
        if (subState == null) {
            return false;
        }
        OrderType type = this.getOrderType();
        // 订单类型为空，不给转换
        if (type == null) {
            return false;
        }

        // 只有订单类型为团购、订单状态为「未到达门槛」的可以改成普通订单
        return type == OrderType.GROUPON && subState == OrderStatus.GROUP_FAILED;
    }


    /**
     * Getters
     */

    public Long getId() {
        return orderPo == null ? orderSimplePo.getId() : orderPo.getId();
    }

    public Long getCustomerId() {
        return orderPo == null ? orderSimplePo.getCustomerId() : orderPo.getCustomerId();
    }

    public Long getShopId() {
        return orderPo == null ? orderSimplePo.getShopId() : orderPo.getShopId();
    }

    public String getOrderSn() {
        return orderPo == null ? orderSimplePo.getOrderSn() : orderPo.getOrderSn();
    }

    public Long getPid() {
        return orderPo == null ? orderSimplePo.getPid() : orderPo.getPid();
    }

    public String getConsignee() {
        return orderPo == null ? null : orderPo.getConsignee();
    }

    public Long getRegionId() {
        return orderPo == null ? null : orderPo.getRegionId();
    }

    public String getAddress() {
        return orderPo == null ? null : orderPo.getAddress();
    }

    public String getMobile() {
        return orderPo == null ? null : orderPo.getMobile();
    }

    public String getMessage() {
        return orderPo == null ? null : orderPo.getMessage();
    }

    public OrderType getOrderType() {
        return orderPo == null
                ? OrderType.getTypeFromCode(orderSimplePo.getOrderType())
                : OrderType.getTypeFromCode(orderPo.getOrderType());
    }

    public Long getFreightPrice() {
        return orderPo == null ? orderSimplePo.getFreightPrice() : orderPo.getFreightPrice();
    }

    public Long getCouponId() {
        return orderPo == null ? null : orderPo.getCouponId();
    }

    public Long getCouponActivityId() {
        return orderPo == null ? null : orderPo.getCouponActivityId();
    }

    public Long getDiscountPrice() {
        return orderPo == null ? orderSimplePo.getDiscountPrice() : orderPo.getDiscountPrice();
    }

    public Long getOriginPrice() {
        return orderPo == null ? orderSimplePo.getOriginPrice() : orderPo.getOriginPrice();
    }

    public Long getPresaleId() {
        return orderPo == null ? null : orderPo.getPresaleId();
    }

    public Long getGrouponId() {
        return orderPo == null ? null : orderPo.getGrouponId();
    }

    public Long getGrouponDiscount() {
        return orderPo == null ? null : orderPo.getGrouponDiscount();
    }

    public Integer getRebateNum() {
        return orderPo == null ? null : orderPo.getRebateNum();
    }

    public LocalDateTime getConfirmTime() {
        return orderPo == null ? null : orderPo.getConfirmTime();
    }

    public String getShipmentSn() {
        return orderPo == null ? null : orderPo.getShipmentSn();
    }

    public Byte getState() {
        return orderPo == null ? orderSimplePo.getState() : orderPo.getState();
    }

    public Byte getSubstate() {
        return orderPo == null ? orderSimplePo.getSubstate() : orderPo.getSubstate();
    }

    public Byte getBeDeleted() {
        return orderPo == null ? null : orderPo.getBeDeleted();
    }

    public LocalDateTime getGmtCreated() {
        return orderPo == null ? orderSimplePo.getGmtCreate() : orderPo.getGmtCreate();
    }

    public LocalDateTime getGmtModified() {
        return orderPo == null ? null : orderPo.getGmtModified();
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public OrderPo getOrderPo() {
        return orderPo;
    }
}
