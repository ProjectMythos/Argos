package net.projectmythos.argos.models.nerd;

import com.mongodb.DBObject;
import dev.morphia.annotations.*;
import dev.morphia.converters.UUIDConverter;
import lombok.*;
import net.md_5.bungee.api.ChatColor;
import net.projectmythos.argos.framework.exceptions.postconfigured.InvalidInputException;
import net.projectmythos.argos.framework.interfaces.Colored;
import net.projectmythos.argos.framework.interfaces.IsColoredAndNicknamed;
import net.projectmythos.argos.framework.interfaces.PlayerOwnedObject;
import net.projectmythos.argos.framework.persistence.serializers.mongodb.LocalDateConverter;
import net.projectmythos.argos.framework.persistence.serializers.mongodb.LocalDateTimeConverter;
import net.projectmythos.argos.utils.*;
import net.projectmythos.argos.utils.worldgroup.SubWorldGroup;
import net.projectmythos.argos.utils.worldgroup.WorldGroup;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static net.projectmythos.argos.utils.Nullables.isNullOrEmpty;

@Data
@Entity(value = "nerd", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Converters({UUIDConverter.class, LocalDateConverter.class, LocalDateTimeConverter.class})
public class Nerd implements PlayerOwnedObject, IsColoredAndNicknamed, Colored {

	@Id
	@NonNull
	protected UUID uuid;
	protected String name;
	protected String preferredName;
	protected String prefix;
	protected boolean checkmark;
	protected LocalDate birthday;
	protected LocalDateTime firstJoin;
	protected LocalDateTime lastJoin;
	protected LocalDateTime lastQuit;
	protected LocalDateTime lastUnvanish;
	protected LocalDateTime lastVanish;
	protected LocalDate promotionDate;
	protected String about;

	protected Set<Pronoun> pronouns = new HashSet<>();
	protected List<String> preferredNames = new ArrayList<>();
	protected Set<String> aliases = new HashSet<>();
	protected Set<String> pastNames = new HashSet<>();

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Location location;

	private Set<WorldGroup> visitedWorldGroups = new HashSet<>();
	private Set<SubWorldGroup> visitedSubWorldGroups = new HashSet<>();

	// Set both to null after they have moved
	private Location loginLocation;
	private Location teleportLocation;

	private Location teleportOnLogin;

	protected static final LocalDateTime EARLIEST_JOIN = LocalDateTime.of(2022, 9, 15, 0, 0);

	@PreLoad
	void preLoad(DBObject dbObject) {
		List<String> visitedWorldGroups = (List<String>) dbObject.get("visitedWorldGroups");
		if (visitedWorldGroups != null && visitedWorldGroups.remove("ONEBLOCK"))
			visitedWorldGroups.add("SKYBLOCK");
		List<String> visitedSubWorldGroups = (List<String>) dbObject.get("visitedSubWorldGroups");
		if (visitedSubWorldGroups != null && visitedSubWorldGroups.remove("LEGACY"))
			visitedSubWorldGroups.add("LEGACY1");

		List<String> pronouns = (List<String>) dbObject.get("pronouns");
		if (!isNullOrEmpty(pronouns)) {
			List<String> fixed = new ArrayList<>() {{
				for (String pronoun : pronouns) {
					final Pronoun of = Pronoun.of(pronoun);
					if (of != null)
						add(of.name());
				}
			}};

			fixed.removeIf(Objects::isNull);
			dbObject.put("pronouns", fixed);
		}

		List<String> aliases = (List<String>) dbObject.get("aliases");
		if (!isNullOrEmpty(aliases))
			dbObject.put("aliases", aliases.stream().map(String::toLowerCase).toList());
	}

	@PostLoad
	void fix() {
		if (!isNullOrEmpty(preferredName)) {
			preferredNames.add(preferredName);
			preferredName = null;
		}
	}

	public static Nerd of(String name) {
		return of(PlayerUtils.getPlayer(name));
	}

	public static Nerd of(Player player) {
		return of(player.getUniqueId());
	}

	public static Nerd of(PlayerOwnedObject object) {
		return of(object.getUniqueId());
	}

	public static Nerd of(OfflinePlayer player) {
		return of(player.getUniqueId());
	}

	public static Nerd of(UUID uuid) {
		return (Nerd) new NerdService().get(uuid);
	}

	public static List<Nerd> of(Collection<UUID> uuids) {
		return uuids.stream().map(Nerd::of).collect(Collectors.toList());
	}

	public void fromPlayer(OfflinePlayer player) {
		uuid = player.getUniqueId();
		name = Name.of(uuid);
		if (player.getFirstPlayed() > 0) {
			LocalDateTime newFirstJoin = Utils.epochMilli(player.getFirstPlayed());
			if (firstJoin == null || firstJoin.isBefore(EARLIEST_JOIN) || newFirstJoin.isBefore(firstJoin))
				firstJoin = newFirstJoin;
		}
	}


	@Override
	public @NotNull String getName() {
		String name = "api-" + getUuid();
		if (UUIDUtils.isUUID0(uuid))
			name = "Console";

		if (name.length() <= 16) // ignore "api-<uuid>" names
			Name.put(uuid, name);

		return name;
	}

	// this is just here for the ToString.Include
	@ToString.Include
	@NotNull
	@Override
	public Rank getRank() {
		return PlayerOwnedObject.super.getRank();
	}

	/**
	 * Returns the user's name formatted with a color formatting code
	 * @deprecated you're probably looking for {@link Nerd#getColoredName()}
	 */
	@ToString.Include
	@Deprecated
	public String getNameFormat() {
		return getRank().getChatColor() + getName();
	}

	/**
	 * Returns the user's nickname with their rank color prefixed. Formerly known as getNicknameFormat.
	 */
	@Override
	public @NotNull String getColoredName() {
		return getChatColor() + getNickname();
	}

	public @NotNull Color getColor() {
		return getRank().colored().getColor();
	}

	public JsonBuilder getChatFormat(Chatter viewer) {
		String prefix = getFullPrefix(viewer);

		final ChatColor rankColor = getRank().getChatColor();
//		final JsonBuilder badge = new BadgeUserService().get(this).getBadgeJson(viewer);

		return new JsonBuilder().next(prefix).next(rankColor + getNickname());

	}

	private String getFullPrefix(Chatter viewer) {
//		if (isKoda())
//			return "";

		String prefix = this.prefix;

		if (isNullOrEmpty(prefix))
			prefix = getRank().getPrefix();

//		if (viewer != null)
//			if (getRank().isMod() && new FreezeService().get(viewer).isFrozen())
//				prefix = getRank().getPrefix();

		if (!isNullOrEmpty(prefix))
			prefix = "&8&l[&f" + prefix + "&8&l] ";

		return prefix;
	}

	public LocalDateTime getLastJoin(Player viewer) {
		if (isOnline()) {
			if (PlayerUtils.canSee(viewer, (Player) this))
				return getLastJoin();

			return getLastUnvanish();
		}

		return getLastJoin();
	}

	public void setLastJoin(LocalDateTime when) {
		lastJoin = when;
		lastUnvanish = when;
	}

	public LocalDateTime getLastQuit(Player viewer) {
		if (isOnline()) {
			if (PlayerUtils.canSee(viewer, (Player) this))
				return getLastQuit();

			return getLastVanish();
		}

		return getLastQuit();
	}

	public void setLastQuit(LocalDateTime when) {
		lastQuit = when;
		lastVanish = when;
	}

	@ToString.Include
	public boolean isVanished() {
		if (!isOnline())
			return false;
		return PlayerUtils.isVanished(getOnlinePlayer());
	}

	public @NotNull WorldGroup getWorldGroup() {
		return WorldGroup.of(getLocation());
	}

	public World getWorld() {
		if (isOnline())
			return getOnlinePlayer().getWorld();

		return new NBTPlayer(this).getWorld();
	}

	public @NotNull Location getLocation() {
		if (isOnline())
			return getOnlinePlayer().getPlayer().getLocation();

		return getOfflineLocation();
	}

	public Location getOfflineLocation() {
		if (true)
			return new NBTPlayer(this).getOfflineLocation();

		// TODO 1.19 Remove if nbt is reliable
		if (location != null)
			return location;

		try {
			location = new NBTPlayer(this).getOfflineLocation();
			new NerdService().save(this);
			return location;
		} catch (Exception ex) {
			throw new InvalidInputException("Could not get location of offline player " + name + ": " + ex.getMessage());
		}
	}

	public List<ItemStack> getInventory() {
		if (isOnline())
			return Arrays.asList(getOnlinePlayer().getInventory().getContents());

		return new NBTPlayer(this).getOfflineInventory();
	}

	public List<ItemStack> getEnderChest() {
		if (isOnline())
			return Arrays.asList(getOnlinePlayer().getEnderChest().getContents());

		return new NBTPlayer(this).getOfflineEnderChest();
	}

	public List<ItemStack> getArmor() {
		if (isOnline())
			return Arrays.asList(getOnlinePlayer().getInventory().getArmorContents());

		return new NBTPlayer(this).getOfflineArmor();
	}

	public ItemStack getOffHand() {
		if (isOnline())
			return getOnlinePlayer().getInventory().getItemInOffHand();

		return new NBTPlayer(this).getOfflineOffHand();
	}

	@Data
	public static class StaffMember implements PlayerOwnedObject {
		@NonNull
		private UUID uuid;
	}

	public enum Pronoun {
		SHE_HER,
		THEY_THEM,
		HE_HIM,
		XE_XEM,
		ANY,
		;

		@Override
		public String toString() {
			return format(name());
		}

		public static String format(String input) {
			if (input == null) return null;
			return input.replaceAll("_", "/").toLowerCase();
		}

		public static Pronoun of(String input) {
			if (input == null) return null;
			for (Pronoun pronoun : values())
				if (pronoun.toString().contains(format(input)))
					return pronoun;
			return null;
		}
	}

	public List<String> getFilteredPreferredNames() {
		return preferredNames.stream().filter(name -> !name.equalsIgnoreCase(getNickname())).toList();
	}

}