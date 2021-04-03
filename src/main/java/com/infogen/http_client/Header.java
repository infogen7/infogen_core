package com.infogen.http_client;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.infogen.json.Jackson;

import lombok.extern.slf4j.Slf4j;

/**
 * HTTP协议调用端输入Header类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月12日 下午3:56:17
 * @since 1.0
 * @version 1.0
 */
@Slf4j
public class Header extends IdentityHashMap<String, Object> {
	private static final long serialVersionUID = -5436768657673377874L;

	public static Header create() {
		return new Header();
	}

	public static Header create(String key, String value) {
		return new Header().put(key, value);
	}

	public static Header create(Map<String, List<String>> name_value_pair) {
		Header header = new Header();
		name_value_pair.forEach((key, values) -> {
			values.forEach(value -> {
				header.put(new String(key), value);
			});
		});
		return header;
	}

	public static Header create(String json) throws JsonParseException, JsonMappingException, IOException {
		Header header = new Header();
		Map<String, Object> fromJson = Jackson.toObject(json, new TypeReference<IdentityHashMap<String, Object>>() {
		});
		for (Entry<String, Object> entry : fromJson.entrySet()) {
			header.put(entry.getKey(), entry.getValue());
		}
		return header;
	}

	//////////////////////// @Override/////////////////////////////////////
	@Override
	public Header put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	///////////////////////////////////////////////////////////
	public String toJson(String _default) {
		try {
			return Jackson.toJson(this);
		} catch (Exception e) {
			log.error("json 解析失败:", e);
			return _default;
		}
	}
}
