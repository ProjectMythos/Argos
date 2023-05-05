package net.projectmythos.argos.framework.persistence.mongodb;

import net.projectmythos.argos.framework.interfaces.PlayerOwnedObject;
import net.projectmythos.argos.utils.PlayerUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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

}