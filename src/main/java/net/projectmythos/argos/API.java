package net.projectmythos.argos;

import com.google.gson.GsonBuilder;
import dev.morphia.converters.TypeConverter;
import net.projectmythos.argos.framework.persistence.mongodb.MongoConnector;
import net.projectmythos.argos.utils.Env;
import org.bukkit.Location;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static net.projectmythos.argos.utils.ReflectionUtils.subTypesOf;

public class API {

    protected static API instance;

    public API() {
        instance = this;
    }

    public static API get() {
        return instance;
    }

    public static Optional<API> getOptional() {
        return Optional.of(instance);
    }

    public void shutdown() {
        MongoConnector.shutdown();
    }

    public Env getEnv() {
        return Argos.getEnv();
    }

    public ClassLoader getClassLoader() {
        return Argos.class.getClassLoader();
    }

    public DatabaseConfig getDatabaseConfig() {
        return DatabaseConfig.builder()
                .link(Argos.getInstance().getConfig().getString("databases.mongodb.link"))
                .host(Argos.getInstance().getConfig().getString("databases.mongodb.host"))
                .username(Argos.getInstance().getConfig().getString("databases.mongodb.username"))
                .modelPath("net.projectmythos.argos.models")
                .env(getEnv())
                .build();
    }

    public Collection<? extends Class<? extends TypeConverter>> getDefaultMongoConverters() {
        return subTypesOf(TypeConverter.class, MongoConnector.class.getPackageName() + ".serializers");
    }

    public Collection<? extends Class<? extends TypeConverter>> getMongoConverters() {
        return subTypesOf(TypeConverter.class, ItemStackConverter.class.getPackageName());
    }

    public GsonBuilder getPrettyPrinter() {
        return new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(Location.class, new LocationGsonSerializer())
                .registerTypeAdapter(LocalDate.class, new LocalDateGsonSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeGsonSerializer());
    }

    public String getAppName() {
        return getClass().getSimpleName();
    }

    public UUID getAppUuid() {
        return UUID.nameUUIDFromBytes(getAppName().getBytes());
    }

}

