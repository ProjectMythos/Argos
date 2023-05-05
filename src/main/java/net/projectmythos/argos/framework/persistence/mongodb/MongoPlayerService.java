package net.projectmythos.argos.framework.persistence.mongodb;

import net.projectmythos.argos.framework.exceptions.postconfigured.PlayerNotFoundException;
import net.projectmythos.argos.framework.interfaces.PlayerOwnedObject;
import net.projectmythos.argos.utils.PlayerUtils;
import net.projectmythos.argos.utils.UUIDUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public abstract class MongoPlayerService<T extends PlayerOwnedObject> extends MongoBukkitService<T> {
    @Override
    protected String pretty(T object) {
        return object.getNickname();
    }

    public List<T> getOnline() {
        List<T> online = new ArrayList<>();
        for (Player player : PlayerUtils.OnlinePlayers.getAll())
            online.add(get(player));
        return online;
    }

    public void saveOnline() {
        for (T user : getOnline())
            save(user);
    }

    public void saveOnlineSync() {
        for (T user : getOnline())
            saveSync(user);
    }

    public T get(String name) {
        Nerd nerd = new NerdService().findExact(name);
        if (nerd == null)
            throw new PlayerNotFoundException(name);
        return get(nerd);
    }

    public void saveSync(T object) {
        if (!isUuidValid(object))
            return;

        super.saveSync(object);
    }

    public void deleteSync(T object) {
        if (!isUuidValid(object))
            return;

        super.deleteSync(object);
    }

    private static final Function<UUID, Boolean> isV4 = UUIDUtils::isV4Uuid;
    private static final Function<UUID, Boolean> is0 = UUIDUtils::isUUID0;
    private static final Function<UUID, Boolean> isApp = UUIDUtils::isAppUuid;

    private boolean isUuidValid(T object) {
        final UUID uuid = object.getUuid();
        return isV4.apply(uuid) || is0.apply(uuid) || isApp.apply(uuid);
    }

}