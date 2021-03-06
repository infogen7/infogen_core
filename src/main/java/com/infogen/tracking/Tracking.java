package com.infogen.tracking;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 统计方法执行时间和调用链记录的注解
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年4月2日 下午12:06:40
 * @since 1.0
 * @version 1.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tracking {
	public String user_definition() default "";
}
