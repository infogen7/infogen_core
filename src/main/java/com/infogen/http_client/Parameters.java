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

@Slf4j
public class Parameters extends IdentityHashMap<String, Object> {
	private static final long serialVersionUID = 9087076382410201666L;

	public static Parameters create() {
		return new Parameters();
	}

	public static Parameters create(String key, String value) {
		return new Parameters().put(key, value);
	}

	public static Parameters create(Map<String, List<String>> name_value_pair) {
		Parameters header = new Parameters();
		name_value_pair.forEach((key, values) -> {
			values.forEach(value -> {
				header.put(new String(key), value);
			});
		});
		return header;
	}

	public static Parameters create(String json) throws JsonParseException, JsonMappingException, IOException {
		Parameters header = new Parameters();
		Map<String, Object> fromJson = Jackson.toObject(json, new TypeReference<IdentityHashMap<String, Object>>() {
		});
		for (Entry<String, Object> entry : fromJson.entrySet()) {
			header.put(entry.getKey(), entry.getValue());
		}
		return header;
	}

	//////////////////////// @Override/////////////////////////////////////
	@Override
	public Parameters put(String key, Object value) {
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