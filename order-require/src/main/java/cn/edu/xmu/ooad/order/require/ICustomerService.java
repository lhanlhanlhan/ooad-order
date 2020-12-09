package cn.edu.xmu.ooad.order.require;

import cn.edu.xmu.ooad.order.require.models.CustomerInfo;

public interface ICustomerService {

    /**
     * 获取顾客用户资料
     *
     * @param userId 用户 Id
     * @return cn.edu.xmu.ooad.order.require.models.CustomerInfo
     */
    CustomerInfo getCustomerInfo(Long userId);
}
