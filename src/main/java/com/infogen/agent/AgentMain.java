/**
 * 
 */
package com.infogen.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.infogen.agent.injection.InjectionClass;
import com.infogen.json.Jackson;

import lombok.extern.slf4j.Slf4j;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
@Slf4j
public class AgentMain {
	private static final transient byte[] add_transformer_lock = new byte[0];

	public static void premain(String args, Instrumentation inst) {

	}

	public static void agentmain(String args, Instrumentation inst) {
		synchronized (add_transformer_lock) {
			Class<?>[] all_loaded_classes = inst.getAllLoadedClasses();

			Class<?> agent_cache = null;
			for (Class<?> clazz : all_loaded_classes) {
				if (clazz.getName().equals(AgentCache.class.getName())) {
					agent_cache = clazz;
				}
			}
			if (agent_cache == null) {
				log.error("没有找到 AGENT_CACHE 对象");
				return;
			}

			//
			List<InjectionClass> injection_class_list = new ArrayList<>();
			try {
				Field field = agent_cache.getField("injection_class_map");
				@SuppressWarnings("unchecked")
				Map<String, String> injection_class_map = (Map<String, String>) field.get(agent_cache);

				for (String injection_class_string : injection_class_map.values()) {
					InjectionClass injection_class = Jackson.toObject(injection_class_string, InjectionClass.class);
					injection_class_list.add(injection_class);
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | IOException e) {
				log.error("重新加载class文件失败 :", e);
				e.printStackTrace();
				help();
			}

			//
			for (Class<?> clazz : all_loaded_classes) {
				for (InjectionClass injection_class : injection_class_list) {
					String injection_class_name = injection_class.getClass_name();
					if (clazz.getName().equals(injection_class_name)) {
						try {
							AgentTransformer transformer = new AgentTransformer(clazz, injection_class);
							inst.addTransformer(transformer, true);
							log.info("重新加载class文件 -> " + injection_class_name);
							inst.retransformClasses(clazz);
							inst.removeTransformer(transformer);
						} catch (Exception e) {
							log.error("重新加载class文件失败 :", e);
							e.printStackTrace();
						}
					}
				}
			}
		}

	}

	public static void help() {
		log.info("eg -> ");
		log.info("AOP.getInstance().add_annotation_handle(Authc.class, new InfoGen_AOP_Handle_Authc());");
		log.info("AOP.getInstance().add_injection_field(\"com.infogen.infogen_demo.service.Signup\", \"user_dao\", \"new com.infogen.infogen_demo.dao.User_DAO_Impl();\");");
		log.info("AOP.getInstance().advice();");
	}
}
