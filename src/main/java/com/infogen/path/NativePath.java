package com.infogen.path;

import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

/**
 * 获取linux和windows平台下各种发布测试环境下基于classpath的路径 测试过jetty和tomcat
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 上午11:51:48
 * @since 1.0
 * @version 1.0
 */
@Slf4j
public class NativePath {
	/**
	 * 获取配置文件路径
	 * 
	 * class path为jar包的情况下,路径指向与jar包同级目录 jetty jar包独立部署方式,需要在maven中添加resources配置targetPath {project.build.directory} tomcat发布,需要将配置文件放在/WEB-INF下面
	 * 
	 * @param path
	 *            相对路径
	 * @return 绝对路径
	 */
	public static Path get(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		String java_class_path = get_class_path();
		if (java_class_path.endsWith(".jar")) {
			int lastIndexOf = java_class_path.lastIndexOf("/");
			if (lastIndexOf == -1) {
				java_class_path = "";
			} else {
				java_class_path = java_class_path.substring(0, lastIndexOf);
			}
		}
		if (!java_class_path.isEmpty() && !java_class_path.endsWith("/")) {
			java_class_path = java_class_path.concat("/");
		}
		java_class_path = java_class_path.concat(path);
		log.info("final path ---> :".concat(java_class_path));
		return Paths.get(java_class_path);
	}

	/**
	 * 当前启动项目的class path (jar包地址或末尾不包含/的class文件夹地址)
	 * 
	 * @return classpath
	 */
	public static String get_class_path() {
		String java_class_path = System.getProperty("java.class.path");
		log.debug("java_class_path -> :".concat(java_class_path));
		log.debug(System.getProperty("os.name"));
		if (System.getProperty("os.name").indexOf("Windows") != -1) {
			int indexof_classes = java_class_path.indexOf("\\classes");
			if (indexof_classes != -1) {
				// 直接代码启动
				java_class_path = java_class_path.substring(0, indexof_classes).concat("\\classes");
				int indexof_separator = java_class_path.lastIndexOf(";");
				if (indexof_separator != -1) {
					java_class_path = java_class_path.substring(indexof_separator + 1);
				}
				log.debug("windows code start --> :".concat(java_class_path));
			} else {
				String webroot = NativePath.class.getResource("").getFile();
				webroot = webroot.replace("file:/", "");
				int indexof_web_inf = webroot.indexOf("/WEB-INF/");
				if (indexof_web_inf != -1) {
					// WEB容器启动
					java_class_path = webroot.substring(0, indexof_web_inf).concat("/WEB-INF/classes");
					log.debug("windows server start --> :".concat(java_class_path));
				} else {
					int comma = java_class_path.indexOf(";");
					if (comma > 0) {
						java_class_path = java_class_path.substring(0, comma);
					}
					// JAR包启动
					log.debug("windows jar start --> :".concat(java_class_path));
				}
			}
		} else {// LINUX
			int indexof_classes = java_class_path.indexOf("/classes");
			if (indexof_classes != -1) {
				// 直接代码启动
				java_class_path = java_class_path.substring(0, indexof_classes).concat("/classes");
				int indexof_separator = java_class_path.lastIndexOf(":");
				if (indexof_separator != -1) {
					java_class_path = java_class_path.substring(indexof_separator + 1);
				}
				log.debug("linux code start --> :".concat(java_class_path));
			} else {
				String webroot = NativePath.class.getResource("").getFile();
				webroot = webroot.replace("file:", "");
				int indexof_web_inf = webroot.indexOf("/WEB-INF/");
				if (indexof_web_inf != -1) {
					// WEB容器启动
					java_class_path = webroot.substring(0, indexof_web_inf).concat("/WEB-INF/classes");
					log.debug("linux server start --> :".concat(java_class_path));
				} else {
					int comma = java_class_path.indexOf(":");
					if (comma > 0) {
						java_class_path = java_class_path.substring(0, comma);
					}
					// JAR包启动
					log.debug("linux jar start --> :".concat(java_class_path));
				}
			}
		}
		return java_class_path;
	}
}
