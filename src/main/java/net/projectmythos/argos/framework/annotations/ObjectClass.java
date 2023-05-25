package net.projectmythos.argos.framework.annotations;

import gg.projecteden.api.interfaces.DatabaseObject;
import net.projectmythos.argos.models.warps.Warps;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectClass {
    Class<? extends DatabaseObject> value();

}

