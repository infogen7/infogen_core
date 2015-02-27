# infogen_aop

封装了 javassist 用于 使用 VirtualMachine 的 attach 来动态加载重新类,并对重新加载的类进行AOP

是 infogen 实现 AOP 功能需要的 jar 包

# 使用示例

		public void attach(Class<?> clazz) {
		
		String class_name = clazz.getName();
		List<InfoGen_Agent_Advice_Method> methods = new ArrayList<>();
		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			methods.add(infogen_logger_attach(class_name, method.getName(), infogen_logger[0].value()));
		}
		if (methods.isEmpty()) {
			return;
		}

		InfoGen_Agent_Advice infogen_advice = new InfoGen_Agent_Advice();
		infogen_advice.setClass_name(class_name);
		infogen_advice.setMethods(methods);
		InfoGen_Agent.class_advice_map.put(class_name, infogen_advice);
		try {
			loadAgent.invoke(virtualmachine_instance, new Object[] { agent_path, class_name });
		} catch (Exception e) {
			logger.error("注入代码失败", e);
		}
	}

	private InfoGen_Agent_Advice_Method infogen_logger_attach(String class_name, String method_name, String user_defined) {

		InfoGen_Agent_Advice_Method method = new InfoGen_Agent_Advice_Method();

		method.setMethod_name(method_name);

		method.setLong_local_variable("infogen_logger_attach_start_millis");
		method.setInsert_before("infogen_logger_attach_start_millis =System.currentTimeMillis();");

		StringBuilder sbd = new StringBuilder();
		sbd.append("com.infogen.aop.InfoGen_AOP.getInstance().infogen_logger_insert_after_call_back(");
		sbd.append("\"").append(class_name).append("\"").append(",");
		sbd.append("\"").append(method_name).append("\"").append(",");
		sbd.append("\"").append(user_defined).append("\"").append(",");
		sbd.append("infogen_logger_attach_start_millis, System.currentTimeMillis());");
		method.setInsert_after(sbd.toString());

		sbd.setLength(0);
		sbd.append("com.infogen.aop.InfoGen_AOP.getInstance().infogen_logger_add_catch_call_back(");
		sbd.append("\"").append(class_name).append("\"").append(",");
		sbd.append("\"").append(method_name).append("\"").append(",");
		sbd.append("\"").append(user_defined).append("\"").append(",");
		sbd.append("$e);throw $e;");
		method.setAdd_catch(sbd.toString());

		return method;
	}

	public void infogen_logger_insert_after_call_back(String class_name, String method_name, String user_defined, long start_millis, long end_millis) {
		StringBuilder sbd = new StringBuilder();
		sbd.append(class_name).append(",").append(method_name).append(",").append(end_millis - start_millis);
	}

	public void infogen_logger_add_catch_call_back(String class_name, String method_name, String user_defined, Throwable e) {
		StringBuilder sbd = new StringBuilder();
		sbd.append(class_name).append(",").append(method_name).append(",").append(e.getMessage()).append(",").append(Tool_Core.stacktrace(e));
	}
