package com.infogen.aop.advice;

import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	public final Logger logger = Logger.getLogger(InfoGen_Advice.class.getName());

	private static class InnerInstance {
		public static InfoGen_Advice instance = new InfoGen_Advice();
	}

	public static InfoGen_Advice getInstance() {
		return InnerInstance.instance;
	}

	private String agent_path = InfoGen_Agent_Path.path();
	private Method loadAgent = null;
	// private Method detach = null;
	private Object virtualmachine_instance = null;
	private InfoGen_ClassLoader classLoader = new InfoGen_ClassLoader(new URL[] {}, null);

	private InfoGen_Advice() {
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

	private Map<String, List<InfoGen_Agent_Advice_Field>> map_autowireds = new HashMap<>();

	public void add_autowired(String class_name, String field_name, String value) {
		InfoGen_Agent_Advice_Field infogen_agent_advice_field = new InfoGen_Agent_Advice_Field();
		infogen_agent_advice_field.setField_name(field_name);
		infogen_agent_advice_field.setValue(value);

		List<InfoGen_Agent_Advice_Field> orDefault = map_autowireds.getOrDefault(class_name, new ArrayList<InfoGen_Agent_Advice_Field>());
		orDefault.add(infogen_agent_advice_field);
		map_autowireds.put(class_name, orDefault);
	}

	public void attach(Class<?> clazz, Map<Class<Annotation>, InfoGen_AOP_Handle> advice_methods) {
		String class_name = clazz.getName();
		List<InfoGen_Agent_Advice_Method> methods = new ArrayList<>();

		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			for (Entry<Class<Annotation>, InfoGen_AOP_Handle> advice : advice_methods.entrySet()) {
				Class<Annotation> key = advice.getKey();
				InfoGen_AOP_Handle value = advice.getValue();

				Annotation[] annotations = method.getAnnotationsByType(key);
				if (annotations.length != 0) {
					InfoGen_Agent_Advice_Method attach_method = value.attach_method(class_name, method, annotations[0]);
					if (attach_method != null) {
						methods.add(attach_method);
					}
				}
			}
		}

		List<InfoGen_Agent_Advice_Field> fields = map_autowireds.get(class_name);

		if (methods.isEmpty() && fields.isEmpty()) {
			return;
		}
		try {

			InfoGen_Agent_Advice_Class infogen_advice = new InfoGen_Agent_Advice_Class();
			infogen_advice.setClass_name(class_name);
			infogen_advice.setMethods(methods);
			infogen_advice.setFields(fields);
			System.out.println(Tool_Jackson.toJson(infogen_advice));
			InfoGen_Agent_Cache.class_advice_map.put(class_name, Tool_Jackson.toJson(infogen_advice));
			loadAgent.invoke(virtualmachine_instance, new Object[] { agent_path, class_name });
		} catch (Exception e) {
			logger.error("注入代码失败", e);
		}
	}
}
