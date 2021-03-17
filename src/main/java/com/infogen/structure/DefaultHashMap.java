package com.infogen.structure;

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
 * HTTP协议调用端输入参数类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月12日 下午3:56:17
 * @since 1.0
 * @version 1.0
 */
@Slf4j
public class DefaultHashMap extends IdentityHashMap<String, Object> {
	private static final long serialVersionUID = -5436768657673377874L;

	public static DefaultHashMap create() {
		return new DefaultHashMap();
	}

	public static DefaultHashMap create(String key, Object value) {
		return new DefaultHashMap().put(key, value);
	}

	public static DefaultHashMap create(Map<String, List<String>> name_value_pair) {
		DefaultHashMap parameter = new DefaultHashMap();
		name_value_pair.forEach((key, values) -> {
			values.forEach(value -> {
				parameter.put(new String(key), value);
			});
		});
		return parameter;
	}

	public static DefaultHashMap create(String json) throws JsonParseException, JsonMappingException, IOException {
		DefaultHashMap parameter = new DefaultHashMap();
		Map<String, Object> fromJson = Jackson.toObject(json, new TypeReference<IdentityHashMap<String, Object>>() {
		});
		for (Entry<String, Object> entry : fromJson.entrySet()) {
			parameter.put(entry.getKey(), entry.getValue());
		}
		return parameter;
	}

	//////////////////////// @Override/////////////////////////////////////
	@Override
	public DefaultHashMap put(String key, Object value) {
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
