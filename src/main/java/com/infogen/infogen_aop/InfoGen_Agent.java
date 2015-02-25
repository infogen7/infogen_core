/**
 * 
 */
package com.infogen.infogen_aop;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

/**
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2015年2月10日 下午6:28:06
 */
public class InfoGen_Agent {
	private transient static String add_transformer_lock = "";

	public static void agentmain(String agentArgs, Instrumentation inst) {

		if (null == agentArgs || agentArgs.length() == 0) {
			System.out.println("参数不能为空:");
			help();
			return;
		}

		InfoGen_Agent_Advice infogen_advice = null;
		try {
			infogen_advice = (InfoGen_Agent_Advice) InfoGen_Agent_Base64.to_Object(agentArgs, InfoGen_Agent_Advice.class);
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("参数转换失败 :");
			help();
			e.printStackTrace();
			return;
		}

		String class_name = infogen_advice.getClass_name();
		if (null == class_name || class_name.length() == 0) {
			System.out.println("class_name 参数为空:");
			help();
			return;
		}
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
	}

	public static void premain(String agentArgs, Instrumentation inst) {
	}

	private static void help() {
		System.out.println("eg -> ");
		System.out.println("Infogen_Agent_Advice infogen_advice = new Infogen_Agent_Advice();");
		System.out.println("infogen_advice.setClass_name(\"com.infogen.Service\");");
		System.out.println("infogen_advice.setMethod_name(\"aop\");");
		System.out.println("infogen_advice.setInsert_before(\"Integer i = ($w)6;System.out.println($1+i);\");");
		System.out.println("infogen_advice.setInsert_after(\"System.out.println(\"after\");\");");
		System.out.println("infogen_advice.setAdd_catch(\"System.out.println(\"error\");throw $e;\");");
		System.out.println("vm.loadAgent(Infogen_Agent_Path.path(), Infogen_Agent_Base64.to_base64(infogen_advice));");
	}
}
