package cn.edu.xmu.oomall.order.annotations;

/**
 * 一旦实现该接口，表示可以由该对象创建对应 Vo 对象
 *
 * @author Han Li
 * Created at 25/11/2020 5:26 下午
 * Modified by Han Li at 25/11/2020 5:26 下午
 */
public interface VoCreatable {
    /**
     * 创建 Vo 对象
     * @return Vo 对象
     */
    Object createVo();
}
