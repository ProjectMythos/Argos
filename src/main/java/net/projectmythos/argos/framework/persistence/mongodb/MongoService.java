package net.projectmythos.argos.framework.persistence.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import dev.morphia.Datastore;
import dev.morphia.annotations.Entity;
import dev.morphia.mapping.cache.EntityCache;
import dev.morphia.query.Sort;
import dev.morphia.query.UpdateException;
import gg.projecteden.api.interfaces.DatabaseObject;
import gg.projecteden.api.interfaces.HasUniqueId;
import lombok.Getter;
import lombok.SneakyThrows;
import net.projectmythos.argos.API;
import net.projectmythos.argos.Argos;
import net.projectmythos.argos.framework.annotations.ObjectClass;
import net.projectmythos.argos.framework.exceptions.ArgosException;
import net.projectmythos.argos.framework.interfaces.PlayerOwnedObject;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static com.mongodb.MongoClient.getDefaultCodecRegistry;
import static net.projectmythos.argos.utils.ReflectionUtils.subTypesOf;
import static net.projectmythos.argos.utils.UUIDUtils.UUID0;

public abstract class MongoService<T extends DatabaseObject> {
    protected static Datastore database = MongoConnector.connect();
    protected static String _id = "_id";

    @Getter
    private static final Set<Class<? extends MongoService>> services = new HashSet<>(); // = subTypesOf(MongoService.class, MongoService.class.getPackageName() + ".models");
    @Getter
    private static final Map<Class<? extends DatabaseObject>, Class<? extends MongoService>> objectToServiceMap = new HashMap<>();
    @Getter
    private static final Map<Class<? extends MongoService>, Class<? extends DatabaseObject>> serviceToObjectMap = new HashMap<>();

    public static void loadServices() {
        loadServices(Collections.emptySet());
    }

    public static void loadServices(String... packages) {
        loadServices(subTypesOf(MongoService.class, packages));
    }

    public static void loadServices(Set<Class<? extends MongoService>> newServices) {
        services.addAll(newServices);
        for (Class<? extends MongoService> service : services) {
            if (Modifier.isAbstract(service.getModifiers()))
                continue;

            ObjectClass annotation = service.getAnnotation(ObjectClass.class);
            if (annotation == null) {
                Argos.warn(service.getSimpleName() + " does not have @" + ObjectClass.class.getSimpleName() + " annotation");
                continue;
            }

            Argos.log("Service loaded: " + service.getSimpleName());

            objectToServiceMap.put(annotation.value(), service);
            serviceToObjectMap.put(service, annotation.value());
        }
    }

    protected Class<T> getObjectClass() {
        ObjectClass annotation = this.getClass().getAnnotation(ObjectClass.class);
        return annotation == null ? null : (Class<T>) annotation.value();
    }

    public static Class<? extends DatabaseObject> ofService(MongoService mongoService) {
        return ofService(mongoService.getClass());
    }

    public static Class<? extends DatabaseObject> ofService(Class<? extends MongoService> mongoService) {
        return serviceToObjectMap.get(mongoService);
    }

    public static Class<? extends MongoService> ofObject(DatabaseObject object) {
        return ofObject(object.getClass());
    }

    public static Class<? extends MongoService> ofObject(Class<? extends DatabaseObject> object) {
        return objectToServiceMap.get(object);
    }

    public static DBObject serialize(Object object) {
        return database.getMapper().toDBObject(object);
    }

    @SneakyThrows
    public static <C> C deserialize(DBObject dbObject) {
        final String className = (String) dbObject.get("className");
        try {
            final Class<C> clazz = (Class<C>) Class.forName(className);
            final EntityCache entityCache = database.getMapper().createEntityCache();
            return database.getMapper().fromDBObject(database, clazz, dbObject, entityCache);
        } catch (ClassNotFoundException ex) {
            Argos.warn("Could not find class " + className);
            return null;
        }
    }

    public MongoCollection<Document> getCollection() {
        return database.getDatabase().getCollection(this.getObjectClass().getAnnotation(Entity.class).value());
    }

    public abstract Map<UUID, T> getCache();

    public void clearCache() {
        this.getCache().clear();
    }

    public Collection<T> cacheAll() {
        database.createQuery(this.getObjectClass()).find().forEachRemaining(this::cache);
        return this.getCache().values();
    }

    public void cache(T object) {
        if (object != null)
            this.getCache().putIfAbsent(object.getUuid(), object);
    }

    public boolean isCached(T object) {
        return this.getCache().containsKey(object.getUuid());
    }

    public void add(T object) {
        this.cache(object);
        this.save(object);
    }

    public void saveCache() {
        this.saveCache(100);
    }

    public void saveCache(int threadCount) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (T object : new ArrayList<>(this.getCache().values()))
            executor.submit(() -> this.saveSync(object));
    }

    public void saveCacheSync() {
        for (T object : new ArrayList<>(getCache().values()))
            this.saveSync(object);
    }

    private static final JsonWriterSettings jsonWriterSettings = JsonWriterSettings.builder().indent(true).build();

    public String asPrettyJson(UUID uuid) {
        final Document document = this.getCollection().find(new BasicDBObject(Map.of(_id, uuid.toString()))).first();
        if (document == null)
            throw new ArgosException("Could not find matching document");

        return document.toBsonDocument(BsonDocument.class, getDefaultCodecRegistry()).toJson(jsonWriterSettings);
    }

    public T get(String name) {
        return this.get(UUID.fromString(name));
    }

    public T get(Player player) {
        return this.get(player.getUniqueId());
    }

    public T get(PlayerOwnedObject object) {
        return this.get(object.getUniqueId());
    }

    public T get(OfflinePlayer player) {
        return this.get(player.getUniqueId());
    }

    @NotNull
    public T get(UUID uuid) {
//		if (isEnableCache())
        return this.getCache(uuid);
//		else
//			return getNoCache(uuid);
    }

    public T get0() {
        return this.get(UUID0);
    }

    public T getApp() {
        return this.get(API.get().getAppUuid());
    }

    public void edit(String uuid, Consumer<T> consumer) {
        this.edit(this.get(uuid), consumer);
    }

    public void edit(UUID uuid, Consumer<T> consumer) {
        this.edit(this.get(uuid), consumer);
    }

    public void edit(Player player, Consumer<T> consumer) {
        this.edit(this.get(player.getUniqueId()), consumer);
    }

    public void edit(T object, Consumer<T> consumer) {
        consumer.accept(object);
        this.save(object);
    }

    public void edit0(Consumer<T> consumer) {
        this.edit(this.get0(), consumer);
    }

    public void editApp(Consumer<T> consumer) {
        this.edit(this.getApp(), consumer);
    }

    public void save(T object) {
        this.checkType(object);
        this.saveSync(object);
    }

    private void checkType(T object) {
        if (this.getObjectClass() == null) return;
        if (!this.getObjectClass().isAssignableFrom(object.getClass()))
            throw new ArgosException(this.getClass().getSimpleName() + " received wrong class type, expected "
                    + this.getObjectClass().getSimpleName() + ", found " + object.getClass().getSimpleName());
    }

    public void delete(T object) {
        this.checkType(object);
        this.deleteSync(object);
    }

    public void deleteAll() {
        this.deleteAllSync();
    }

    @NotNull
    protected T getCache(UUID uuid) {
        Objects.requireNonNull(this.getObjectClass(), "You must provide an owning class or override get(UUID)");
        if (this.getCache().containsKey(uuid) && this.getCache().get(uuid) == null)
            this.getCache().remove(uuid);
        return this.getCache().computeIfAbsent(uuid, $ -> this.getNoCache(uuid));
    }

    protected T getNoCache(UUID uuid) {
        T object = database.createQuery(this.getObjectClass()).field(_id).equal(uuid).first();
        if (object == null)
            object = this.createObject(uuid);
        if (object == null)
            Argos.log("New instance of " + this.getObjectClass().getSimpleName() + " is null");
        return object;
    }

    protected T createObject(UUID uuid) {
        try {
            Constructor<? extends DatabaseObject> constructor = this.getObjectClass().getDeclaredConstructor(UUID.class);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(uuid);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            ex.printStackTrace();
            throw new ArgosException(this.getClass().getSimpleName() + " does not have a UUID constructor (missing @NonNull on UUID or @RequiredArgsConstructor on class?)");
        }
    }

    public List<T> getPage(int page, int amount) {
        return database.createQuery(this.getObjectClass()).offset((page - 1) * amount).limit(amount).find().toList();
    }

    public List<T> getAll() {
        return database.createQuery(this.getObjectClass()).find().toList();
    }

    public List<T> getAllLimit(int limit) {
        return database.createQuery(this.getObjectClass()).limit(limit).find().toList();
    }

    public List<T> getAllSortedBy(Sort... sorts) {
        return database.createQuery(this.getObjectClass())
                .order(sorts)
                .find().toList();
    }

    public List<T> getAllSortedByLimit(int limit, Sort... sorts) {
        return database.createQuery(this.getObjectClass())
                .order(sorts)
                .limit(limit)
                .find().toList();
    }

    protected boolean deleteIf(T object) {
        return false;
    }

    protected void beforeSave(T object) {
    }

    protected void beforeDelete(T object) {
    }

    public void saveSync(T object) {
        this.beforeSave(object);

        if (this.deleteIf(object)) {
            this.deleteSync(object);
            return;
        }

        this.saveSyncReal(object);
    }

    protected void saveSyncReal(T object) {
        try {
            database.merge(object);
        } catch (UpdateException doesntExistYet) {
            try {
                database.save(object);
            } catch (Exception ex2) {
                this.handleSaveException(object, ex2, "saving");
            }
        } catch (Exception ex3) {
            this.handleSaveException(object, ex3, "updating");
        }
    }

    protected void handleSaveException(T object, Exception ex, String type) {
        String toString = object.toString();
        String extra = toString.length() >= Short.MAX_VALUE ? "" : ": " + toString;
        Argos.warn("Error " + type + " " + object.getClass().getSimpleName() + extra);
        ex.printStackTrace();
    }

    public void deleteSync(T object) {
        this.beforeDelete(object);

        this.getCache().remove(object.getUuid());
        database.delete(object);
        this.getCache().remove(object.getUuid());
        object = null;
    }

    public void deleteAllSync() {
        database.getCollection(this.getObjectClass()).drop();
        this.clearCache();
    }

    @NotNull
    protected <U> List<U> map(AggregateIterable<Document> documents, Class<U> clazz) {
        return new ArrayList<>() {{
            for (Document purchase : documents)
                this.add(database.getMapper().fromDBObject(database, clazz, new BasicDBObject(purchase), null));
        }};
    }

}