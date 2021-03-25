package com.infogen.http_client;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.infogen.http_client.exception.HTTPFailException;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * http调用的工具类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:08:13
 * @since 1.0
 * @version 1.0
 */
@Slf4j
public class InfoGenHTTP {
	// 当使用长轮循时需要注意不能超过此时间
	private static Integer connect_timeout = 3_000;// 连接时间
	private static Integer socket_timeout = 30_000;// 数据传输时间
	private static OkHttpClient client = new OkHttpClient.Builder()//
			.addInterceptor(new GzipRequestInterceptor()).connectTimeout(connect_timeout, TimeUnit.MILLISECONDS).writeTimeout(socket_timeout, TimeUnit.MILLISECONDS).readTimeout(socket_timeout, TimeUnit.MILLISECONDS).build();

	public static void create(OkHttpClient client) {
		InfoGenHTTP.client = client;
	}

	static {
		log.info("初始化HTTP CLIENT");
	}

	// /////////////////////////////////////////////////////////////////get/////////////////////////////////////////////////////////////
	// get 获取 rest 资源
	public static String do_get(String url, Map<String, Object> params, Map<String, Object> headers) throws IOException, HTTPFailException {
		Builder builder = new Request.Builder().url(concat_url_params(url, params));
		add_headers(builder, headers);
		Request request = builder.build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.body().string();
		} else {
			throw new HTTPFailException(response.code(), response.message());
		}
	}

	public static void do_get_async(String url, Map<String, Object> params, Map<String, Object> headers, Callback callback) {
		Builder builder = new Request.Builder().url(concat_url_params(url, params));
		add_headers(builder, headers);
		callback = callback == null ? async_get_callback : callback;
		Request request = builder.build();
		client.newCall(request).enqueue(callback);
	}

	private static String concat_url_params(String url, Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return url;
		}
		Iterator<String> iterator = params.keySet().iterator();
		StringBuilder sbd = new StringBuilder();

		String first_key = iterator.next();
		sbd.append(url).append("?").append(first_key).append("=").append(params.get(first_key));
		while (iterator.hasNext()) {
			String key = iterator.next();
			sbd.append("&").append(key).append("=").append(params.get(key));
		}
		url = sbd.toString();
		return url;
	}
	// ////////////////////////////////////////////////////////post: form json xml///////////////////////////////////////////////////////////////////////////

	public static String do_post(String url, Map<String, Object> params, Map<String, Object> headers) throws IOException, HTTPFailException {
		FormBody body = concat_form_params(params);

		Builder builder = new Request.Builder().url(url);
		add_headers(builder, headers);
		Request request = builder.post(body).build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.body().string();
		} else {
			throw new HTTPFailException(response.code(), response.message());
		}
	}

	public static void do_post_async(String url, Map<String, Object> params, Map<String, Object> headers, Callback callback) throws IOException {
		FormBody body = concat_form_params(params);

		Builder builder = new Request.Builder().url(url);
		add_headers(builder, headers);
		Request request = builder.post(body).build();
		callback = callback == null ? async_post_callback : callback;
		client.newCall(request).enqueue(callback);
	}

	private static FormBody concat_form_params(Map<String, Object> params) {
		FormBody.Builder form_builder = new FormBody.Builder();

		if (params == null || params.isEmpty()) {
			return form_builder.build();
		}

		for (Entry<String, Object> entry : params.entrySet()) {
			form_builder.add(entry.getKey(), String.valueOf(entry.getValue()));
		}
		return form_builder.build();
	}

	/////////////////////////////////////////////////////////////////////////////////
	public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");//

	public static String do_post_json(String url, String json, Map<String, Object> headers) throws IOException, HTTPFailException {
		RequestBody body = RequestBody.create(json, MEDIA_TYPE_JSON);

		Builder builder = new Request.Builder().url(url);
		add_headers(builder, headers);
		Request request = builder.post(body).build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.body().string();
		} else {
			throw new HTTPFailException(response.code(), response.message());
		}
	}

	public static void do_post_json_async(String url, String json, Map<String, Object> headers, Callback callback) throws IOException {
		RequestBody body = RequestBody.create(json, MEDIA_TYPE_JSON);

		Builder builder = new Request.Builder().url(url);
		add_headers(builder, headers);
		Request request = builder.post(body).build();
		if (callback == null) {
			callback = async_post_callback;
		}
		client.newCall(request).enqueue(callback);
	}

	//
	public static final MediaType MEDIA_TYPE_XML = MediaType.parse("text/xml;charset=UTF-8");//

	public static String do_post_xml(String url, String xml, Map<String, Object> headers) throws IOException, HTTPFailException {
		RequestBody body = RequestBody.create(xml, MEDIA_TYPE_XML);

		Builder builder = new Request.Builder().url(url);
		add_headers(builder, headers);
		Request request = builder.post(body).build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.body().string();
		} else {
			throw new HTTPFailException(response.code(), response.message());
		}
	}

	public static void do_post_xml_async(String url, String xml, Map<String, Object> headers, Callback callback) throws IOException {
		RequestBody body = RequestBody.create(xml, MEDIA_TYPE_XML);

		Builder builder = new Request.Builder().url(url);
		add_headers(builder, headers);
		Request request = builder.post(body).build();
		if (callback == null) {
			callback = async_post_callback;
		}
		client.newCall(request).enqueue(callback);
	}

	///////////////////////////////////////////////////////////////////////////////////////
	private static void add_headers(Request.Builder builder, Map<String, Object> headers) {
		if (headers == null) {
			return;
		}
		headers.forEach((key, value) -> {
			if (value != null) {
				builder.header(key, String.valueOf(value));
			}
		});
	}

	private static final Callback async_get_callback = new Callback() {
		@Override
		public void onFailure(Call call, IOException e) {
			Request request = call.request();
			log.error("do_async_get 报错:".concat(request.url().toString()), e);
		}

		@Override
		public void onResponse(Call call, Response response) throws IOException {
			if (response.isSuccessful()) {
			} else {
				log.error("do_async_get 错误-返回非2xx:".concat(response.request().url().toString()));
			}
		}
	};

	private static final Callback async_post_callback = new Callback() {
		@Override
		public void onFailure(Call call, IOException e) {
			Request request = call.request();
			log.error("do_async_post_bytype 报错:".concat(request.url().toString()), e);
		}

		@Override
		public void onResponse(Call call, Response response) throws IOException {
			if (response.isSuccessful()) {
			} else {
				log.error("do_async_post_bytype 错误-返回非2xx:".concat(response.request().url().toString()));
			}
		}
	};
}
