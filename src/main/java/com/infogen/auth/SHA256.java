package com.infogen.auth;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 {

	/**
	 * @param text
	 *            密码
	 * @return MD5
	 * @throws NoSuchAlgorithmException
	 *             加密异常
	 */
	public static String sha256DigestAsHex(String text) throws NoSuchAlgorithmException {
		MessageDigest instance = MessageDigest.getInstance("SHA-256");
		instance.update(text.getBytes(Charset.forName("UTF-8")));
		byte[] tmp = instance.digest();

		StringBuilder sbd = new StringBuilder();
		for (byte bytei : tmp) {
			String hs = Integer.toHexString(bytei & 0xFF);
			if (hs.length() == 1) {
				sbd.append("0");
			}
			sbd.append(hs);
		}
		return sbd.toString();
	}
}
