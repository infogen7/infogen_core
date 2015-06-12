/**
 * 
 */
package com.larrylgq.aop.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	public static long getObjectSize(Object o) {
		return instrumentation.getObjectSize(o);
	}

	/**
	 * 递归计算当前对象占用空间总大小，包括当前类和超类的实例字段大小以及实例字段引用对象大小
	 * 
	 * @param objP
	 * @return
	 * @throws IllegalAccessException
	 */
	public static long getFullObjectSize(Object o) {
		Set<Object> visited = new HashSet<Object>();
		Deque<Object> deque = new ArrayDeque<Object>();
		deque.add(o);
		long size = 0L;
		while (deque.size() > 0) {
			Object obj = deque.poll();
			// sizeOf的时候已经计基本类型和引用的长度，包括数组
			size += skipObject(visited, obj) ? 0L : getObjectSize(obj);
			Class<?> tmpObjClass = obj.getClass();
			if (tmpObjClass.isArray()) {
				// [I , [F 基本类型名字长度是2
				if (tmpObjClass.getName().length() > 2) {
					for (int i = 0, len = Array.getLength(obj); i < len; i++) {
						Object tmp = Array.get(obj, i);
						if (tmp != null) {
							// 非基本类型需要深度遍历其对象
							deque.add(Array.get(obj, i));
						}
					}
				}
			} else {
				while (tmpObjClass != null) {
					Field[] fields = tmpObjClass.getDeclaredFields();
					for (Field field : fields) {
						if (Modifier.isStatic(field.getModifiers()) // 静态不计
								|| field.getType().isPrimitive()) { // 基本类型不重复计
							continue;
						}

						field.setAccessible(true);
						Object fieldValue;
						try {
							fieldValue = field.get(obj);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							continue;
						}
						if (fieldValue == null) {
							continue;
						}
						deque.add(fieldValue);
					}
					tmpObjClass = tmpObjClass.getSuperclass();
				}
			}
		}
		return size;
	}

	/**
	 * String.intern的对象不计；计算过的不计
	 *
	 * @param visited
	 * @param obj
	 * @return
	 */
	static boolean skipObject(Set<Object> visited, Object obj) {
		if (obj instanceof String && obj == ((String) obj).intern()) {
			return true;
		}
		return visited.contains(obj);
	}

	public static void help() {
		System.out.println("eg -> ");
		System.out.println("AOP.getInstance().add_advice_method(Authc.class, new InfoGen_AOP_Handle_Authc());");
		System.out.println("AOP.getInstance().add_autowired_field(\"com.infogen.infogen_demo.service.Signup\", \"user_dao\", \"new com.infogen.infogen_demo.dao.User_DAO_Impl();\");");
		System.out.println("AOP.getInstance().advice();");
	}
}
