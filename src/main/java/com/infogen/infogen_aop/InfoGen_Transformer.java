/**
 * 
 */
package com.infogen.infogen_aop;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2015年2月10日 下午6:28:44
 */
public class InfoGen_Transformer implements ClassFileTransformer {
	private transient String transform_lock = "";

	private Map<String, byte[]> class_name_map = new HashMap<>();
	private InfoGen_Agent_Advice infogen_advice = null;
	private ClassPool class_pool = ClassPool.getDefault();

	public InfoGen_Transformer(InfoGen_Agent_Advice infogen_advice) {
		this.infogen_advice = infogen_advice;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (!className.equals(infogen_advice.getClass_name().replace(".", "/"))) {
			return classfileBuffer;
		}
		String class_name = infogen_advice.getClass_name();
		synchronized (transform_lock) {
			byte[] bytecode = class_name_map.get(class_name);
			if (bytecode != null) {
				return bytecode;
			} else {
				try {
					CtClass ct_class = class_pool.get(class_name);
					CtMethod ct_method = ct_class.getDeclaredMethod(infogen_advice.getMethod_name());
					String long_local_variable = infogen_advice.getLong_local_variable();
					if (long_local_variable != null) {
						ct_method.addLocalVariable(long_local_variable, CtClass.longType);
					}
					String insert_before = infogen_advice.getInsert_before();
					if (insert_before != null) {
						ct_method.insertBefore(insert_before);
					}
					String insert_after = infogen_advice.getInsert_after();
					if (insert_after != null) {
						ct_method.insertAfter(insert_after);
					}
					String add_catch = infogen_advice.getAdd_catch();
					if (add_catch != null) {
						CtClass ctClass = class_pool.get("java.lang.Throwable");
						ct_method.addCatch(add_catch, ctClass);
					}
					class_name_map.put(class_name, ct_class.toBytecode());
					return class_name_map.get(class_name);
				} catch (Throwable e) {
					System.out.println("transform 字节码文件错误 :");
					e.printStackTrace();
					return classfileBuffer;
				}
			}
		}

	}
}