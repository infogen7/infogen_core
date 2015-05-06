/**
 * 
 */
package com.infogen.aop.agent;

import java.io.Serializable;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Agent_Advice_Method implements Serializable {
	private static final long serialVersionUID = -57940195739144408L;
	private String method_name;
	private String long_local_variable;
	private String insert_before;
	private String insert_after;
	private String add_catch;
	private String annotation;

	public String getMethod_name() {
		return method_name;
	}

	public void setMethod_name(String method_name) {
		this.method_name = method_name;
	}

	public String getLong_local_variable() {
		return long_local_variable;
	}

	public void setLong_local_variable(String long_local_variable) {
		this.long_local_variable = long_local_variable;
	}

	public String getInsert_before() {
		return insert_before;
	}

	public void setInsert_before(String insert_before) {
		this.insert_before = insert_before;
	}

	public String getInsert_after() {
		return insert_after;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public void setInsert_after(String insert_after) {
		this.insert_after = insert_after;
	}

	public String getAdd_catch() {
		return add_catch;
	}

	public void setAdd_catch(String add_catch) {
		this.add_catch = add_catch;
	}

}