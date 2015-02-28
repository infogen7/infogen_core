/**
 * 
 */
package com.infogen.infogen_aop;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Agent_Path {

	// file:/home/xxx/.m2/repository/com/infogen/infogen_agent/V1.0.00R150210/infogen_agent-V1.0.00R150210.jar
	// file:/home/xxx/workspace/infogen/lib/infogen_agent.jar
	public static String path() {
		String location = InfoGen_Agent_Path.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		if (location.contains(".jar!")) {
			location = location.substring(0, location.indexOf(".jar!")).concat(".jar");
		}
		return location;
	}
}
