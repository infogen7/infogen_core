/**
 * 
 */
package com.infogen.agent.injection;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class InjectionClass implements Serializable {
	private static final long serialVersionUID = -8990106273196548492L;
	private String class_name;
	private Set<InjectionMethod> methods = new HashSet<>();
	private Set<InjectionField> fields = new HashSet<>();

	public String getClass_name() {
		return class_name;
	}

	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}

	public Set<InjectionMethod> getMethods() {
		return methods;
	}

	public void setMethods(Set<InjectionMethod> methods) {
		this.methods = methods;
	}

	public Set<InjectionField> getFields() {
		return fields;
	}

	public void setFields(Set<InjectionField> fields) {
		this.fields = fields;
	}

}
