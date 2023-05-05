package net.projectmythos.argos.models.nickname;

import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.converters.UUIDConverter;
import lombok.*;
import net.projectmythos.argos.framework.interfaces.PlayerOwnedObject;
import net.projectmythos.argos.utils.UUIDUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.*;

import static net.projectmythos.argos.utils.Nullables.isNullOrEmpty;

@Getter
@Builder
@Entity(value = "nickname", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Converters(UUIDConverter.class)
public class Nickname implements PlayerOwnedObject {
	@Id
	@NonNull
	protected UUID uuid;

	protected String nickname;

	public static String of(String name) {
		return of(UUID.fromString(name));
	}

	public static String of(Player player) {
		return of(player.getUniqueId());
	}

	public static String of(PlayerOwnedObject object) {
		return of(object.getUniqueId());
	}

	public static String of(OfflinePlayer player) {
		return of(player.getUniqueId());
	}

	public static String of(Nerd nerd) {
		return of(nerd.getUniqueId());
	}

	public static String of(UUID uuid) {
		return new NicknameService().get(uuid).getNickname();
	}

	public @NotNull String getNickname() {
		if (UUIDUtils.isUUID0(uuid))
			return "Console";
		if (isNullOrEmpty(nickname))
			return getName();
		return nickname;
	}

	public String getNicknameRaw() {
		return nickname;
	}

	public boolean hasNickname() {
		return !isNullOrEmpty(nickname);
	}

}
