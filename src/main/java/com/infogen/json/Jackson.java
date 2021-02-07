/**
 * 
 */
package com.infogen.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * json转换
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午12:00:41
 * @since 1.0
 * @version 1.0
 */
public class Jackson {
	private final static ObjectMapper objectMapper = new ObjectMapper();

	static {
		// TODO 是否需要时间转换 默认时间戳
		objectMapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		// 允许单引号
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		// 允许反斜杆等字符
		objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
		// 允许出现对象中没有的字段
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private Jackson() {
	}

	public static String toJson(Object object) throws JsonProcessingException {
		return objectMapper.writeValueAsString(object);
	}

	public static <T> T toObject(String json, Class<T> valueType) throws IOException {
		return objectMapper.readValue(json, valueType);
	}

	public static <T> T toObject(String json, TypeReference<T> typeReference) throws IOException {
		return (T) objectMapper.readValue(json, typeReference);
	}
}
