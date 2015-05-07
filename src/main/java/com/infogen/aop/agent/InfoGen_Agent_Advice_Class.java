/**
 * 
 */
package com.infogen.aop.agent;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Agent_Advice_Class implements Serializable {
	private static final long serialVersionUID = -8990106273196548492L;
	private String class_name;
	private Set<InfoGen_Agent_Advice_Method> methods = new HashSet<>();
	private Set<InfoGen_Agent_Advice_Field> fields = new HashSet<>();

	public String getClass_name() {
		return class_name;
	}

	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}

	public Set<InfoGen_Agent_Advice_Method> getMethods() {
		return methods;
	}

	public void setMethods(Set<InfoGen_Agent_Advice_Method> methods) {
		this.methods = methods;
	}

	public Set<InfoGen_Agent_Advice_Field> getFields() {
		return fields;
	}

	public void setFields(Set<InfoGen_Agent_Advice_Field> fields) {
		this.fields = fields;
	}

}
