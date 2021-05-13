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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.infogen.agent.AgentCache;
import com.infogen.agent.AgentPath;
import com.infogen.agent.injection.InjectionClass;
import com.infogen.agent.injection.InjectionField;
import com.infogen.agent.injection.InjectionMethod;
import com.infogen.attach.AttachPath;
import com.infogen.json.Jackson;
import com.infogen.path.NativePath;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月30日 下午2:03:45
 * @since 1.0
 * @version 1.0
 */
public class AOP {
	private final static Logger LOGGER = LoggerFactory.getLogger(AOP.class);

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
	private Map<Class<? extends Annotation>, BasicInterceptor> annotation_handles = new HashMap<>();

	public void add_annotation_handle(Class<? extends Annotation> annotation, BasicInterceptor handle) {
		Objects.requireNonNull(annotation);
		Objects.requireNonNull(handle);
		try {
			annotation_handles.put(annotation, handle);
		} catch (java.lang.ClassCastException e) {
			LOGGER.error("clazz 不是注解类型", e);
		}
	}

	// IOC
	private Map<String, Set<InjectionField>> injection_fields = new HashMap<>();

	public void add_injection_field(String class_name, String field_name, String value) {
		InjectionField injection_field = new InjectionField();
		injection_field.setField_name(field_name);
		injection_field.setValue(value);

		Set<InjectionField> orDefault = injection_fields.getOrDefault(class_name, new HashSet<InjectionField>());
		orDefault.add(injection_field);
		injection_fields.put(class_name, orDefault);
	}

	///////////////////////////////////////////////////////////////////////
	public Boolean isadvice = false;

	public void advice() {
		classes.forEach(clazz -> {
			generate_injection_class(clazz);
		});

		String attach_path = AttachPath.path().replace(" ", "\" \"");
		String agent_Path = AgentPath.path().replace(" ", "\" \"");
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
	private void generate_injection_class(Class<?> clazz) {
		String class_name = clazz.getName();

		// field
		Set<InjectionField> fields = injection_fields.getOrDefault(class_name, new HashSet<InjectionField>());

		// method
		Set<InjectionMethod> methods = new HashSet<>();
		for (Class<? extends Annotation> key : annotation_handles.keySet()) {
			BasicInterceptor handle = annotation_handles.get(key);

			for (Method method : clazz.getDeclaredMethods()) {
				Annotation[] annotations = method.getAnnotationsByType(key);
				if (annotations.length == 0) {
					continue;
				}

				InjectionMethod injection_method = handle.injection_method(class_name, method, annotations[0]);
				if (injection_method == null) {
					continue;
				}

				Class<?>[] parameterTypes = method.getParameterTypes();
				StringBuilder stringbuilder = new StringBuilder();
				for (Class<?> type : parameterTypes) {
					stringbuilder.append(type.getName()).append(" ");
				}
				injection_method.setMethod_parameters(stringbuilder.toString().trim());

				methods.add(injection_method);
			}
		}

		if (methods.isEmpty() && fields.isEmpty()) {
			return;
		}

		InjectionClass infogen_injection_class = new InjectionClass();
		infogen_injection_class.setClass_name(class_name);
		infogen_injection_class.setFields(fields);
		infogen_injection_class.setMethods(methods);

		try {
			AgentCache.injection_class_map.put(class_name, Jackson.toJson(infogen_injection_class));
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
			Files.walk(Paths.get(class_path)).filter(path -> {
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
