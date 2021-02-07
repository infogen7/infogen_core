package com.infogen.agent.advice;

import java.io.Serializable;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class Agent_Advice_Field implements Serializable {
	private static final long serialVersionUID = 4011923307122937602L;
	private String field_name;
	private String value;

	public String getField_name() {
		return field_name;
	}

	public void setField_name(String field_name) {
		this.field_name = field_name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}