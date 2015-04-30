/**
 * 
 */
package com.infogen.aop.autowired;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.infogen.aop.advice.event_handle.InfoGen_AOP_Handle;
import com.infogen.aop.agent.InfoGen_Agent_Advice_Class;
import com.infogen.aop.agent.InfoGen_Agent_Advice_Method;
import com.infogen.aop.agent.InfoGen_Agent_Cache;
import com.infogen.aop.tools.Tool_Jackson;

/**
 * 通过附着agent的方式实现面向切面的功能
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2015年2月11日 下午5:32:23
 * @param <T>
 */
public class InfoGen_Autowired {
	public final Logger logger = Logger.getLogger(InfoGen_Autowired.class.getName());

	private static class InnerInstance {
		public static InfoGen_Autowired instance = new InfoGen_Autowired();
	}

	public static InfoGen_Autowired getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Autowired() {
	}

	public void wired(Class<?> clazz, Map<Class<Annotation>, InfoGen_AOP_Handle> advices) {
		String class_name = clazz.getName();
		List<InfoGen_Agent_Advice_Method> methods = new ArrayList<>();

		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			for (Entry<Class<Annotation>, InfoGen_AOP_Handle> advice : advices.entrySet()) {
				Class<Annotation> key = advice.getKey();
				InfoGen_AOP_Handle value = advice.getValue();

				Annotation[] annotation = method.getAnnotationsByType(key);
				if (annotation.length != 0) {
					methods.add(value.attach(class_name, method.getName()));
				}
			}
		}
		if (methods.isEmpty()) {
			return;
		}
		try {
			InfoGen_Agent_Advice_Class infogen_advice = new InfoGen_Agent_Advice_Class();
			infogen_advice.setClass_name(class_name);
			infogen_advice.setMethods(methods);
			InfoGen_Agent_Cache.class_advice_map.put(class_name, Tool_Jackson.toJson(infogen_advice));
		} catch (Exception e) {
			logger.error("注入代码失败", e);
		}
	}
}
