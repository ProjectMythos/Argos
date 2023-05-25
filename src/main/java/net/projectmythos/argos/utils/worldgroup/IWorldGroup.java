package net.projectmythos.argos.utils.worldgroup;

import lombok.NonNull;
import org.bukkit.Bukkit;
import net.projectmythos.argos.utils.PlayerUtils.OnlinePlayers;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface IWorldGroup {

		String name();

		List<String> getWorldNames();

		default boolean contains(World world) {
			return contains(world.getName());
		}

		default boolean contains(String world) {
			return getWorldNames().stream().anyMatch(world::equalsIgnoreCase);
		}

		default List<@NonNull World> getWorlds() {
			return getWorldNames().stream().map(Bukkit::getWorld).filter(Objects::nonNull).collect(Collectors.toList());
		}

		default List<@NonNull Player> getPlayers() {
			return getWorlds().stream().map(world -> OnlinePlayers.where().world(world).get()).flatMap(Collection::stream).toList();
		}

	}
