package cn.edu.xmu.oomall.order.connector.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 买家用户信息
 *
 * @author Han Li
 * Created at 25/11/2020 9:34 上午
 * Modified by Han Li at 25/11/2020 9:34 上午
 */
@Data
@AllArgsConstructor
public class UserInfo {

    private Long id;

    private String username;

    private Short state;

}
