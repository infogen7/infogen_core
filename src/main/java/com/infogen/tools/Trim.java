package com.infogen.tools;

import java.util.regex.Pattern;

public class Trim {

	public static String exec(String string, String _default) {
		if (string == null) {
			return _default;
		}
		return exec(string);
	}

	public static String exec(String string) {
		if (string == null) {
			return null;
		}
		// 去掉特殊空格
		string = string.replace(" ", " ");
		// 并将中间多个连续的空格合并成一个
		String trim = Pattern.compile("[' ']+").matcher(string).replaceAll(" ").trim();
		//
		if (trim.startsWith(" ")) {
			trim = trim.substring(1);
		}
		return trim;
	}
}
