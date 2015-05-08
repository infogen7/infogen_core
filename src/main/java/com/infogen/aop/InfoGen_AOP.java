package com.infogen.aop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.infogen.aop.advice.InfoGen_Advice;
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
			classes = auto_scan_absolute(NativePath.get_class_path());
		} catch (IOException e) {
			logger.error("扫描class失败:", e);
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

	public void start(InfoGen_Advice infogen_advice) {
		classes.forEach((clazz) -> {
			infogen_advice.attach(clazz);
		});
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
