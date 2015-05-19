/**
 * 
 */
package com.larrylgq.aop.util;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 自定义类加载器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 上午11:49:39
 * @since 1.0
 * @version 1.0
 */
public class AOP_ClassLoader extends URLClassLoader {

	public AOP_ClassLoader(URL[] urls) {
		super(urls);
	}

	public AOP_ClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public void addJar(URL url) {
		this.addURL(url);
	}

}