package com.infogen.tracking.annotation_handle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.infogen.agent.injection.InjectionMethod;
import com.infogen.aop.AnnotationHandle;
import com.infogen.tracking.annotation.Execution;

import lombok.extern.slf4j.Slf4j;

/**
 * 统计方法执行时间和调用链记录的处理器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 下午6:11:09
 * @since 1.0
 * @version 1.0
 */
@Slf4j
public class InfoGenExecutionHandle extends AnnotationHandle {

	@Override
	public InjectionMethod injection_method(String class_name, Method method, Annotation annotation) {
		String method_name = method.getName();

		Class<?>[] parameterTypes = method.getParameterTypes();
		StringBuilder stringbuilder = new StringBuilder();
		for (Class<?> type : parameterTypes) {
			stringbuilder.append(type.getName()).append(" ");
		}
		String full_method_name = stringbuilder.toString();

		String user_definition = ((Execution) annotation).user_definition();
		if (user_definition.contains(",")) {
			user_definition.replaceAll(",", "|");
			log.warn("注解Execution中user_definition字段不能出现 ',' 将被替换成 '|' ".concat(class_name).concat(".").concat(method_name));
		}

		InjectionMethod advice_method = new InjectionMethod();
		advice_method.setMethod_name(method_name);
		advice_method.setLong_local_variable("infogen_logger_injection_start_millis");

		StringBuilder insert_before = new StringBuilder();
		insert_before.append("infogen_logger_injection_start_millis = System.currentTimeMillis();");
		advice_method.setInsert_before(insert_before.toString());

		StringBuilder insert_after = new StringBuilder();
		insert_after.append("com.infogen.tracking.event_handle.InfoGenExecutionHandle.insert_after_call_back(");
		insert_after.append("\"").append(class_name).append("\"").append(",");
		insert_after.append("\"").append(method_name).append("\"").append(",");
		insert_after.append("\"").append(user_definition).append("\"").append(",");
		insert_after.append("\"").append(full_method_name).append("\"").append(",");
		insert_after.append("infogen_logger_injection_start_millis, System.currentTimeMillis(),$_);");
		advice_method.setInsert_after(insert_after.toString());

		StringBuilder add_catch = new StringBuilder();
		add_catch.append("com.infogen.tracking.event_handle.InfoGenExecutionHandle.add_catch_call_back(");
		add_catch.append("\"").append(class_name).append("\"").append(",");
		add_catch.append("\"").append(method_name).append("\"").append(",");
		add_catch.append("\"").append(user_definition).append("\"").append(",");
		add_catch.append("\"").append(full_method_name).append("\"").append(",");
		add_catch.append("$e);throw $e;");
		advice_method.setAdd_catch(add_catch.toString());

		return advice_method;
	}

	public InfoGenExecutionHandle(TrackingHandle handle) {
		super();
		InfoGenExecutionHandle.tracking_handle = handle;
	}

	private static TrackingHandle tracking_handle = null;

	public static void insert_after_call_back(String class_name, String method_name, String user_definition, String full_method_name, long start_millis, long end_millis, Object return0) {
		if (tracking_handle != null) {
			tracking_handle.insert_after_call_back(class_name, method_name, user_definition, full_method_name, start_millis, end_millis, return0);
		}
	}

	public static void add_catch_call_back(String class_name, String method_name, String user_definition, String full_method_name, Throwable e) {
		if (tracking_handle != null) {
			tracking_handle.add_catch_call_back(class_name, method_name, user_definition, full_method_name, e);
		}
	}
}
