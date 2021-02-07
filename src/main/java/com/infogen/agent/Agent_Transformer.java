/**
 * 
 */
package com.infogen.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.agent.advice.Agent_Advice_Class;
import com.infogen.agent.advice.Agent_Advice_Field;
import com.infogen.agent.advice.Agent_Advice_Method;

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
public class Agent_Transformer implements ClassFileTransformer {
	private final static Logger LOGGER = LogManager.getLogger(Agent_Transformer.class.getName());

	private Agent_Advice_Class infogen_advice = null;
	private Class<?> reload_class = null;
	private ClassPool class_pool = ClassPool.getDefault();

	public Agent_Transformer(Agent_Advice_Class infogen_advice, Class<?> reload_class) {
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

			// 注入属性
			Set<Agent_Advice_Field> fields = infogen_advice.getFields();
			for (Agent_Advice_Field infoGen_Agent_Advice_Field : fields) {
				String insertAfter = new StringBuilder("this.").append(infoGen_Agent_Advice_Field.getField_name()).append(" = ").append(infoGen_Agent_Advice_Field.getValue()).toString();
				CtConstructor[] constructors = ct_class.getConstructors();
				for (CtConstructor ctConstructor : constructors) {
					LOGGER.info(insertAfter);
					ctConstructor.insertAfter(insertAfter);
				}
			}

			// 注入方法
			Set<Agent_Advice_Method> methods = infogen_advice.getMethods();
			for (Agent_Advice_Method infogen_agent_advice_method : methods) {
				CtMethod ct_method = null;

				//
				CtMethod[] declaredMethods = ct_class.getDeclaredMethods(infogen_agent_advice_method.getMethod_name());
				for (CtMethod tmp_ct_method : declaredMethods) {
					CtClass[] parameterTypes = tmp_ct_method.getParameterTypes();
					StringBuilder stringbuilder = new StringBuilder();
					for (CtClass type : parameterTypes) {
						stringbuilder.append(type.getName()).append(" ");
					}
					if (stringbuilder.toString().trim().equals(infogen_agent_advice_method.getMethod_parameters())) {
						ct_method = tmp_ct_method;
					}
				}

				//
				if (ct_method == null) {
					continue;
				}
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

			return ct_class.toBytecode();
		} catch (Throwable e) {
			LOGGER.info("transform 字节码文件错误 :");
			LOGGER.info("1:请检查是不是javassist jar包冲突,例如hibernate等都会包含一个版本较低的javassist依赖 (使用maven可以尝试将infogen的jar包放到dependencies列表的最上方)");
			LOGGER.info("2:如有需要将返回值($_)作为参数调用回调方法,返回值必须是对象类型,否则会报类似");
			LOGGER.info("javassist.CannotCompileException: [source error] insert_after_call_back(boolean) not found in com.infogen.tracking.event_handle.InfoGen_AOP_Handle_Execution");
			LOGGER.info("的错误.");
			e.printStackTrace();
			LOGGER.error(e);
			return classfileBuffer;
		}

	}
}