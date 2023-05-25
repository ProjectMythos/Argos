package net.projectmythos.argos.framework.interfaces;

import gg.projecteden.api.interfaces.HasUniqueId;
import gg.projecteden.api.interfaces.Nicknamed;
import gg.projecteden.parchment.HasLocation;
import gg.projecteden.parchment.OptionalLocation;
import gg.projecteden.parchment.OptionalPlayerLike;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.ComponentLike;
import net.projectmythos.argos.Argos;
import net.projectmythos.argos.features.afk.AFK;
import net.projectmythos.argos.framework.exceptions.postconfigured.PlayerNotOnlineException;
import net.projectmythos.argos.models.nerd.Nerd;
import net.projectmythos.argos.models.nerd.Rank;
import net.projectmythos.argos.models.nickname.Nickname;
import net.projectmythos.argos.models.nickname.NicknameService;
import net.projectmythos.argos.utils.*;
import net.projectmythos.argos.utils.worldgroup.WorldGroup;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

import static net.projectmythos.argos.utils.Nullables.isNullOrEmpty;
import static net.projectmythos.argos.utils.UUIDUtils.isUUID0;

/**
 * A mongo database object owned by a player
 */
public interface PlayerOwnedObject extends Identified, ForwardingAudience.Single, DatabaseObject, Nicknamed {

	@NotNull UUID getUuid();

	/*
	 * Gets the unique ID of this object. Alias for {@link #getUuid()}, for compatibility with {@link HasUniqueId}.
	 *
	 * @return this object's unique ID
	 */

	@NotNull
	default UUID getUniqueId() {
		return getUuid();
	}

	/*
	 * Gets the unique ID of this object. Alias for {@link #getUuid()}, for compatibility with {@link HasUniqueId}.
	 * @return this object's unique ID
	 */

	/*
	 * Gets the offline player for this object.
	 * <p>
	 * <b>WARNING:</b> This method involves I/O operations to fetch user data which can be costly,
	 * especially if used in a Task. Please consider if {@link #getUuid()}, {@link #getName()},
	 * or {@link #isOnline()} are suitable for your purposes.
	 * </p>
	 * If a method requires {@link OfflinePlayer} and just uses it for {@link #getUniqueId()},
	 * consider changing the parameter of the method to {@link HasUniqueId}.
	 * @return offline player
	 * @deprecated method can be costly and often unnecessary
	 */
	@Deprecated
	default @NotNull OfflinePlayer getOfflinePlayer() {
		return Objects.requireNonNullElseGet(getPlayer(), () -> Bukkit.getOfflinePlayer(getUuid()));
	}

	/**
	 * Gets the online player for this object and returns null if they're not online
	 * @return online player or null
	 */
	default @Nullable Player getPlayer() {
		return Bukkit.getPlayer(getUuid());
	}

	/**
	 * Gets the online player for this object and throws if they're not online
	 * @return online player
	 * @throws PlayerNotOnlineException player is not online
	 */
	default @NotNull Player getOnlinePlayer() throws PlayerNotOnlineException {
		Player player = getPlayer();
		if (player == null)
			throw new PlayerNotOnlineException(getUuid());
		return player;
	}

	default boolean isOnline() {
		return getPlayer() != null;
	}

	default boolean isUuid0() {
		return isUUID0(getUuid());
	}

	//	default boolean isAfk() {
//		return AFK.get(getOnlinePlayer()).isAfk();
//	}
//
//	default boolean isTimeAfk() {
//		return AFK.get(getOnlinePlayer()).isTimeAfk();
//	}
//
	default @NotNull Nerd getNerd() {
		return Nerd.of(this);
	}

	default @NotNull Rank getRank() {
		return Rank.of(this);
	}

	default @NotNull Nerd getOnlineNerd() {
		return Nerd.of(getOnlinePlayer());
	}

	default @NotNull WorldGroup getWorldGroup() {
		return getOnlineNerd().getWorldGroup();
	}

	default Distance distanceTo(Location location) {
		return distance(getOnlinePlayer().getLocation(), location);
	}

	default @NotNull String getName() {
		String name = Name.of(this);
		if (name == null)
			name = Nerd.of(getUuid()).getName();
		return name;
	}

	default @NotNull String getNickname() {
		return Nickname.of(getUuid());
	}

	default Nickname getNicknameData() {
		return new NicknameService().get(getUuid());
	}

	default boolean hasNickname() {
		return !isNullOrEmpty(getNicknameData().getNicknameRaw());
	}

	default void debug(String message) {
		if (Argos.isDebug())
			sendMessage(message);
	}

	default void debug(ComponentLike message) {
		if (Argos.isDebug())
			sendMessage(message);
	}

	default String toPrettyString() {
		return StringUtils.toPrettyString(this);
	}

	default void sendMessage(String message) {
		if (isUUID0(getUuid()))
			Argos.log(message);
		else
			sendMessage(json(message));
	}

//	default void sendOrMail(String message) {
//		if (isUUID0(getUuid())) {
//			Athena.log(message);
//			return;
//		}
//
//		if (isOnline())
//			sendMessage(json(message));
//		else
//			Mail.fromServer(getUuid(), WorldGroup.SURVIVAL, message).send();
//	}

	default void sendMessage(UUID sender, ComponentLike component, MessageType type) {
		if (isUUID0(getUuid()))
			Argos.log(AdventureUtils.asPlainText(component));
		else
			// TODO - 1.19.2 Chat Validation Kick
//			 sendMessage(identityOf(sender), component, type);
			sendMessage(component, type);
	}

	default void sendMessage(UUID sender, ComponentLike component) {
		if (isUUID0(getUuid()))
			Argos.log(AdventureUtils.asPlainText(component));
		else
			// TODO - 1.19.2 Chat Validation Kick
			// sendMessage(identityOf(sender), component);
			sendMessage(component);
	}

	default void sendMessage(int delay, String message) {
		Tasks.wait(delay, () -> sendMessage(message));
	}

	default void sendMessage(int delay, ComponentLike component) {
		Tasks.wait(delay, () -> {
			if (isUUID0(getUuid()))
				Argos.log(AdventureUtils.asPlainText(component));
			else
				sendMessage(component);
		});
	}

	default JsonBuilder json() {
		return json("");
	}

	default JsonBuilder json(String message) {
		return new JsonBuilder(message);
	}

	default @NotNull Identity identity() {
		return Identity.identity(getUuid());
	}

	@Override
	default @NotNull Audience audience() {
		return Objects.requireNonNullElse(getPlayer(), Audience.empty());
	}

}

