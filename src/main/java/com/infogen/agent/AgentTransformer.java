/**
 * 
 */
package com.infogen.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import com.infogen.agent.injection.InjectionClass;
import com.infogen.agent.injection.InjectionField;
import com.infogen.agent.injection.InjectionMethod;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import lombok.extern.slf4j.Slf4j;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
@Slf4j
public class AgentTransformer implements ClassFileTransformer {
	private InjectionClass injection_class = null;
	private Class<?> clazz = null;
	private ClassPool class_pool = ClassPool.getDefault();

	public AgentTransformer(Class<?> clazz, InjectionClass injection_class) {
		this.clazz = clazz;
		this.injection_class = injection_class;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (!className.equals(injection_class.getClass_name().replace(".", "/"))) {
			return classfileBuffer;
		}
		String class_name = injection_class.getClass_name();
		try {
			class_pool.insertClassPath(new ClassClassPath(clazz));// war包下使用必须
			CtClass ct_class = class_pool.get(class_name);
			ct_class.defrost();

			// 注入属性
			Set<InjectionField> fields = injection_class.getFields();
			for (InjectionField injection_field : fields) {
				String insertAfter = new StringBuilder("this.").append(injection_field.getField_name()).append(" = ").append(injection_field.getValue()).toString();
				CtConstructor[] ct_constructor_array = ct_class.getConstructors();
				for (CtConstructor ct_constructor : ct_constructor_array) {
					log.info(insertAfter);
					ct_constructor.insertAfter(insertAfter);
				}
			}

			// 注入方法
			Set<InjectionMethod> methods = injection_class.getMethods();
			for (InjectionMethod injection_method : methods) {
				CtMethod ct_method = null;

				//
				CtMethod[] ct_method_array = ct_class.getDeclaredMethods(injection_method.getMethod_name());
				for (CtMethod tmp_ct_method : ct_method_array) {
					CtClass[] ct_parameterTypes = tmp_ct_method.getParameterTypes();
					StringBuilder stringbuilder = new StringBuilder();
					for (CtClass ct_type : ct_parameterTypes) {
						stringbuilder.append(ct_type.getName()).append(" ");
					}
					if (stringbuilder.toString().trim().equals(injection_method.getMethod_parameters())) {
						ct_method = tmp_ct_method;
					}
				}

				//
				if (ct_method == null) {
					continue;
				}
				String long_local_variable = injection_method.getLong_local_variable();
				if (long_local_variable != null) {
					ct_method.addLocalVariable(long_local_variable, CtClass.longType);
				}
				String insert_before = injection_method.getInsert_before();
				if (insert_before != null) {
					ct_method.insertBefore(insert_before);
				}
				String insert_after = injection_method.getInsert_after();
				if (insert_after != null) {
					ct_method.insertAfter(insert_after);
				}
				String add_catch = injection_method.getAdd_catch();
				if (add_catch != null) {
					CtClass ctClass = class_pool.get("java.lang.Throwable");
					ct_method.addCatch(add_catch, ctClass);
				}
				String set_exception_types = injection_method.getSet_exception_types();
				if (set_exception_types != null) {
					String[] split = set_exception_types.trim().split(",");
					CtClass[] types = new CtClass[split.length];
					for (int i = 0; i < split.length; i++) {
						types[i] = class_pool.get(split[i]);
					}
					ct_method.setExceptionTypes(types);
				}
			}

			return ct_class.toBytecode();
		} catch (Throwable e) {
			log.info("transform 字节码文件错误 :");
			log.info("1:请检查是不是javassist jar包冲突,例如hibernate等都会包含一个版本较低的javassist依赖 (使用maven可以尝试将infogen的jar包放到dependencies列表的最上方)");
			log.info("2:如有需要将返回值($_)作为参数调用回调方法,返回值必须是对象类型,否则会报类似");
			log.info("javassist.CannotCompileException: [source error] insert_after_call_back(boolean) not found in com.infogen.tracking.event_handle.InfoGenExecutionHandle");
			log.info("的错误.");
			e.printStackTrace();
			log.error("", e);
			return classfileBuffer;
		}

	}
}