/**
 * 
 */
package com.infogen.attach;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class Infogen_Attach_Path {

	// file:/home/xxx/.m2/repository/com/infogen/infogen_core/V1.0.00R150210/infogen_core-V1.0.00R150210.jar
	// file:/home/xxx/workspace/infogen/lib/infogen_core.jar!xxxx.class
	public static String path() {
		String location = Infogen_Attach_Path.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		location = location.replace("file:", "");
		if (System.getProperty("os.name").indexOf("Windows") != -1) {
			location = location.substring(1);
		}
		if (location.contains(".jar!")) {
			location = location.substring(0, location.indexOf(".jar!")).concat(".jar");
		}
		return location;
	}
}
