/**
 * 
 */
package com.infogen.infogen_aop;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Agent {
	public static Map<String, InfoGen_Agent_Advice_Class> class_advice_map = new HashMap<>();
	private transient static String add_transformer_lock = "";

	public static void agentmain(String class_name, Instrumentation inst) {
		try {
			if (null == class_name || class_name.length() == 0) {
				System.out.println("参数不能为空:");
				help();
				return;
			}

			InfoGen_Agent_Advice_Class infogen_advice = class_advice_map.get(class_name);
			InfoGen_Transformer transformer = new InfoGen_Transformer(infogen_advice);
			synchronized (add_transformer_lock) {
				try {
					Class<?> reload_class = ClassLoader.getSystemClassLoader().loadClass(class_name);

					inst.addTransformer(transformer, true);
					System.out.println("重新加载class文件 -> " + class_name);
					inst.retransformClasses(reload_class);
				} catch (Exception e) {
					System.out.println("重新加载class文件失败 :");
					e.printStackTrace();
				}
				inst.removeTransformer(transformer);
			}
		} catch (Exception e) {
			System.out.println("重新加载class文件失败 :");
			e.printStackTrace();
		}
	}

	public static void premain(String agentArgs, Instrumentation inst) {
	}

	private static void help() {
		System.out.println("eg -> ");
		System.out.println("Infogen_Agent_Advice infogen_advice = new Infogen_Agent_Advice();");
		System.out.println("infogen_advice.setClass_name(class_name);");
		System.out.println("infogen_advice.setMethod_name(method_name);");
		System.out.println("infogen_advice.setInsert_before(\"Integer i = ($w)6;System.out.println($1+i);\");");
		System.out.println("infogen_advice.setInsert_after(\"System.out.println(\"after\");\");");
		System.out.println("infogen_advice.setAdd_catch(\"System.out.println(\"error\");throw $e;\");");
		System.out.println("InfoGen_Agent.class_advice_map.put(class_name, infogen_advice);");
		System.out.println("vm.loadAgent(Infogen_Agent_Path.path(),class_name);");
	}
}
