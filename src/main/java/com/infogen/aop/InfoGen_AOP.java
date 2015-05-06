/**
 * @author larry/larrylv@outlook.com
 * @date 创建时间 2015年4月30日 下午2:03:45
 * @version 1.0
 */
package com.infogen.aop;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.infogen.aop.advice.InfoGen_Advice;
import com.infogen.aop.advice.event_handle.InfoGen_AOP_Handle;
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

	private InfoGen_AOP() {
		try {
			find_all_class();
		} catch (IOException e) {
			logger.error("扫描class失败:", e);
		}
	}

	// ///////////////////////////////////////////////////////////component_scan/////////////////////////////////////////////////
	private Set<Class<?>> classes = new LinkedHashSet<>();

	public Set<Class<?>> getClasses() {
		return classes;
	}

	private Map<Class<Annotation>, InfoGen_AOP_Handle> advices = new HashMap<>();

	@SuppressWarnings("unchecked")
	public void add_advice(Object clazz, InfoGen_AOP_Handle instance) {
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(instance);
		try {
			advices.put((Class<Annotation>) clazz, instance);
		} catch (java.lang.ClassCastException e) {
			logger.error("clazz 不是注解类型", e);
		}
	}

	public void start() {
		classes.forEach((clazz) -> {
			InfoGen_Advice.getInstance().attach(clazz, advices);
		});
	}

	private Pattern anonymous_inner_class_compile = Pattern.compile("^*[$][0-9]+\\.class$");

	@SuppressWarnings("resource")
	private Set<Class<?>> find_all_class() throws IOException {
		String get_class_path = NativePath.get_class_path();
		if (get_class_path.endsWith(".jar")) {
			Enumeration<JarEntry> entries = new JarFile(get_class_path).entries();
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
			Files.walk(Paths.get(get_class_path)).filter((path) -> {
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
