package com.infogen.http_idl;

import java.io.Serializable;

import lombok.Data;

/**
 * HTTP协议返回值封装
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
@Data
public class Result<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer code;
	private String message;
	private T data;

	public Result(Integer code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	//////////////////////////////// create//////////////////////////////////
	public static <T> Result<T> success(T data) {
		return new Result<T>(CODE.success.code, "", data);
	}

	public static <T> Result<T> fail(String message) {
		return new Result<T>(CODE.error.code, message, null);
	}

	public Boolean isSuccess() {
		return this.code == CODE.success.code;
	}
}
