package com.infogen.aop;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.infogen.aop.advice.event_handle.AOP_Handle;
import com.infogen.aop.agent.Agent_Advice_Class;
import com.infogen.aop.agent.Agent_Advice_Field;
import com.infogen.aop.agent.Agent_Advice_Method;
import com.infogen.aop.agent.Agent_Cache;
import com.infogen.attach.Infogen_Attach_Path;
import com.infogen.json.Jackson;
import com.infogen.path.NativePath;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月30日 下午2:03:45
 * @since 1.0
 * @version 1.0
 */
public class AOP {
	private final static Logger LOGGER = LogManager.getLogger(AOP.class.getName());

	private static class InnerInstance {
		public static final AOP instance = new AOP();
	}

	public static AOP getInstance() {
		return InnerInstance.instance;
	}

	// Scan Class
	private Set<Class<?>> classes = new LinkedHashSet<>();

	private AOP() {
		try {
			classes = auto_scan_absolute(NativePath.get_class_path());
		} catch (Exception e) {
			LOGGER.error("加载class失败:", e);
		}
	}

	// AOP
	private Map<Class<? extends Annotation>, AOP_Handle> advice_methods = new HashMap<>();

	public void add_advice_method(Class<? extends Annotation> clazz, AOP_Handle instance) {
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(instance);
		try {
			advice_methods.put(clazz, instance);
		} catch (java.lang.ClassCastException e) {
			LOGGER.error("clazz 不是注解类型", e);
		}
	}

	// IOC
	private Map<String, Set<Agent_Advice_Field>> advice_fields = new HashMap<>();

	public void add_autowired_field(String class_name, String field_name, String value) {
		Agent_Advice_Field infogen_agent_advice_field = new Agent_Advice_Field();
		infogen_agent_advice_field.setField_name(field_name);
		infogen_agent_advice_field.setValue(value);

		Set<Agent_Advice_Field> orDefault = advice_fields.getOrDefault(class_name, new HashSet<Agent_Advice_Field>());
		orDefault.add(infogen_agent_advice_field);
		advice_fields.put(class_name, orDefault);
	}

	///////////////////////////////////////////////////////////////////////
	public Boolean isadvice = false;

	public void advice() {
		classes.forEach((clazz) -> {
			generate_agent_advice(clazz);
		});

		String attach_path = Infogen_Attach_Path.path().replaceAll(" ", "\" \"");

		String agent_Path = Infogen_Core_Path.path().replaceAll(" ", "\" \"");
		Long pid = ProcessHandle.current().pid();
		try {
			Process process = Runtime.getRuntime().exec("java -jar " + attach_path + " " + agent_Path + " " + pid);
			int exitVal = process.waitFor();
			if (exitVal != 0) {
				LOGGER.error("注入代码失败:" + exitVal);
			}
		} catch (InterruptedException | IOException e) {
			LOGGER.error("注入代码失败", e);
		}

		isadvice = true;
	}

	//////////////////////////
	private void generate_agent_advice(Class<?> clazz) {
		String class_name = clazz.getName();
		Set<Agent_Advice_Field> fields = advice_fields.getOrDefault(class_name, new HashSet<Agent_Advice_Field>());
		Set<Agent_Advice_Method> methods = new HashSet<>();

		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			for (Class<? extends Annotation> key : advice_methods.keySet()) {
				AOP_Handle handle = advice_methods.get(key);

				Annotation[] annotations = method.getAnnotationsByType(key);
				if (annotations.length == 0) {
					continue;
				}

				Agent_Advice_Method attach_method = handle.attach_method(class_name, method, annotations[0]);
				if (attach_method == null) {
					continue;
				}

				Class<?>[] parameterTypes = method.getParameterTypes();
				StringBuilder stringbuilder = new StringBuilder();
				for (Class<?> type : parameterTypes) {
					stringbuilder.append(type.getName()).append(" ");
				}
				attach_method.setMethod_parameters(stringbuilder.toString().trim());

				methods.add(attach_method);
			}
		}

		if (methods.isEmpty() && fields.isEmpty()) {
			return;
		}

		Agent_Advice_Class infogen_advice_class = new Agent_Advice_Class();
		infogen_advice_class.setClass_name(class_name);
		infogen_advice_class.setMethods(methods);
		infogen_advice_class.setFields(fields);

		try {
			Agent_Cache.class_advice_map.put(class_name, Jackson.toJson(infogen_advice_class));
		} catch (JsonProcessingException e) {
			LOGGER.error("生成AOP 参数失败", e);
		}

	}

	// ///////////////////////////////////////////////////////////component_scan/////////////////////////////////////////////////
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
					LOGGER.error("加载class失败:", e);
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
					LOGGER.error("加载class失败:", e);
				}
			});
		}
		return classes;
	}

	////////////////////////////////////////////////////////////////////////

	public Set<Class<?>> getClasses() {
		return classes;
	}

	public void addClasses(Class<?> clazz) {
		classes.add(clazz);
	}
}
