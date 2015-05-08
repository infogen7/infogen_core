package com.infogen.aop.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;

import org.apache.log4j.Logger;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年5月4日 下午2:11:06
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_AOP_Configuration {
	public final Logger logger = Logger.getLogger(InfoGen_AOP_Configuration.class.getName());
	public final static ZoneId zoneid = ZoneId.of("GMT+08:00");
	public final static Charset charset = StandardCharsets.UTF_8;

}
