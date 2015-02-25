# infogen_aop

封装了 javassist 用于 使用 VirtualMachine 的 attach 来动态加载重新类,并对重新加载的类进行AOP

是 infogen 实现 AOP 功能需要的 jar 包

# 使用示例

		InfoGen_Agent_Advice infogen_advice = new InfoGen_Agent_Advice();
		infogen_advice.setClass_name(class_name);
		infogen_advice.setMethod_name(method_name);
		StringBuilder sbd = new StringBuilder();

		infogen_advice.setLong_local_variable("infogen_logger_attach_start_millis");
		infogen_advice.setInsert_before("infogen_logger_attach_start_millis =System.currentTimeMillis();");
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
