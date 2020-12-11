package cn.edu.xmu.ooad.goods.require.model;

import lombok.Data;

import java.io.Serializable;


@Data
public class FreightModelSimple implements Serializable {

    private Long id;
    private Integer unit;
    private String name;
    private Byte type;
    private Byte defaultModel;
    private String gmtCreate;
    private String gmtModified;

    public FreightModelSimple(){}

    public FreightModelSimple(Long id, Integer unit, String name, Byte type, Byte defaultModel, String gmtCreate, String gmtModified)
    {
        this.id = id;
        this.unit = unit;
        this.name = name;
        this.type = type;
        this.defaultModel = defaultModel;
        this.gmtCreate = gmtCreate;
        this.gmtModified = gmtModified;
    }


}
