package cn.edu.xmu.ooad.order.order.model.bo.order.impl;

import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.centre.utils.Accessories;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.order.centre.utils.SpringUtils;
import cn.edu.xmu.ooad.order.order.dao.PaymentDao;
import cn.edu.xmu.ooad.order.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderType;
import cn.edu.xmu.ooad.order.order.enums.RefundStatus;
import cn.edu.xmu.ooad.order.order.model.bo.order.Order;
import cn.edu.xmu.ooad.order.order.model.bo.order.OrderItem;
import cn.edu.xmu.ooad.order.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderSimplePo;
import cn.edu.xmu.ooad.order.order.model.po.PaymentPo;
import cn.edu.xmu.ooad.order.order.model.po.RefundPo;
import cn.edu.xmu.ooad.order.require.IShopService;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.*;

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
        IShopService iShopService = SpringUtils.getBean(IShopService.class);
        // 获取所有店铺的订购商品
        Map<Long, List<OrderItem>> shopsItemLists = new HashMap<>();
        Map<Long, Long> shopsOrigPriceList = new HashMap<>();
        Map<Long, Long> shopsDiscountList = new HashMap<>();
        Map<Long, Long> shopsFreightList = new HashMap<>();
        // 运费平摊，就按照每个店铺付款金额平摊吧
        // 获取单位运费
        long unitFreight = this.getFreightPrice() / (this.getOriginPrice() - this.getDiscountPrice());
        for (OrderItem item : this.orderItemList) {
            // 获取商品的信息
            Long skuId = item.getSkuId();
            SkuInfo skuInfo = iShopService.getSkuInfo(skuId);
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
            orderPo.setFreightPrice(shopsFreightList.get(shopId)); // 运费怎么平摊？ mingqiu 说按照实付金额分
            // 订单种类为普通订单，订单状态为已支付 (已支付才能分单)
            orderPo.setOrderType(OrderType.NORMAL.getCode());
            orderPo.setState(OrderChildStatus.PAID.getCode()); // 普通订单没有 subState
            orderPo.setGmtCreate(nowTime);
            orderPo.setGmtModified(nowTime);
            orderPo.setOrderSn(Accessories.genSerialNumber());

            // 生成一笔 Order
            NormalOrder order = new NormalOrder(orderPo);
            // 無語，差點忘記把 item 放進去了媽的
            order.setOrderItemList(shopOrderItems);
            orderList.add(order);
        });

        return orderList;
    }

    /**
     * 判断该订单要支付的金额
     * <p>
     * 返回：-1：内部错误；>= 0：应付金额
     */
    @Override
    public long shallPayPrice() {
        if (this.getState() != OrderStatus.PENDING_PAY) {
            return 0L;
        }
        // 获取已支付之金额
        PaymentDao paymentDao = SpringUtils.getBean(PaymentDao.class);
        // 获取该订单上的所有支付单
        List<PaymentPo> poList = paymentDao.getPaymentByOrderId(this.getId());
        if (poList == null) {
            // 数据库错误
            return -1L;
        }
        // 获取已支付之金额
        long paidPrice = poList
                .stream()
                .mapToLong(PaymentPo::getAmount)
                .sum();
        // 获得总共需付款 (总共需付款 = 訂單總價+運費-優惠)
        long allShallPayPrice = getOriginPrice() +
                getFreightPrice() -
                getDiscountPrice();

        return allShallPayPrice - paidPrice;
    }

    /**
     * 判断订单是否可被修改
     */
    @Override
    public boolean canModify() {
        // 只有「未发货」才能让客户修改
        OrderChildStatus subState = this.getSubstate();
        return subState == OrderChildStatus.PAID;
    }

    /**
     * 判断该订单是否可被删除
     */
    @Override
    public boolean canDelete() {
        return (this.getState() == OrderStatus.CANCELLED || this.getState() == OrderStatus.DONE);
    }

    /**
     * 判断该 订单 是否可被客户取消
     */
    @Override
    public boolean canCustomerCancel() {
        // 只有未支付的才能被客户取消
        return this.getState() == OrderStatus.PENDING_PAY;
    }

    /**
     * 判断该 订单 是否可被商户取消
     */
    @Override
    public boolean canShopCancel() {
        // 只有未支付的才能被商户取消
        return this.getState() == OrderStatus.PENDING_PAY;
    }

    /**
     * 判断该 订单 是否可被签收
     */
    @Override
    public boolean canSign() {
        // 只有订单状态为「已发货」的可以签收
        return getSubstate() == OrderChildStatus.SHIPPED;
    }

    /**
     * 判断该 订单 是否可被发货
     */
    @Override
    public boolean canDeliver() {
        // 只有「已付款完成 (已成团)」的团购订单，才能被发货
        return getSubstate() == OrderChildStatus.PAID;
    }

    @Override
    public void triggerPaid() {
        // 订单状态改成已支付
        this.setState(OrderStatus.PENDING_RECEIVE);
        this.setSubstate(OrderChildStatus.PAID);
    }

    @Override
    public int triggerCancelled() {
        // 根据订单的付款单创建退款单
        PaymentDao paymentDao = SpringUtils.getBean(PaymentDao.class);
        // 获取该订单上的所有支付单
        List<PaymentPo> poList = paymentDao.getPaymentByOrderId(this.getId());
        if (poList == null) {
            // 数据库错误
            return -1;
        }
        // 依次创建退款单
        for (PaymentPo paymentPo : poList) {
            // 获取售后单ID
            Long aftersaleId = paymentPo.getAftersaleId();
            // 新建 refund PO对象
            RefundPo refundPo = new RefundPo();
            refundPo.setPaymentId(paymentPo.getId());
            refundPo.setOrderId(this.getId()); // 此订单的 id
            refundPo.setAftersaleId(aftersaleId);
            // 模拟支付环境的都是已经退款
            refundPo.setState(RefundStatus.ALREADY_REFUND.getCode());
            refundPo.setAmount(paymentPo.getAmount());
            refundPo.setGmtCreate(LocalDateTime.now());
            refundPo.setGmtModified(LocalDateTime.now());
            // TODO - 如果是返点支付，应该把返点回充至账户中
            // 将退款单Po对象插入数据库
            try {
                int response = paymentDao.addRefund(refundPo);
                if (response <= 0) {
                    System.err.println("自动下退款单失败！orderId=" + this.getId());
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return 1;
            }
        }
        return 0;
    }
}
