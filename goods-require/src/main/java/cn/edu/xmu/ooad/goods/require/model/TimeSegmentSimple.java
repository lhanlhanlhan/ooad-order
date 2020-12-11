package cn.edu.xmu.ooad.goods.require.model;

import lombok.Data;

import java.io.Serializable;
import java.sql.Time;

@Data
public class TimeSegmentSimple implements Serializable {

    private Long id;
    private String beginTime;
    private String endTime;
    private String gmtCreate;
    private String gmtModified;

    public TimeSegmentSimple(){}

    public TimeSegmentSimple(Long id, String beginTime, String endTime, String gmtCreate , String gmtModified)
    {
        this.id = id;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.gmtCreate = gmtCreate;
        this.gmtModified = gmtModified;
    }


}
