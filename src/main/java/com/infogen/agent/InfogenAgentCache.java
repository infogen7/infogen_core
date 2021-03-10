/**
 * 
 */
package com.infogen.agent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class InfogenAgentCache {
	public static Map<String, String> injection_class_map = new HashMap<>();

	public static void load_class() {
		// 加载 Agent_Cache 的 Class 到 JVM
	}
}
