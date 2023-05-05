package net.projectmythos.argos.utils;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import gg.projecteden.api.interfaces.HasUniqueId;
import gg.projecteden.parchment.HasOfflinePlayer;
import gg.projecteden.parchment.HasPlayer;
import gg.projecteden.parchment.OptionalPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.ComponentLike;
import net.projectmythos.argos.Argos;
import net.projectmythos.argos.framework.exceptions.postconfigured.InvalidInputException;
import net.projectmythos.argos.framework.exceptions.postconfigured.PlayerNotFoundException;
import net.projectmythos.argos.framework.exceptions.postconfigured.PlayerNotOnlineException;
import net.projectmythos.argos.framework.interfaces.PlayerOwnedObject;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static net.projectmythos.argos.utils.Nullables.isNullOrAir;
import static net.projectmythos.argos.utils.Nullables.isNullOrEmpty;
import static net.projectmythos.argos.utils.StringUtils.stripColor;
import static net.projectmythos.argos.utils.UUIDUtils.isUuid;
import static net.projectmythos.argos.utils.Utils.getMin;

public class PlayerUtils {

    public static List<UUID> uuidsOf(Collection<Player> players) {
        return players.stream().map(Player::getUniqueId).toList();
    }

    public static @NotNull OfflinePlayer getPlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public static @NotNull OfflinePlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    public static @NotNull OfflinePlayer getPlayer(Identity identity) {
        return getPlayer(identity.uuid());
    }

    /**
     * Searches for a player whose username or nickname fully or partially matches the given partial name.
     * @param partialName UUID or partial text of a username/nickname
     * @return an offline player
     * @throws InvalidInputException input was null or empty
     * @throws PlayerNotFoundException a player matching that (nick)name could not be found
     */
    public static @NotNull OfflinePlayer getPlayer(String partialName) throws InvalidInputException, PlayerNotFoundException {
        if (partialName == null || partialName.length() == 0)
            throw new InvalidInputException("No player name given");

        String original = partialName;
        partialName = partialName.toLowerCase().trim();

        if (isUuid(partialName))
            return getPlayer(UUID.fromString(partialName));

        final List<Player> players = OnlinePlayers.getAll();

        for (Player player : players)
            if (player.getName().equalsIgnoreCase(partialName))
                return player;
        for (Player player : players)
            if (Nickname.of(player).equalsIgnoreCase((partialName)))
                return player;

        NicknameService nicknameService = new NicknameService();
        Nickname fromNickname = nicknameService.getFromNickname(partialName);
        if (fromNickname != null)
            return fromNickname.getOfflinePlayer();

        for (Player player : players)
            if (player.getName().toLowerCase().startsWith(partialName))
                return player;
        for (Player player : players)
            if (Nickname.of(player).toLowerCase().startsWith((partialName)))
                return player;

        for (Player player : players)
            if (player.getName().toLowerCase().contains((partialName)))
                return player;
        for (Player player : players)
            if (Nickname.of(player).toLowerCase().contains((partialName)))
                return player;

        NerdService nerdService = new NerdService();

        Nerd fromAlias = nerdService.getFromAlias(partialName);
        if (fromAlias != null)
            return fromAlias.getOfflinePlayer();

        List<Nerd> matches = nerdService.find(partialName);
        if (matches.size() > 0) {
            Nerd nerd = matches.get(0);
            if (nerd != null)
                return nerd.getOfflinePlayer();
        }

        throw new PlayerNotFoundException(original);
    }

    public static void send(@Nullable Object recipient, @Nullable Object message, @NotNull Object... objects) {
        if (recipient == null || message == null)
            return;

        if (message instanceof String string && objects.length > 0)
            message = String.format(string, objects);

        if (recipient instanceof CommandSender sender) {
            if (!(message instanceof String || message instanceof ComponentLike))
                message = message.toString();

            if (message instanceof String string)
                sender.sendMessage(new JsonBuilder(string));
            else if (message instanceof ComponentLike componentLike)
                sender.sendMessage(componentLike);
        }

        else if (recipient instanceof OfflinePlayer offlinePlayer) {
            Player player = offlinePlayer.getPlayer();
            if (player != null) {
                send(player, message);
            }
        } else if (recipient instanceof UUID uuid)
            send(getPlayer(uuid), message);

        else if (recipient instanceof Identity identity)
            send(getPlayer(identity), message);

        else if (recipient instanceof Identified identified)
            send(getPlayer(identified.identity()), message);
    }

    public static @NotNull Player getOnlinePlayer(UUID uuid) {
        final OfflinePlayer player = getPlayer(uuid);
        if (!player.isOnline() || player.getPlayer() == null)
            throw new PlayerNotOnlineException(player);
        return player.getPlayer();
    }

    public static @NotNull Player getOnlinePlayer(Identity identity) {
        return getOnlinePlayer(identity.uuid());
    }

    public static MinMaxResult<Player> getNearestPlayer(Location location) {
        return getMin(OnlinePlayers.where().world(location.getWorld()).get(), player -> distance(player, location).get());
    }

    public static MinMaxResult<Player> getNearestVisiblePlayer(Location location, Integer radius) {
        List<Player> players = OnlinePlayers.where().world(location.getWorld()).get().stream()
                .filter(_player -> !GameMode.SPECTATOR.equals(_player.getGameMode()))
                .filter(_player -> !isVanished(_player))
                .collect(toList());

        if (radius > 0)
            players = players.stream().filter(player -> distance(player, location).lte(radius)).collect(toList());

        return getMin(players, player -> distance(player, location).get());
    }

    public static MinMaxResult<Player> getNearestPlayer(Player original) {
        Player _original = original;
        List<Player> players = OnlinePlayers.where().world(_original.getWorld()).get().stream()
                .filter(player -> !isSelf(_original, player)).collect(toList());

        return getMin(players, player -> distance(player, _original).get());
    }

    public enum Dev implements PlayerOwnedObject {
        CYN("1d70383f-21ba-4b8b-a0b4-6c327fbdade1"),
        ;

        @Getter
        private final UUID uuid;

        public static Dev of(UUID uuid) {
            for (Dev dev : values())
                if (dev.getUuid().equals(uuid))
                    return dev;
            return null;
        }

        public @NotNull UUID getUniqueId() {return uuid;}

        Dev(String uuid) {
            this.uuid = UUID.fromString(uuid);
        }

        public void send(Object message) {
            PlayerUtils.send(this, message);
        }

        public void send(String message, Object... args) {
            send(message.formatted(args));
        }

        public void debug(Object message) {
            if (Argos.isDebug())
                PlayerUtils.send(this, message);
        }

        public boolean is(UUID uuid) {
            return this.uuid.equals(uuid);
        }

        public boolean isNot(Player player) {
            return !is(player.getUniqueId());
        }
    }

    public static class OnlinePlayers {
        private UUID viewer;
        private World world;
        private WorldGroup worldGroup;
        private String region;
        private Location origin;
        private Double radius;
        private Boolean afk;
        private Boolean vanished;
        private Predicate<Rank> rank;
        private String permission;
        private List<UUID> include;
        private List<UUID> exclude;
        private List<Predicate<Player>> filters = new ArrayList<>();

        public static OnlinePlayers where() {
            return new OnlinePlayers();
        }

        public static List<Player> getAll() {
            return where().get();
        }

        public OnlinePlayers viewer(Player player) {
            this.viewer = player.getUniqueId();
            return this;
        }

        public OnlinePlayers world(String world) {
            return world(Objects.requireNonNull(Bukkit.getWorld(world)));
        }

        public OnlinePlayers world(World world) {
            this.world = world;
            return this;
        }

        public OnlinePlayers worldGroup(WorldGroup worldGroup) {
            this.worldGroup = worldGroup;
            return this;
        }

        public OnlinePlayers region(String region) {
            this.region = region;
            return this;
        }

        public OnlinePlayers radius(double radius) {
            this.radius = radius;
            return this;
        }

        public OnlinePlayers radius(Location origin, double radius) {
            this.origin = origin;
            this.radius = radius;
            return this;
        }

        public OnlinePlayers afk(boolean afk) {
            this.afk = afk;
            return this;
        }

        public OnlinePlayers vanished(boolean vanished) {
            this.vanished = vanished;
            return this;
        }

        public OnlinePlayers rank(Rank rank) {
            return rank(_rank -> _rank == rank);
        }

        public OnlinePlayers rank(Predicate<Rank> rankPredicate) {
            this.rank = rankPredicate;
            return this;
        }

        public OnlinePlayers hasPermission(String permission) {
            this.permission = permission;
            return this;
        }

        public OnlinePlayers includePlayers(List<Player> players) {
            return include(players.stream().map(Player::getUniqueId).toList());
        }

        public OnlinePlayers include(List<UUID> uuids) {
            if (this.include == null)
                this.include = new ArrayList<>();
            if (uuids == null)
                uuids = new ArrayList<>();

            this.include.addAll(uuids);
            return this;
        }

        public OnlinePlayers excludeSelf() {
            return exclude(viewer);
        }

        public OnlinePlayers excludePlayers(List<Player> players) {
            return exclude(players.stream().map(Player::getUniqueId).toList());
        }

        public OnlinePlayers exclude(Player player) {
            return exclude(List.of(player.getUniqueId()));
        }

        public OnlinePlayers exclude(UUID uuid) {
            return exclude(List.of(uuid));
        }

        public OnlinePlayers exclude(List<UUID> uuids) {
            if (this.exclude == null)
                this.exclude = new ArrayList<>();
            this.exclude.addAll(uuids);
            return this;
        }

        public OnlinePlayers filter(Predicate<Player> filter) {
            this.filters.add(filter);
            return this;
        }

        public List<Player> get() {
            final Supplier<List<UUID>> online = () -> Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(toList());
            final List<UUID> uuids = include == null ? online.get() : include;

            if (uuids.isEmpty())
                return Collections.emptyList();

            Stream<Player> stream = uuids.stream()
                    .filter(uuid -> exclude == null || !exclude.contains(uuid))
                    .map(Bukkit::getOfflinePlayer)
                    .filter(OfflinePlayer::isOnline)
                    .map(OfflinePlayer::getPlayer)
                    .filter(player -> !CitizensUtils.isNPC(player));

            if (origin == null && this.viewer != null) {
                final Player viewer = Bukkit.getPlayer(this.viewer);
                if (viewer != null)
                    origin = viewer.getLocation();
            }

            for (Filter filter : Filter.values())
                stream = filter.filter(this, stream);

            for (Predicate<Player> filter : filters)
                stream = stream.filter(filter);

            return stream.toList();
        }

        public void forEach(Consumer<Player> consumer) {
            get().forEach(consumer);
        }

        @AllArgsConstructor
        private enum Filter {
            //			AFK(
//					search -> search.afk != null,
//					(search, player) -> new AFKUserService().get(player).isAfk() == search.afk),
//			VANISHED(
//					search -> search.vanished != null,
//					(search, player) -> Nerd.of(player).isVanished() == search.vanished),
            RANK(
                    search -> search.rank != null,
                    (search, player) -> search.rank.test(Rank.of(player))),
            PERMISSION(
                    search -> search.permission != null,
                    (search, player) -> player.hasPermission(search.permission)),
            VIEWER(
                    search -> search.viewer != null,
                    (search, player) -> canSee(Bukkit.getPlayer(search.viewer), player)),
            WORLD(
                    search -> search.world != null,
                    (search, player) -> player.getWorld().equals(search.world)),
            WORLDGROUP(
                    search -> search.worldGroup != null,
                    (search, player) -> WorldGroup.of(player) == search.worldGroup),
            REGION(
                    search -> search.world != null && search.region != null,
                    (search, player) -> new WorldGuardUtils(search.world).isInRegion(player, search.region)),
            RADIUS(
                    search -> search.origin != null && search.radius != null,
                    (search, player) -> search.origin.getWorld().equals(player.getWorld()) && distance(player.getLocation(), search.origin).lte(search.radius)),
            ;

            private final Predicate<OnlinePlayers> canFilter;
            private final BiPredicate<OnlinePlayers, Player> predicate;

            private Stream<Player> filter(OnlinePlayers search, Stream<Player> stream) {
                if (!canFilter.test(search))
                    return stream;

                return stream.filter(player -> predicate.test(search, player));
            }
        }
    }

    @Getter
    private static Map<String, Advancement> advancements = new LinkedHashMap<>();

    static {
        Map<String, Advancement> advancements = new LinkedHashMap<>();
        Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
        while (it.hasNext()) {
            Advancement advancement = it.next();
            advancements.put(advancement.getKey().getKey().toLowerCase(), advancement);
        }

        PlayerUtils.advancements = Utils.sortByKey(advancements);
    }

    public static Advancement getAdvancement(String name) {
        name = name.toLowerCase();
        if (advancements.containsKey(name))
            return advancements.get(name);
        throw new InvalidInputException("Advancement &e" + name + " &cnot found");
    }

    public static boolean selectHotbarItem(Player player, ItemStack toSelect) {
        final ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (isNullOrAir(toSelect) || toSelect.equals(mainHand)) {
            return false;
        }

        List<ItemStack> contents = Arrays.stream(getHotbarContents(player)).toList();
        for (int i = 0; i < contents.size(); i++) {
            ItemStack item = contents.get(i);
            if (Nullables.isNullOrAir(item))
                continue;

            if (toSelect.equals(item)) {
                player.getInventory().setHeldItemSlot(i);
                return true;
            }
        }

        return false;

    }

    public static void removeItems(Player player, List<ItemStack> items) {
        for (ItemStack item : items) {
            removeItem(player, item);
        }
    }

    public static void removeItem(Player player, ItemStack item) {
        final Player _player = player.getPlayer();
        final PlayerInventory inv = _player.getInventory();
        inv.removeItem(item);
        if (_player.getItemOnCursor().equals(item))
            _player.setItemOnCursor(null);
    }

    public static void giveItem(Player player, Material material) {
        giveItem(player, material, 1);
    }

    public static void giveItem(Player player, Material material, String nbt) {
        giveItem(player, material, 1, nbt);
    }

    public static void giveItem(Player player, Material material, int amount) {
        giveItem(player, material, amount, null);
    }

    public static void giveItem(Player player, Material material, int amount, String nbt) {
        if (material == Material.AIR)
            throw new InvalidInputException("Cannot spawn air");

        if (amount > 64) {
            for (int i = 0; i < (amount / 64); i++)
                giveItem(player, new ItemStack(material, 64), nbt);
            giveItem(player, new ItemStack(material, amount % 64), nbt);
        } else {
            giveItem(player, new ItemStack(material, amount), nbt);
        }
    }

    public static void giveItem(Player player, ItemStack item) {
        giveItems(player, item);
    }

    public static void giveItems(Player player, ItemStack item) {
        giveItems(player, Collections.singletonList(item));
    }

    public static void giveItem(Player player, ItemStack item, String nbt) {
        giveItems(player, Collections.singletonList(item), nbt);
    }

    public static void giveItems(Player player, Collection<ItemStack> items) {
        giveItems(player, items, null);
    }

    public static void giveItems(Player player, Collection<ItemStack> items, String nbt) {
        List<ItemStack> finalItems = new ArrayList<>(items);
        finalItems.removeIf(Nullables::isNullOrAir);
        finalItems.removeIf(itemStack -> itemStack.getAmount() == 0);
        if (!Nullables.isNullOrEmpty(nbt)) {
            finalItems.clear();
            NBTContainer nbtContainer = new NBTContainer(nbt);
            for (ItemStack item : new ArrayList<>(items)) {
                NBTItem nbtItem = new NBTItem(item);
                nbtItem.mergeCompound(nbtContainer);
                finalItems.add(nbtItem.getItem());
            }
        }
    }

    public static List<ItemStack> giveItemsAndGetExcess(OfflinePlayer player, ItemStack item) {
        return giveItemsAndGetExcess(player, Collections.singletonList(item));
    }

    public static List<ItemStack> giveItemsAndGetExcess(OfflinePlayer player, List<ItemStack> items) {
        if (!player.isOnline() || player.getPlayer() == null)
            return items;

        return giveItemsAndGetExcess(player.getPlayer().getInventory(), items);
    }

    @NotNull
    public static List<ItemStack> giveItemsAndGetExcess(Inventory inventory, List<ItemStack> items) {
        return new ArrayList<>() {{
            for (ItemStack item : fixMaxStackSize(items))
                if (!isNullOrAir(item))
                    addAll(inventory.addItem(item.clone()).values());
        }};
    }

    public static void dropExcessItems(HasPlayer player, List<ItemStack> excess) {
        dropItems(player.getPlayer().getLocation(), excess);
    }

    public static void dropItems(Location location, List<ItemStack> items) {
        if (!isNullOrEmpty(items))
            for (ItemStack item : items)
                if (!isNullOrAir(item) && item.getAmount() > 0)
                    location.getWorld().dropItemNaturally(location, item);
    }

    /**
     * Gets {@link Player}s from a list of {@link HasPlayer}
     * @param hasPlayers list of player containers
     * @return list of players
     */
    public static @NonNull List<Player> getPlayers(List<? extends @NonNull HasPlayer> hasPlayers) {
        return hasPlayers.stream().map(HasPlayer::getPlayer).collect(toList());
    }

    /**
     * Gets {@link Player}s from a list of {@link OptionalPlayer} if they are non null
     * @param hasPlayers list of optional players
     * @return list of non-null players
     */
    public static @NonNull List<@NonNull Player> getNonNullPlayers(Collection<? extends @NonNull OptionalPlayer> hasPlayers) {
        return hasPlayers.stream().map(OptionalPlayer::getPlayer).filter(Objects::nonNull).collect(toList());
    }

    @Getter
    @AllArgsConstructor
    public enum ArmorSlot {
        HELMET(EquipmentSlot.HEAD),
        CHESTPLATE(EquipmentSlot.CHEST),
        LEGGINGS(EquipmentSlot.LEGS),
        BOOTS(EquipmentSlot.FEET),
        ;

        private final EquipmentSlot slot;

        public Material getLeather() {
            return Material.getMaterial("LEATHER_" + name());
        }

    }

    public static List<ItemStack> fixMaxStackSize(List<ItemStack> items) {
        List<ItemStack> fixed = new ArrayList<>();
        for (ItemStack item : items) {
            if (isNullOrAir(item))
                continue;

            final Material material = item.getType();

            while (item.getAmount() > material.getMaxStackSize()) {
                final ItemStack replacement = item.clone();
                final int moving = Math.min(material.getMaxStackSize(), item.getAmount() - material.getMaxStackSize());
                replacement.setAmount(moving);
                item.setAmount(item.getAmount() - moving);

                fixed.add(replacement);
            }
            fixed.add(item);
        }

        return fixed;
    }

    public static void runCommand(CommandSender sender, String commandNoSlash) {
        if (sender == null)
            return;

//		if (sender instanceof Player)
//			Utils.callEvent(new PlayerCommandPreprocessEvent((Player) sender, "/" + command));

        Runnable command = () -> Bukkit.dispatchCommand(sender, commandNoSlash);

        if (Bukkit.isPrimaryThread())
            command.run();
        else
            Tasks.sync(command);
    }

    public static void runCommandAsOp(CommandSender sender, String commandNoSlash) {
        boolean deop = !sender.isOp();
        sender.setOp(true);
        runCommand(sender, commandNoSlash);
        if (deop)
            sender.setOp(false);
    }

    public static void runCommandAsConsole(String commandNoSlash) {
        runCommand(Bukkit.getConsoleSender(), commandNoSlash);
    }

    @NotNull
    public static Set<@NotNull ItemStack> getNonNullInventoryContents(Player player) {
        return Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static ItemStack[] getHotbarContents(Player player) {
        return Arrays.copyOfRange(player.getPlayer().getInventory().getContents(), 0, 9);
    }

    public static void giveItemPreferNonHotbar(Player player, ItemStack item) {
        Set<Integer> openSlots = new HashSet<>();
        for (int i = 9; i < 36; i++) {
            if (isNullOrAir(player.getInventory().getContents()[i]))
                openSlots.add(i);
        }
        if (openSlots.size() > 0)
            player.getInventory().setItem(RandomUtils.randomElement(openSlots), item);
        else
            player.getInventory().addItem(item);
    }

    /**
     * Tests if a player can see a vanished player. Returns false if either player is null.
     *
     * @param viewer player who is viewing
     * @param target target player to check
     * @return true if the target can be seen by the viewer
     */
    @Contract("null, _ -> false; _, null -> false")
    public static boolean canSee(@Nullable Player viewer, @Nullable Player target) {
        if (viewer == null || target == null)
            return false;

        return !isVanished(target) || viewer.hasPermission("pv.see");
    }

    public static List<String> getOnlineUuids() {
        return OnlinePlayers.getAll().stream()
                .map(player -> player.getUniqueId().toString())
                .collect(toList());
    }

    public static boolean isWorldGuardEditing(Player player) {
        return player.hasPermission("worldguard.region.bypass.*");
    }

    public static boolean isVanished(@Nullable Player player) {
        if (player == null) return false;
        for (MetadataValue meta : player.getPlayer().getMetadata("vanished"))
            return (meta.asBoolean());
        return false;
    }

    public static boolean isHidden(@Nullable Player player) {
        if (player == null) return false;

        return isVanished(player) || GameMode.SPECTATOR == player.getPlayer().getGameMode();
    }

    @Contract("null, _ -> false; _, null -> false")
    public static boolean isSelf(@Nullable Player player1, @Nullable Player player2) {
        if (player1 == null || player2 == null)
            return false;

        return isSelf(player1.getUniqueId(), player2.getUniqueId());
    }


    public static boolean isSelf(@Nullable UUID uuid1, @Nullable UUID uuid2) {
        return uuid1 != null && uuid1.equals(uuid2);
    }



}