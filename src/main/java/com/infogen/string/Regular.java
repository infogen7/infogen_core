package com.infogen.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regular {
	private static Boolean check(String string, String pattern) {
		if (null == string || string.trim().length() <= 0) {
			return false;
		}
		return Pattern.compile(pattern).matcher(string).matches();
	}

	public static Boolean is_mobile(String string) {// 判断是否为手机号码 符合返回ture
		return check(string, "^1\\d{10}$");
	}

	public static Boolean is_password_6_20(String string) {// 判断是否为6-20位密码
		return check(string, "^\\w{6,20}$");
	}

	public static Boolean is_name(String string) {// 判断是否2-16位汉字或字母
		return check(string, "^([\\u4e00-\\u9fa5a-zA-Z0-9_\\.\\s]{2,16})$");
	}

	public static Boolean is_email(String string) {// 判断字段是否为Email 符合返回ture
		return check(string, "\\w+(\\.\\w+)*@\\w+(\\.\\w+)+");
	}

	public static Boolean is_phone(String string) {// 判断是否为电话号码 符合返回ture
		return check(string, "(^(\\\\d{2,4}[-_－—]?)?\\\\d{3,8}([-_－—]?\\\\d{3,8})?([-_－—]?\\\\d{1,7})?$)|(^0?1[35]\\\\d{9}$)");
	}

	public static Boolean is_url(String string) {// 判断是否为Url 符合返回ture
		return check(string, "^(http|www|ftp|)?(://)?(\\\\w+(-\\\\w+)*)(\\\\.(\\\\w+(-\\\\w+)*))*((:\\\\d+)?)(/(\\\\w+(-\\\\w+)*))*(\\\\.?(\\\\w)*)(\\\\?)?\" + \"(((\\\\w*%)*(\\\\w*\\\\?)*(\\\\w*:)*(\\\\w*\\\\+)*(\\\\w*\\\\.)*(\\\\w*&)*(\\\\w*-)*(\\\\w*=)*(\\\\w*%)*(\\\\w*\\\\?)*\" + \"(\\\\w*:)*(\\\\w*\\\\+)*(\\\\w*\\\\.)*\" + \"(\\\\w*&)*(\\\\w*-)*(\\\\w*=)*)*(\\\\w*)*)$");
	}

	public static Boolean is_idcard(String string) {// 判断字段是否为身份证 符合返回ture
		if (null == string || string.trim().length() <= 0) {
			return false;
		}
		if (string.trim().length() == 15 || string.trim().length() == 18) {
			return check(string, "((11|12|13|14|15|21|22|23|31|32|33|34|35|36|37|41|42|43|44|45|46|50|51|52|53|54|61|62|63|64|65)[0-9]{4})" + "(([1|2][0-9]{3}[0|1][0-9][0-3][0-9][0-9]{3}" + "[Xx0-9])|([0-9]{2}[0|1][0-9][0-3][0-9][0-9]{3}))");
		} else {
			return false;
		}
	}

	/////////////////////
	private static String find(String string, String pattern) {
		if (null == string || string.trim().length() <= 0) {
			return "";
		}
		Matcher matcher = Pattern.compile(pattern).matcher(string);
		if (matcher.find()) {
			return matcher.group(0);
		}
		return "";
	}

	public static String find_address(String string) {// 判断是否为地址 中文，英文，中划线
		return find(string, "[\\u4e00-\\u9fa50-9-]+");
	}
}
