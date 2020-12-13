package cn.edu.xmu.ooad.order.interfaces;

/**
 * 一旦实现该接口，表示可以由该对象创建对应的 SimpleVo (概要) 对象
 *
 * @author Han Li
 * Created at 25/11/2020 5:27 下午
 * Modified by Han Li at 25/11/2020 5:27 下午
 */
public interface SimpleVoCreatable {
    /**
     * 创建概要 Vo 对象
     *
     * @return 概要 Vo 对象
     */
    Object createSimpleVo();
}
