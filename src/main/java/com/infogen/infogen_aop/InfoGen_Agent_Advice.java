/**
 * 
 */
package com.infogen.infogen_aop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Agent_Advice implements Serializable {
	private static final long serialVersionUID = -8990106273196548492L;
	private String class_name;
	private List<InfoGen_Agent_Advice_Method> methods = new ArrayList<>();

	public String getClass_name() {
		return class_name;
	}

	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}

	public List<InfoGen_Agent_Advice_Method> getMethods() {
		return methods;
	}

	public void setMethods(List<InfoGen_Agent_Advice_Method> methods) {
		this.methods = methods;
	}

}
