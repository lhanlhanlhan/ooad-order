package cn.edu.xmu.ooad.other.required.model;
import java.io.Serializable;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponActivitySimple implements Serializable{
	private static final long serialVersionUID = 5342978673361353618L;
	private Integer id;
	private String name;
	private String beginTime;
	private String endTime;
	
}
