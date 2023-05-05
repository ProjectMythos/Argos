package net.projectmythos.argos.framework.commands.models.annotations;

import net.projectmythos.argos.utils.TimeUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cooldown {
	TimeUtils.TickTime value();
	double x() default 1;
	boolean global() default false;
	String bypass() default "";

}
