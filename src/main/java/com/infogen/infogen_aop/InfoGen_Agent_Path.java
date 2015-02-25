/**
 * 
 */
package com.infogen.infogen_aop;

/**
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2015年2月10日 下午6:28:06
 */
public class InfoGen_Agent_Path {

	// file:/home/xxx/.m2/repository/com/infogen/infogen_agent/V1.0.00R150210/infogen_agent-V1.0.00R150210.jar
	// file:/home/xxx/workspace/infogen/lib/infogen_agent.jar
	public static String path() {
		String location = InfoGen_Agent_Path.class.getProtectionDomain().getCodeSource().getLocation().toString();
		location = location.replace("file:", "");
		if (System.getProperty("os.name").indexOf("Windows") != -1) {
			location = location.substring(1);
		}
		return location;
	}
}
