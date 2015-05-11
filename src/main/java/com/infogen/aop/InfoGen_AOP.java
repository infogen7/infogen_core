package com.infogen.aop;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.infogen.aop.advice.event_handle.InfoGen_AOP_Handle;
import com.infogen.aop.agent.InfoGen_Agent_Advice_Class;
import com.infogen.aop.agent.InfoGen_Agent_Advice_Field;
import com.infogen.aop.agent.InfoGen_Agent_Advice_Method;
import com.infogen.aop.agent.InfoGen_Agent_Cache;
import com.infogen.aop.agent.InfoGen_Agent_Path;
import com.infogen.aop.tools.Tool_Jackson;
import com.infogen.aop.util.InfoGen_ClassLoader;
import com.infogen.aop.util.NativePath;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月30日 下午2:03:45
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_AOP {
	public final static Logger logger = Logger.getLogger(InfoGen_AOP.class.getName());

	private static class InnerInstance {
		public static InfoGen_AOP instance = new InfoGen_AOP();
	}

	public static InfoGen_AOP getInstance() {
		return InnerInstance.instance;
	}

	private static InfoGen_ClassLoader classLoader = new InfoGen_ClassLoader(new URL[] {}, null);
	private static Method loadAgent = null;
	private static Object virtualmachine_instance = null;

	private InfoGen_AOP() {
		try {
			classes = auto_scan_absolute(NativePath.get_class_path());

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

	// 面向切面
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

	// 依赖注入
	private Map<String, Set<InfoGen_Agent_Advice_Field>> map_autowireds = new HashMap<>();

	public void add_autowired_field(String class_name, String field_name, String value) {
		InfoGen_Agent_Advice_Field infogen_agent_advice_field = new InfoGen_Agent_Advice_Field();
		infogen_agent_advice_field.setField_name(field_name);
		infogen_agent_advice_field.setValue(value);

		Set<InfoGen_Agent_Advice_Field> orDefault = map_autowireds.getOrDefault(class_name, new HashSet<InfoGen_Agent_Advice_Field>());
		orDefault.add(infogen_agent_advice_field);
		map_autowireds.put(class_name, orDefault);
	}

	//
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
						Class<?>[] parameterTypes = method.getParameterTypes();
						StringBuilder stringbuilder = new StringBuilder();
						for (Class<?> type : parameterTypes) {
							stringbuilder.append(type.getName()).append(" ");
						}
						attach_method.setMethod_parameters(stringbuilder.toString().trim());
						methods.add(attach_method);
					}
				}
			}
		}

		Set<InfoGen_Agent_Advice_Field> fields = map_autowireds.getOrDefault(class_name, new HashSet<InfoGen_Agent_Advice_Field>());

		if (methods.isEmpty() && fields.isEmpty()) {
			return;
		}
		InfoGen_Agent_Advice_Class infogen_advice = new InfoGen_Agent_Advice_Class();
		infogen_advice.setClass_name(class_name);
		infogen_advice.setMethods(methods);
		infogen_advice.setFields(fields);
		InfoGen_Agent_Cache.class_advice_map.put(class_name, Tool_Jackson.toJson(infogen_advice));
	}

	//
	public void advice() {
		classes.forEach((clazz) -> {
			attach(clazz);
		});

		try {
			loadAgent.invoke(virtualmachine_instance, new Object[] { InfoGen_Agent_Path.path(), "" });
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("注入代码失败", e);
		}
	}

	// ///////////////////////////////////////////////////////////component_scan/////////////////////////////////////////////////
	private Set<Class<?>> classes = new LinkedHashSet<>();

	public Set<Class<?>> getClasses() {
		return classes;
	}

	public void addClasses(Class<?> clazz) {
		classes.add(clazz);
	}

	private Pattern anonymous_inner_class_compile = Pattern.compile("^*[$][0-9]+\\.class$");

	@SuppressWarnings("resource")
	private Set<Class<?>> auto_scan_absolute(String class_path) throws IOException {
		Set<Class<?>> classes = new LinkedHashSet<>();
		if (class_path.endsWith(".jar")) {
			Enumeration<JarEntry> entries = new JarFile(class_path).entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String class_name = entry.getName();
				if (!class_name.toString().endsWith(".class") && !anonymous_inner_class_compile.matcher(class_name).find()) {
					continue;
				}
				class_name = class_name.replace(".class", "").replace("/", ".");
				try {
					classes.add(Thread.currentThread().getContextClassLoader().loadClass(class_name));
				} catch (ClassNotFoundException e) {
					logger.info("加载class失败:");
					e.printStackTrace();
				}
			}
		} else {
			Files.walk(Paths.get(class_path)).filter((path) -> {
				String path_string = path.toString();
				return path_string.endsWith(".class") && !anonymous_inner_class_compile.matcher(path_string).find();
			}).forEach((name) -> {
				String class_name = name.toString();
				if (System.getProperty("os.name").indexOf("Windows") != -1) {
					class_name = class_name.substring(class_name.indexOf("\\classes\\") + 9).replace(".class", "").replace("\\", ".");
				} else {
					class_name = class_name.substring(class_name.indexOf("/classes/") + 9).replace(".class", "").replace("/", ".");
				}
				try {
					classes.add(Thread.currentThread().getContextClassLoader().loadClass(class_name));
				} catch (Exception e) {
					logger.info("加载class失败:");
					e.printStackTrace();
				}
			});
		}
		return classes;
	}
}
