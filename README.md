# infogen_aop

封装了 javassist 用于 使用 VirtualMachine 的 attach 来动态加载重新类,并对重新加载的类进行AOP

是 infogen 实现 AOP 功能需要的 jar 包

# 使用示例

		AOP.getInstance().add_advice_method(Authc.class, new InfoGen_AOP_Handle_Authc());//基于注解的切面  必须 extends AOP_Handle
		AOP.getInstance().add_autowired_field("com.infogen.infogen_demo.service.Signup", "user_dao", "new com.infogen.infogen_demo.dao.User_DAO_Impl();");//依赖注入
		AOP.getInstance().advice();


版本:  V2.0.01R151022
		1.修改版本号,更新依赖包
版本:  V2.0.01R150929
		1.去掉Return中的get_Success方法，避免某些插件扫描时报错