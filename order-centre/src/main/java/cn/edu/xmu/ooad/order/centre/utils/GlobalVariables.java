package cn.edu.xmu.ooad.order.centre.utils;

import cn.edu.xmu.ooad.order.require.models.RegionInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局变量
 */
public class GlobalVariables {
    public static Map<Long, RegionInfo> regionInfoMap = null;

    public static void setRegionInfoList(List<RegionInfo> infoList) {
        if (infoList != null) {
            regionInfoMap = new HashMap<>();
            infoList.forEach(regionInfo -> regionInfoMap.put(regionInfo.getId(), regionInfo));
        }
    }
}
