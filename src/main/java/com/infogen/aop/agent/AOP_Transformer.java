/**
 * 
 */
package com.infogen.aop.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class AOP_Transformer implements ClassFileTransformer {

	private Agent_Advice_Class infogen_advice = null;
	private Class<?> reload_class = null;
	private ClassPool class_pool = ClassPool.getDefault();

	public AOP_Transformer(Agent_Advice_Class infogen_advice, Class<?> reload_class) {
		this.infogen_advice = infogen_advice;
		this.reload_class = reload_class;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (!className.equals(infogen_advice.getClass_name().replace(".", "/"))) {
			return classfileBuffer;
		}
		String class_name = infogen_advice.getClass_name();
		try {
			class_pool.insertClassPath(new ClassClassPath(reload_class));// war包下使用必须
			CtClass ct_class = class_pool.get(class_name);
			ct_class.defrost();

			//
			Set<Agent_Advice_Field> fields = infogen_advice.getFields();
			for (Agent_Advice_Field infoGen_Agent_Advice_Field : fields) {
				String insertAfter = new StringBuilder("this.").append(infoGen_Agent_Advice_Field.getField_name()).append(" = ").append(infoGen_Agent_Advice_Field.getValue()).toString();
				CtConstructor[] constructors = ct_class.getConstructors();
				for (CtConstructor ctConstructor : constructors) {
					ctConstructor.insertAfter(insertAfter);
				}
			}

			//
			Set<Agent_Advice_Method> methods = infogen_advice.getMethods();
			for (Agent_Advice_Method infogen_agent_advice_method : methods) {
				System.out.println(ct_class.getName());
				System.out.println(infogen_agent_advice_method.getMethod_name());
				CtMethod[] declaredMethods = ct_class.getDeclaredMethods(infogen_agent_advice_method.getMethod_name());
				for (CtMethod ct_method : declaredMethods) {
					CtClass[] parameterTypes = ct_method.getParameterTypes();
					StringBuilder stringbuilder = new StringBuilder();
					for (CtClass type : parameterTypes) {
						stringbuilder.append(type.getName()).append(" ");
					}
					if (stringbuilder.toString().trim().equals(infogen_agent_advice_method.getMethod_parameters())) {
						String long_local_variable = infogen_agent_advice_method.getLong_local_variable();
						if (long_local_variable != null) {
							ct_method.addLocalVariable(long_local_variable, CtClass.longType);
						}
						String insert_before = infogen_agent_advice_method.getInsert_before();
						if (insert_before != null) {
							ct_method.insertBefore(insert_before);
						}
						String insert_after = infogen_agent_advice_method.getInsert_after();
						if (insert_after != null) {
							ct_method.insertAfter(insert_after);
						}
						String add_catch = infogen_agent_advice_method.getAdd_catch();
						if (add_catch != null) {
							CtClass ctClass = class_pool.get("java.lang.Throwable");
							ct_method.addCatch(add_catch, ctClass);
						}
						String set_exception_types = infogen_agent_advice_method.getSet_exception_types();
						if (set_exception_types != null) {
							String[] split = set_exception_types.trim().split(",");
							CtClass[] types = new CtClass[split.length];
							for (int i = 0; i < split.length; i++) {
								types[i] = class_pool.get(split[i]);
							}
							ct_method.setExceptionTypes(types);
						}

					}
				}
			}

			return ct_class.toBytecode();
		} catch (Throwable e) {
			System.out.println("transform 字节码文件错误 :");
			e.printStackTrace();
			return classfileBuffer;
		}

	}
}