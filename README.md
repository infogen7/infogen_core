# infogen_aop

封装了 javassist 用于 使用 VirtualMachine 的 attach 来动态加载重新类,并对重新加载的类进行AOP

是 infogen 实现 AOP 功能需要的 jar 包

# 使用示例

	private void infogen_logger_attach(String class_name, String method_name, String user_defined) throws AgentLoadException, AgentInitializationException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		InfoGen_Agent_Advice infogen_advice = new InfoGen_Agent_Advice();
		infogen_advice.setClass_name(class_name);
		infogen_advice.setMethod_name(method_name);

		infogen_advice.setLong_local_variable("infogen_logger_attach_start_millis");
		infogen_advice.setInsert_before("infogen_logger_attach_start_millis =System.currentTimeMillis();");

		StringBuilder sbd = new StringBuilder();
		sbd.append("com.infogen.InfoGen_AOP.getInstance().infogen_logger_insert_after_call_back(");
		sbd.append("\"").append(class_name).append("\"").append(",");
		sbd.append("\"").append(method_name).append("\"").append(",");
		sbd.append("\"").append(user_defined).append("\"").append(",");
		sbd.append("infogen_logger_attach_start_millis, System.currentTimeMillis());");
		infogen_advice.setInsert_after(sbd.toString());

		sbd.setLength(0);
		sbd.append("com.infogen.InfoGen_AOP.getInstance().infogen_logger_add_catch_call_back(");
		sbd.append("\"").append(class_name).append("\"").append(",");
		sbd.append("\"").append(method_name).append("\"").append(",");
		sbd.append("\"").append(user_defined).append("\"").append(",");
		sbd.append("$e);throw $e;");
		infogen_advice.setAdd_catch(sbd.toString());

		loadAgent.invoke(virtualmachine_instance, new Object[] { agent_path, InfoGen_Agent_Base64.to_base64(infogen_advice).replace("\n", "") });
	}

	public void infogen_logger_insert_after_call_back(String class_name, String method_name, String user_defined, long start_millis, long end_millis) {
		StringBuilder sbd = new StringBuilder();
		sbd.append(class_name).append(",").append(method_name).append(",").append(end_millis - start_millis);
		producer.send(new KeyedMessage<String, String>(InfoGen_Configuration.logger_topic_execution_time, class_name, sbd.toString()));
	}

	public void infogen_logger_add_catch_call_back(String class_name, String method_name, String user_defined, Throwable e) {
		StringBuilder sbd = new StringBuilder();
		sbd.append(class_name).append(",").append(method_name).append(",").append(e.getMessage()).append(",").append(Tool_Core.stacktrace(e));
		producer.send(new KeyedMessage<String, String>(InfoGen_Configuration.logger_topic_exception, class_name, sbd.toString()));
	}
