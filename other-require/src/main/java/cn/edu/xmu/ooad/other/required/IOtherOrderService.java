package cn.edu.xmu.ooad.other.required;

import cn.edu.xmu.ooad.other.required.model.OtherOrderItemComplex;
import cn.edu.xmu.ooad.other.required.model.OtherOrderItemSimple;
import cn.edu.xmu.ooad.other.required.model.OtherOrderSimple;

public interface IOtherOrderService {
	/**根据orderItemId获取orderItem相关信息，若资源不存在返回空
	 * @author chenqw
	 * @param orderItemId
	 * @return OrderItemSimple or null
	 */
	public OtherOrderItemSimple getOrderItemByOrderItemId(Long orderItemId);
	/**根据orderItemId获取orderItem相关信息和order信息，若资源不存在返回空
	 * 
	 * @param orderItemId
	 * @return
	 */
	public OtherOrderItemComplex getOrderItemComplexByOrderItemId(Long orderItemId);
	/**根据orderItemId获取全部orderItem信息和order信息，若资源不存在返回空
	 * @author chenqw
	 * @param orderItemId
	 * @return
	 */
	public OtherOrderSimple getOrderByOrderItemId(Long orderItemId);
}
