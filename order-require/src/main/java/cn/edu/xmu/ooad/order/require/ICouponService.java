package cn.edu.xmu.ooad.order.require;

import cn.edu.xmu.ooad.order.require.models.CouponActivityInfo;

public interface ICouponService {

    /**
     * 根据 优惠券 id、买家 id 获取优惠券并返回所绑定的优惠活动
     * @param couponId 优惠券 id
     * @param customerId 买家 id
     * @return 优惠活动资料，如果买家并不拥有此张优惠券，就返回 null
     */
    CouponActivityInfo getCoupon(Long customerId, Long couponId);


    /**
     * 根据 优惠活动 id 获取优惠活动
     *
     * @param couponActivityId 优惠活动 id
     * @return 优惠活动资料
     */
    CouponActivityInfo getCouponActivity(Long couponActivityId);

}
