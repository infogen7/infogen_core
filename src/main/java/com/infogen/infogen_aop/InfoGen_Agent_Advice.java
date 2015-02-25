/**
 * 
 */
package com.infogen.infogen_aop;

import java.io.Serializable;

/**
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2015年2月11日 上午10:59:45
 */
public class InfoGen_Agent_Advice implements Serializable {
	private static final long serialVersionUID = -8990106273196548492L;
	private String class_name;
	private String method_name;
	private String long_local_variable;
	private String insert_before;
	private String insert_after;
	private String add_catch;

	public String getClass_name() {
		return class_name;
	}

	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}

	public String getMethod_name() {
		return method_name;
	}

	public void setMethod_name(String method_name) {
		this.method_name = method_name;
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

	public void setInsert_after(String insert_after) {
		this.insert_after = insert_after;
	}

	public String getAdd_catch() {
		return add_catch;
	}

	public void setAdd_catch(String add_catch) {
		this.add_catch = add_catch;
	}

	public String getLong_local_variable() {
		return long_local_variable;
	}

	public void setLong_local_variable(String long_local_variable) {
		this.long_local_variable = long_local_variable;
	}

}
