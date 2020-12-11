package cn.edu.xmu.ooad.goods.require;

import cn.edu.xmu.ooad.goods.require.model.TimeSegmentSimple;
import java.time.LocalDateTime;

public interface ITimeSegmentService {

    /**
     * 根据时间段id 获取一个 时间段的具体信息
     */
    TimeSegmentSimple getTimeSegment(Long timeSegmentId);

    /**
     * 根据时间段id 获取一个 时间段是否为秒杀时段 1表示为秒杀时段
     */
    Boolean isTimeSegment(Long timeSegmentId);

    /**
     * 根据当前的时间段 返回一个秒杀时段的id 没有返回空
     */
    Long getTimeSegmentByNow(LocalDateTime now);
}
