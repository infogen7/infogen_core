package com.infogen.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.infogen.agent.injection.InjectionMethod;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月29日 下午3:35:56
 * @since 1.0
 * @version 1.0
 */
public abstract class AnnotationImpl {
	public abstract InjectionMethod injection_method(String class_name, Method method, Annotation annotation);
}
