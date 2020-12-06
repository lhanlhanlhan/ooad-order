package cn.edu.xmu.ooad.order.interfaces;

/**
 * 一旦实现接口，表示该 Object 可以进行一致性检查 (防篡改)
 *
 * @author Han Li
 * Created at 25/11/2020 5:41 下午
 * Modified by Han Li at 25/11/2020 5:41 下午
 */
public interface AuthenticObject {

    /**
     * 判断该对象是否被篡改
     *
     * @return 是否被篡改，若被篡改，返回 false
     */
    boolean isAuthentic();

    /**
     * 获取对象的签名
     *
     * @return 对象的签名
     */
    String calcSignature();
}
