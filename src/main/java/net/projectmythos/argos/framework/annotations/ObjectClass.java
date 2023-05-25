package net.projectmythos.argos.framework.annotations;

import net.projectmythos.argos.models.nerd.Nerd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectClass {
    Class<Nerd> value();

}
