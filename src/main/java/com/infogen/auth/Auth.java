package com.infogen.auth;

import java.security.NoSuchAlgorithmException;
import com.infogen.time.Time;

public class Auth {
	public static String HEAD_KEY_TOKEN = "X-DCToken";

	public static String sign(String appID, String appKey, String service, String method) throws NoSuchAlgorithmException {
		Long timestamp = Time.GMT8();
		String parameters = new StringBuilder(appID).append("&").append(appKey).append("&").append(service).append("&").append(method).append("&").append(timestamp).toString();
		String md5 = MD5.md5DigestAsHex(parameters);
		return new StringBuilder(appID).append(".").append(md5).append(".").append(timestamp).toString();
	}
}