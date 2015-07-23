/**
 * 
 */
package com.larrylgq.aop.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;

import com.larrylgq.aop.tools.Tool_Jackson;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class AOP_Agent {
	private transient static String add_transformer_lock = "";

	public static void agentmain(String args, Instrumentation inst) {
		instrumentation = inst;
		// Caused by: java.lang.ClassCastException: com.infogen.aop.agent.InfoGen_Agent_Advice_Class cannot be cast to com.infogen.aop.agent.InfoGen_Agent_Advice_Class
		Agent_Advice_Class infogen_agent_advice_class = null;
		synchronized (add_transformer_lock) {
			Class<?>[] allLoadedClasses = inst.getAllLoadedClasses();
			for (Class<?> loadedClasse : allLoadedClasses) {
				if (loadedClasse.getName().equals("com.larrylgq.aop.agent.Agent_Cache")) {
					try {
						Field field = loadedClasse.getField("class_advice_map");
						@SuppressWarnings("unchecked")
						Map<String, String> class_advice_map = (Map<String, String>) field.get(loadedClasse);
						for (String infogen_advice : class_advice_map.values()) {
							infogen_agent_advice_class = Tool_Jackson.toObject(infogen_advice, Agent_Advice_Class.class);
							for (Class<?> clazz : allLoadedClasses) {
								String class_name = infogen_agent_advice_class.getClass_name();
								if (clazz.getName().equals(class_name)) {
									try {
										AOP_Transformer transformer = new AOP_Transformer(infogen_agent_advice_class, clazz);
										inst.addTransformer(transformer, true);
										System.out.println("重新加载class文件 -> " + class_name);
										inst.retransformClasses(clazz);
										inst.removeTransformer(transformer);
									} catch (Exception e) {
										System.out.println("重新加载class文件失败 :");
										e.printStackTrace();
									}
								}
							}
						}
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | IOException e) {
						System.out.println("重新加载class文件失败 :");
						e.printStackTrace();
						help();
					}
				}
			}
		}

	}

	private static Instrumentation instrumentation;

	public static void premain(String args, Instrumentation inst) {
		instrumentation = inst;
	}

	/**
	 * Calls java.lang.instrument.Instrument.getObjectSize(object).
	 * 
	 * @param object
	 *            the object to size
	 * @return an implementation-specific approximation of the amount of storage consumed by the specified object.
	 * 
	 */
	public static long sizeOf(Object object) {
		if (instrumentation == null) {
			return -1;
		}

		return instrumentation.getObjectSize(object);
	}

	/**
	 * Calculates full size of object iterating over its hierarchy graph.
	 * 
	 * @param obj
	 *            object to calculate size of
	 * @return object size
	 */
	public static long deepSizeOf(Object obj) {
		Map<Object, Object> visited = new IdentityHashMap<Object, Object>();
		long result = internalSizeOf(obj, visited);
		visited.clear();
		return result;
	}

	private static long internalSizeOf(Object obj, Map<Object, Object> visited) {
		if (obj == null || visited.containsKey(obj)) {
			return 0;
		}
		visited.put(obj, null);

		long result = 0;
		// get size of object + primitive variables + member pointers
		result += sizeOf(obj);

		// process all array elements
		Class<?> clazz = obj.getClass();
		if (clazz.isArray()) {
			if (clazz.getName().length() != 2) {// skip primitive type array
				int length = Array.getLength(obj);
				for (int i = 0; i < length; i++) {
					result += internalSizeOf(Array.get(obj, i), visited);
				}
			}
			return result;
		}

		// process all fields of the object
		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (!Modifier.isStatic(fields[i].getModifiers())) {
					if (fields[i].getType().isPrimitive()) {
						continue; // skip primitive fields
					} else {
						fields[i].setAccessible(true);
						try {
							// objects to be estimated are put to stack
							Object objectToAdd = fields[i].get(obj);
							result += internalSizeOf(objectToAdd, visited);
						} catch (IllegalAccessException ex) {
						}
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		return result;
	}

	public static void help() {
		System.out.println("eg -> ");
		System.out.println("AOP.getInstance().add_advice_method(Authc.class, new InfoGen_AOP_Handle_Authc());");
		System.out.println("AOP.getInstance().add_autowired_field(\"com.infogen.infogen_demo.service.Signup\", \"user_dao\", \"new com.infogen.infogen_demo.dao.User_DAO_Impl();\");");
		System.out.println("AOP.getInstance().advice();");
	}
}
