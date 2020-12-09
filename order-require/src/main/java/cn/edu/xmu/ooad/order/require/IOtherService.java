package cn.edu.xmu.ooad.order.require;

import cn.edu.xmu.ooad.order.require.models.RegionInfo;

public interface IOtherService {

    /**
     * 用 Region ID 获取一个 Region
     * @param id regionId
     * @return RegionInfo
     */
    RegionInfo getRegion(Long id);

}
