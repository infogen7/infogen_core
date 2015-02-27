/**
 * 
 */
package com.infogen.infogen_aop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Agent_Base64 {
	@SuppressWarnings("restriction")
	private static sun.misc.BASE64Encoder base64encoder = new sun.misc.BASE64Encoder();
	@SuppressWarnings("restriction")
	private static sun.misc.BASE64Decoder base64decoder = new sun.misc.BASE64Decoder();

	@SuppressWarnings("restriction")
	public static String to_base64(Object object) throws IOException {
		// 序列化对象
		try (ByteArrayOutputStream bOut = new ByteArrayOutputStream();//
				ObjectOutputStream objOut = new ObjectOutputStream(bOut);) {
			objOut.writeObject(object);
			String encode = base64encoder.encode(bOut.toByteArray());
			return encode;
		}
	}

	@SuppressWarnings({ "restriction", "unchecked" })
	public static <T> Object to_Object(String base64, Class<T> clazz) throws IOException, ClassNotFoundException {
		T object = null;
		byte[] objBytes = base64decoder.decodeBuffer(base64);
		try (ByteArrayInputStream bIn = new ByteArrayInputStream(objBytes);//
				ObjectInputStream objIn = new ObjectInputStream(bIn);) {
			Object obj = objIn.readObject();
			if (obj != null) {
				if (obj instanceof InfoGen_Agent_Advice) {
					object = (T) obj;
				}
			}
		}
		return object;
	}

}
