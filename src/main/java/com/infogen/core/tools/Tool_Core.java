/**
 * 
 */
package com.infogen.core.tools;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 基本工具方法
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 上午11:52:17
 * @since 1.0
 * @version 1.0
 */
public class Tool_Core {
	private static final Logger LOGGER = LogManager.getLogger(Tool_Core.class.getName());

	/**
	 * @param e
	 *            异常类
	 * @return 拼接打印 exception 栈内容
	 */
	public static String stacktrace(Throwable e) {
		StringBuilder stack_trace = new StringBuilder();
		while (e != null) {
			String error_message = e.getMessage();
			error_message = error_message == null ? "\r\n" : error_message.concat("\r\n");
			stack_trace.append(error_message);
			stack_trace.append("<br>");
			for (StackTraceElement string : e.getStackTrace()) {
				stack_trace.append(string.toString());
				stack_trace.append("<br>");
			}
			e = e.getCause();
		}
		return stack_trace.toString();
	}

	/**
	 * 创建文件并自动补全文件路径的缺失文件夹
	 * 
	 * @param paths
	 *            文件路径
	 */
	public static void prepare_files(Path... paths) {
		for (Path path : paths) {
			try {
				if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
					Files.createFile(path);
				}
			} catch (IOException e) {
				LOGGER.error("创建依赖文件夹失败", e);
			}
		}
	}

	/**
	 * @param path
	 *            文件路径
	 * @return 加载文件为字符串
	 * @throws IOException
	 *             读取文件异常
	 */
	public static String load_file(Path path) throws IOException {
		// 获取缓存的服务配置
		StringBuilder sbf = new StringBuilder();
		Files.lines(path, StandardCharsets.UTF_8).forEach(line -> {
			sbf.append(line);
		});
		return sbf.toString();
	}

	/**
	 * @param password
	 *            密码
	 * @param salt
	 *            盐值
	 * @return MD5
	 * @throws NoSuchAlgorithmException
	 *             加密异常
	 */
	public static String MD5(String password, String salt) throws NoSuchAlgorithmException {
		MessageDigest instance = MessageDigest.getInstance("MD5");
		instance.update((password + "{" + salt.toString() + "}").getBytes(Charset.forName("UTF-8")));
		char hexDigits[] = { // 用来将字节转换成 16 进制表示的字符
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		byte tmp[] = instance.digest(); // MD5 的计算结果是一个 128 位的长整数，用字节表示就是 16 个字节
		char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，所以表示成 16 进制需要 32 个字符
		int k = 0; // 表示转换结果中对应的字符位置
		for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节转换成 16 进制字符的转换
			byte byte0 = tmp[i]; // 取第 i 个字节
			str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,>>> 为逻辑右移，将符号位一起右移
			str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
		}
		return new String(str);
	}

	/**
	 * @return 获取本机IP
	 */
	public static String getLocalIP(String... nic_names) {
		if (nic_names.length == 0) {
			nic_names = new String[] { "eth", "wlan" };
		}
		String ip = null;
		try {
			if (System.getProperty("os.name").indexOf("Linux") != -1) {
				for (String nic_name : nic_names) {
					ip = get_local_ip_bystartswith(nic_name);
					if (ip != null) {
						break;
					}
				}
			} else {
				ip = InetAddress.getLocalHost().getHostAddress().toString();
			}
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
		return ip;
	}

	private static String get_local_ip_bystartswith(String startsWith) throws SocketException {
		String ip = null;
		Enumeration<?> e1 = (Enumeration<?>) NetworkInterface.getNetworkInterfaces();
		while (e1.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface) e1.nextElement();
			if (ni.getName().startsWith(startsWith)) {
				Enumeration<?> e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					InetAddress ia = (InetAddress) e2.nextElement();
					if (ia instanceof Inet6Address) {
						continue;
					}
					ip = ia.getHostAddress();
				}
				break;
			}
		}
		return ip;
	}

	/**
	 * @return 获取本机主机名
	 */
	public static String getHostName() {
		if (System.getenv("COMPUTERNAME") != null) {
			return System.getenv("COMPUTERNAME");
		} else {
			try {
				return (InetAddress.getLocalHost()).getHostName();
			} catch (UnknownHostException uhe) {
				String host = uhe.getMessage();
				if (host != null) {
					int colon = host.indexOf(':');
					if (colon > 0) {
						return host.substring(0, colon);
					}
				}
				return "UnknownHost";
			}
		}
	}

	/**
	 * @param string
	 *            输入字符串
	 * @return trim 替换中文空格的特殊字符为普通空格 去掉字符串中前后的空格，并将中间多个连续的空格合并成一个 去掉开头出现的空格
	 */
	public static String trim(String string) {
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

	/**
	 * 
	 * @param string_ip
	 *            ip的string类型
	 * @return IP转成数字类型
	 */
	public long ip_to_long(String string_ip) {
		long[] ip = new long[4];
		int position1 = string_ip.indexOf(".");
		int position2 = string_ip.indexOf(".", position1 + 1);
		int position3 = string_ip.indexOf(".", position2 + 1);
		ip[0] = Long.parseLong(string_ip.substring(0, position1));
		ip[1] = Long.parseLong(string_ip.substring(position1 + 1, position2));
		ip[2] = Long.parseLong(string_ip.substring(position2 + 1, position3));
		ip[3] = Long.parseLong(string_ip.substring(position3 + 1));
		return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3]; // ip1*256*256*256+ip2*256*256+ip3*256+ip4
	}

	private static final String PATTERN_LINE_START = "^";
	private static final String PATTERN_LINE_END = "$";
	private static final char[] META_CHARACTERS = { '$', '^', '[', ']', '(', ')', '{', '}', '|', '+', '.', '\\' };

	/**
	 * 通配符匹配
	 * 
	 * @param pattern
	 *            规则
	 * @param str
	 *            验证字符串
	 * @return 是否验证成功
	 */
	public static boolean pattern_match(String pattern, String str) {
		pattern = convertToRegexPattern(pattern);
		return Pattern.matches(pattern, str);
	}

	private static String convertToRegexPattern(String wildcardString) {
		String result = PATTERN_LINE_START;
		char[] chars = wildcardString.toCharArray();
		for (char ch : chars) {
			if (Arrays.binarySearch(META_CHARACTERS, ch) >= 0) {
				result += "\\" + ch;
				continue;
			}
			switch (ch) {
			case '*':
				result += ".*";
				break;
			case '?':
				result += ".{0,1}";
				break;
			default:
				result += ch;
			}
		}
		result += PATTERN_LINE_END;
		return result;
	}

}
