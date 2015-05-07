package com.infogen.aop.advice;

import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;

import com.infogen.aop.advice.event_handle.InfoGen_AOP_Handle;
import com.infogen.aop.agent.InfoGen_Agent_Advice_Class;
import com.infogen.aop.agent.InfoGen_Agent_Advice_Field;
import com.infogen.aop.agent.InfoGen_Agent_Advice_Method;
import com.infogen.aop.agent.InfoGen_Agent_Cache;
import com.infogen.aop.agent.InfoGen_Agent_Path;
import com.infogen.aop.tools.Tool_Jackson;
import com.infogen.aop.util.InfoGen_ClassLoader;

/**
 * 通过附着agent的方式实现面向切面的功能
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2015年2月11日 下午5:32:23
 * @param <T>
 */
public class InfoGen_Advice {
	public static final Logger logger = Logger.getLogger(InfoGen_Advice.class.getName());

	private static InfoGen_ClassLoader classLoader = new InfoGen_ClassLoader(new URL[] {}, null);
	private static Method loadAgent = null;
	private static Object virtualmachine_instance = null;
	static {
		try {
			String port = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

			String java_home = System.getProperty("java.home");
			logger.info("java.home  => " + java_home);
			if (System.getProperty("os.name").indexOf("Windows") != -1) {
				if (java_home.contains("jdk")) {
					java_home = java_home.replace("jre", "").concat("lib/tools.jar");
				} else {
					java_home = java_home.replace("jre", "jdk").concat("/lib/tools.jar");
				}
			} else {
				java_home = java_home.replace("jre", "").concat("lib/tools.jar");
			}

			logger.info("jdk home dir => " + java_home);
			classLoader.addJar(Paths.get(java_home).toUri().toURL());
			Class<?> clazz = classLoader.loadClass("com.sun.tools.attach.VirtualMachine");
			Method attach = clazz.getMethod("attach", new Class[] { String.class });
			virtualmachine_instance = attach.invoke(null, new Object[] { port });
			loadAgent = clazz.getMethod("loadAgent", new Class[] { String.class, String.class });
			// TODO 如果只加载一次应该在使用完成后close
			// detach = clazz.getMethod("detach", new Class[] {});
			classLoader.close();
		} catch (Exception e) {
			logger.error("初始化AOP失败", e);
		}
	}

	private Map<Class<Annotation>, InfoGen_AOP_Handle> advice_methods = new HashMap<>();

	@SuppressWarnings("unchecked")
	public void add_advice_method(Object clazz, InfoGen_AOP_Handle instance) {
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(instance);
		try {
			advice_methods.put((Class<Annotation>) clazz, instance);
		} catch (java.lang.ClassCastException e) {
			logger.error("clazz 不是注解类型", e);
		}
	}

	private Map<String, Set<InfoGen_Agent_Advice_Field>> map_autowireds = new HashMap<>();

	public void add_autowired_field(String class_name, String field_name, String value) {
		InfoGen_Agent_Advice_Field infogen_agent_advice_field = new InfoGen_Agent_Advice_Field();
		infogen_agent_advice_field.setField_name(field_name);
		infogen_agent_advice_field.setValue(value);

		Set<InfoGen_Agent_Advice_Field> orDefault = map_autowireds.getOrDefault(class_name, new HashSet<InfoGen_Agent_Advice_Field>());
		orDefault.add(infogen_agent_advice_field);
		map_autowireds.put(class_name, orDefault);
	}

	/**
	 * 
	 * @param clazz
	 */
	public void attach(Class<?> clazz) {

		String class_name = clazz.getName();
		Set<InfoGen_Agent_Advice_Method> methods = new HashSet<>();

		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			for (Entry<Class<Annotation>, InfoGen_AOP_Handle> advice : advice_methods.entrySet()) {
				Class<Annotation> key = advice.getKey();
				InfoGen_AOP_Handle value = advice.getValue();

				Annotation[] annotations = method.getAnnotationsByType(key);
				if (annotations.length != 0) {
					Annotation annotation = annotations[0];
					InfoGen_Agent_Advice_Method attach_method = value.attach_method(class_name, method, annotation);
					if (attach_method != null) {
						attach_method.setAnnotation(annotation.toString());
						methods.add(attach_method);
					}
				}
			}
		}

		Set<InfoGen_Agent_Advice_Field> fields = map_autowireds.getOrDefault(class_name, new HashSet<InfoGen_Agent_Advice_Field>());

		if (methods.isEmpty() && fields.isEmpty()) {
			return;
		}
		try {
			InfoGen_Agent_Advice_Class infogen_advice = new InfoGen_Agent_Advice_Class();
			infogen_advice.setClass_name(class_name);
			infogen_advice.setMethods(methods);
			infogen_advice.setFields(fields);
			InfoGen_Agent_Cache.class_advice_map.put(class_name, Tool_Jackson.toJson(infogen_advice));

			loadAgent.invoke(virtualmachine_instance, new Object[] { InfoGen_Agent_Path.path(), class_name });
		} catch (Exception e) {
			logger.error("注入代码失败", e);
		}
	}
}
