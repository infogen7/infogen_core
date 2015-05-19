package com.infogen.aop.advice.event_handle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.infogen.aop.agent.Agent_Advice_Field;
import com.infogen.aop.agent.Agent_Advice_Method;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月29日 下午3:35:56
 * @since 1.0
 * @version 1.0
 */
public abstract class AOP_Handle {

	public abstract Agent_Advice_Method attach_method(String class_name, Method method, Annotation annotation);

	public abstract Agent_Advice_Field attach_field(String class_name, Field field, Annotation annotation);
}
