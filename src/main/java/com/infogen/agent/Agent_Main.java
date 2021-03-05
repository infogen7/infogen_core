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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infogen.agent.advice.Agent_Advice_Class;
import com.infogen.json.Jackson;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class Agent_Main {
	private final static Logger LOGGER = LoggerFactory.getLogger(Agent_Main.class);

	private transient static String add_transformer_lock = "";

	public static void agentmain(String args, Instrumentation inst) {
		// Caused by: java.lang.ClassCastException:
		// com.infogen.aop.agent.InfoGen_Agent_Advice_Class cannot be cast to
		// com.infogen.aop.agent.InfoGen_Agent_Advice_Class
		synchronized (add_transformer_lock) {
			Class<?>[] all_loaded_classes = inst.getAllLoadedClasses();

			//
			List<Agent_Advice_Class> agent_advice_class_list = new ArrayList<>();
			Boolean has = false;
			for (Class<?> loaded_classe : all_loaded_classes) {
				if (loaded_classe.getName().equals(Agent_Cache.class.getName())) {
					has = true;
					Class<?> agent_cache = loaded_classe;

					try {
						Field field = agent_cache.getField("class_advice_map");
						@SuppressWarnings("unchecked")
						Map<String, String> class_advice_map = (Map<String, String>) field.get(agent_cache);

						for (String infogen_advice : class_advice_map.values()) {
							Agent_Advice_Class infogen_agent_advice_class = Jackson.toObject(infogen_advice, Agent_Advice_Class.class);
							agent_advice_class_list.add(infogen_agent_advice_class);
						}
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | IOException e) {
						LOGGER.error("重新加载class文件失败 :", e);
						e.printStackTrace();
						help();
					}
				}
			}
			if (!has) {
				LOGGER.error("没有找到 AGENT_CACHE 对象");
				return;
			}

			//
			for (Agent_Advice_Class agent_advice_class : agent_advice_class_list) {
				for (Class<?> clazz : all_loaded_classes) {
					String advice_class_name = agent_advice_class.getClass_name();
					if (clazz.getName().equals(advice_class_name)) {
						try {
							Agent_Transformer transformer = new Agent_Transformer(agent_advice_class, clazz);
							inst.addTransformer(transformer, true);
							LOGGER.info("重新加载class文件 -> " + advice_class_name);
							inst.retransformClasses(clazz);
							inst.removeTransformer(transformer);
						} catch (Exception e) {
							LOGGER.error("重新加载class文件失败 :", e);
							e.printStackTrace();
						}
					}
				}
			}
		}

	}

	public static void help() {
		LOGGER.info("eg -> ");
		LOGGER.info("AOP.getInstance().add_advice_method(Authc.class, new InfoGen_AOP_Handle_Authc());");
		LOGGER.info("AOP.getInstance().add_autowired_field(\"com.infogen.infogen_demo.service.Signup\", \"user_dao\", \"new com.infogen.infogen_demo.dao.User_DAO_Impl();\");");
		LOGGER.info("AOP.getInstance().advice();");
	}
}
