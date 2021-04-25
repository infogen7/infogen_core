package com.infogen.auth;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import com.infogen.time.Time;

public class Auth {
	public static String X_DUBBOTOKEN = "X-DubboToken";

	public static String dubbo(String appID, String appKey, String service, String method) throws NoSuchAlgorithmException {
		Long timestamp = Time.GMT8();
		String parameters = new StringBuilder(appID).append("&").append(appKey).append("&").append(service).append("&").append(method).append("&").append(timestamp).toString();
		String md5 = MD5.md5DigestAsHex(parameters);
		return new StringBuilder(appID).append(".").append(md5).append(".").append(timestamp).toString();
	}

	public static String calculateHash(BigInteger index, String previousHash, Long timestamp, String data) throws NoSuchAlgorithmException {
		previousHash = previousHash == null ? "" : previousHash;
		return SHA256.sha256DigestAsHex(index + previousHash + timestamp + data);
	}

}