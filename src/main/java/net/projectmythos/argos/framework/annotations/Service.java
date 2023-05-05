package net.projectmythos.argos.framework.annotations;

import gg.projecteden.api.interfaces.DatabaseObject;
import net.projectmythos.argos.framework.persistence.mongodb.MongoService;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    Class<? extends MongoService<? extends DatabaseObject>> value();

}
