package net.projectmythos.argos.features.menus;

import lombok.*;
import net.projectmythos.argos.Argos;
import net.projectmythos.argos.features.menus.anvilgui.AnvilGUI;
import net.projectmythos.argos.features.menus.api.ClickableItem;
import net.projectmythos.argos.features.menus.api.ItemClickData;
import net.projectmythos.argos.features.menus.api.annotations.Rows;
import net.projectmythos.argos.features.menus.api.content.InventoryContents;
import net.projectmythos.argos.features.menus.api.content.InventoryProvider;
import net.projectmythos.argos.features.menus.api.content.SlotIterator;
import net.projectmythos.argos.features.menus.api.content.SlotIterator.Type;
import net.projectmythos.argos.features.menus.api.content.SlotPos;
import net.projectmythos.argos.framework.exceptions.ArgosException;
import net.projectmythos.argos.framework.exceptions.MythosException;
import net.projectmythos.argos.framework.exceptions.postconfigured.InvalidInputException;
import net.projectmythos.argos.utils.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.*;

import static net.projectmythos.argos.utils.StringUtils.*;
import static net.projectmythos.argos.utils.UUIDUtils.UUID0;

public abstract class MenuUtils {

    public static final int COLUMNS = 9;

    public static SlotIterator innerSlotIterator(InventoryContents contents) {
        return innerSlotIterator(contents, SlotPos.of(0, 0));
    }

    public static SlotIterator innerSlotIterator(InventoryContents contents, SlotPos start) {
        final SlotIterator slotIterator = contents.newIterator(Type.HORIZONTAL, start);
        final int rows = contents.config().getRows();
        for (int i = 0; i < rows * COLUMNS; i++)
            if (i < COLUMNS || i % COLUMNS == 0 || (i + 1) % COLUMNS == 0 || i >= (rows - 1) * COLUMNS)
                slotIterator.blacklist(i);

        return slotIterator;
    }

    public static int calculateRows(int items) {
        return calculateRows(items, 2, 9);
    }

    public static int calculateRows(int items, int extraRows) {
        return calculateRows(items, extraRows, 9);
    }

    public static int calculateRows(int items, int extraRows, int itemsAcross) {
        return (int) Math.min(6, Math.ceil(Integer.valueOf(items).doubleValue() / itemsAcross) + extraRows);
    }

    public static List<String> getLocationLore(Location location) {
        if (location == null)
            return Collections.singletonList("null");

        return List.of("&3X:&e " + (int) location.getX(), "&3Y:&e " + (int) location.getY(), "&3Z:&e " + (int) location.getZ());
    }

    public static void handleException(Player player, String prefix, Throwable ex) {
        if (ex.getCause() != null && ex.getCause() instanceof ArgosException)
            PlayerUtils.send(player, new JsonBuilder(prefix + "&c").next(((ArgosException) ex.getCause()).getJson()));
        else if (ex instanceof ArgosException)
            PlayerUtils.send(player, new JsonBuilder(prefix + "&c").next(((ArgosException) ex).getJson()));
        else if (ex.getCause() != null && ex.getCause() instanceof MythosException)
            PlayerUtils.send(player, new JsonBuilder(prefix + "&c").next(ex.getCause().getMessage()));
        else if (ex instanceof MythosException)
            PlayerUtils.send(player, new JsonBuilder(prefix + "&c").next(ex.getMessage()));
        else {
            PlayerUtils.send(player, "&cAn internal error occurred while attempting to execute this command");
            ex.printStackTrace();
        }
    }

    public static void centerItems(ClickableItem[] items, InventoryContents contents, int row) {
        centerItems(items, contents, row, true);
    }

    public static void centerItems(ClickableItem[] items, InventoryContents contents, int row, boolean space) {
        if (items.length > 9)
            throw new InvalidInputException("Cannot center more than 9 items on one row");
        int[] even = {3, 5, 1, 7};
        int[] odd = {4, 2, 6, 0, 8};
        int[] noSpace = {4, 3, 5, 2, 6, 1, 7, 0, 8};
        if (items.length < 5 && space)
            if (items.length % 2 == 0)
                for (int i = 0; i < items.length; i++)
                    contents.set(row, even[i], items[i]);
            else
                for (int i = 0; i < items.length; i++)
                    contents.set(row, odd[i], items[i]);
        else
            for (int i = 0; i < items.length; i++)
                contents.set(row, noSpace[i], items[i]);
    }

    public static void openAnvilMenu(Player player, String text, BiFunction<Player, String, AnvilGUI.Response> onComplete, Consumer<Player> onClose) {
        new AnvilGUI.Builder()
                .text(text)
                .onComplete(onComplete)
                .onClose(onClose)
                .plugin(Argos.getInstance())
                .open(player);
    }

    @Builder
    @RequiredArgsConstructor
    public static class AnvilMenu<T> {
        private @NotNull
        final InventoryProvider menu;
        private @NotNull final ItemClickData click;
        private @NotNull final Supplier<@Nullable ?> getter;
        private @NotNull final Consumer<@Nullable T> setter;
        private @Nullable final Predicate<@NotNull String> checker;
        private @NotNull final Function<@NotNull String, @Nullable T> converter;
        /**
         * Runs a method after the {@link #setter} is called, i.e. {@link Arena#write()}
         */
        private @Nullable final Runnable writer;
        private @NotNull final String error;

        public void open() {
            openAnvilMenu(click.getPlayer(), String.valueOf(getter.get()), (p, text) -> {
                try {
                    if (checker != null && checker.test(text)) {
                        setter.accept(converter.apply(text));
                        if (writer != null)
                            writer.run();
                        return AnvilGUI.Response.close();
                    }
                } catch(Exception ignored){}
                PlayerUtils.send(p, error);
                return AnvilGUI.Response.close();
            }, p -> Tasks.wait(1, () -> menu.open(p)));
        }

        public static class AnvilMenuBuilder<T> {
            public void open() {
                build().open();
            }
        }

        public static class IntegerBuilder extends AnvilMenuBuilder<Integer> {
            public IntegerBuilder() {
                super();
                checker(Utils::isInt);
                converter(Integer::parseInt);
                error("Input must be an integer");
            }

            /**
             * Sets the {@link #converter} to ensure inputs are greater than or equal to zero
             */
            @Contract("-> this")
            public IntegerBuilder nonNegativeChecker() {
                checker(text -> Utils.isInt(text) && Integer.parseInt(text) >= 0);
                error("Input must be a non-negative integer");
                return this;
            }

            /**
             * Sets the {@link #converter} to ensure inputs are greater than or equal to zero
             */
            @Contract("-> this")
            public IntegerBuilder positiveChecker() {
                checker(text -> Utils.isInt(text) && Integer.parseInt(text) > 0);
                error("Input must be a positive integer");
                return this;
            }
        }
    }

    // TODO: uncomment if doing custom materials

    @Rows(3)
    @Builder(buildMethodName = "_build")
    @AllArgsConstructor
    public static class ConfirmationMenu extends InventoryProvider {
        @Getter
        @Builder.Default
        private final String title = FontUtils.getMenuTexture("禧", 3) + "&4Are you sure?";
        @Builder.Default
        private final String cancelText = "&cNo";
        private final List<String> cancelLore;
//        @Builder.Default
//        private final ItemStack cancelItem = new ItemBuilder(CustomMaterial.GUI_CLOSE.getNoNamedItem()).dyeColor(ColorType.RED).build();
        @Builder.Default
        private final Consumer<ItemClickData> onCancel = (e) -> e.getPlayer().closeInventory();
        @Builder.Default
        private final String confirmText = "&aYes";
        private final List<String> confirmLore;
//        @Builder.Default
//        private final ItemStack confirmItem = new ItemBuilder(CustomMaterial.GUI_CHECK.getNoNamedItem()).dyeColor(ColorType.LIGHT_GREEN).build();
        @NonNull
        private final Consumer<ItemClickData> onConfirm;
        private final Consumer<ItemClickData> onFinally;
        private final Consumer<InventoryContents> additionalContents;


        public static class ConfirmationMenuBuilder {

            public ConfirmationMenuBuilder titleWithSlot(String title) {
                this.title$value = FontUtils.getMenuTexture("埤", 3) + "&4" + title;
                this.title$set = true;
                return this;
            }

            public ConfirmationMenuBuilder title(String title) {
                this.title$value = FontUtils.getMenuTexture("禧", 3) + "&4" + title;
                this.title$set = true;
                return this;
            }

            public ConfirmationMenuBuilder displayItem(ItemStack item) {
                this.additionalContents = contents -> contents.set(0, 4, ClickableItem.empty(item));
                return this;
            }

            public void open(Player player) {
                Tasks.sync(() -> _build().open(player));
            }

            @Deprecated
            public ConfirmationMenu build() {
                throw new UnsupportedOperationException("Use open(player)");
            }

        }

        @Override
        public void init() {
            ItemBuilder cancel = new ItemBuilder(cancelItem).name(cancelText).lore(cancelLore);
            ItemBuilder confirm = new ItemBuilder(confirmItem).name(confirmText).lore(confirmLore);

            contents.set(1, 2, ClickableItem.of(cancel.build(), e -> {
                try {
                    if (onCancel != null)
                        onCancel.accept(e);

                    if (title.equals(e.getPlayer().getOpenInventory().getTitle()))
                        e.getPlayer().closeInventory();

                    if (onFinally != null)
                        onFinally.accept(e);
                } catch (Exception ex) {
                    MenuUtils.handleException(viewer, "", ex);
                }
            }));

            contents.set(1, 6, ClickableItem.of(confirm.build(), e -> {
                try {
                    onConfirm.accept(e);

                    if (colorize(title).equals(e.getPlayer().getOpenInventory().getTitle()))
                        e.getPlayer().closeInventory();

                    if (onFinally != null)
                        onFinally.accept(e);
                } catch (Exception ex) {
                    MenuUtils.handleException(viewer, "", ex);
                }
            }));

            if (additionalContents != null)
                additionalContents.accept(contents);
        }
    }

    // TODO: JOBS - Temporary menu until jobs are complete
    @Rows(3)
    @Builder(buildMethodName = "_build")
    @AllArgsConstructor
    public static class SurvivalNPCShopMenu extends InventoryProvider {
        @Getter
        @Builder.Default
        private final String title = "Shop";
        private final int npcId;
        private final Map<ItemStack, Double> products;

        private final BankerService bankerService = new BankerService();

        public static class SurvivalNPCShopMenuBuilder {
            public void open(Player player) {
                Tasks.sync(() -> _build().open(player));
            }

            @Deprecated
            public SurvivalNPCShopMenu build() {
                throw new UnsupportedOperationException("Use open(player)");
            }
        }

        @Override
        public void init() {
            addCloseItem();

            final List<ClickableItem> items = new ArrayList<>();

            products.forEach((item, price) -> {
                final boolean canAfford = bankerService.get(viewer).has(price, ShopGroup.SURVIVAL);
                final ItemBuilder displayItem = new ItemBuilder(item).lore("&3Price: " + (canAfford ? "&a" : "&c") + prettyMoney(price));

                items.add(ClickableItem.of(displayItem, e -> {
                    if (canAfford)
                        ConfirmationMenu.builder()
                                .onConfirm(e2 -> {
                                    try {
                                        bankerService.withdraw(TransactionCause.MARKET_PURCHASE.of(null, viewer, BigDecimal.valueOf(-price), ShopGroup.SURVIVAL, pretty(item)));
                                        PlayerUtils.giveItem(viewer, item);
                                        Shop.log(UUID0, viewer.getUniqueId(), ShopGroup.SURVIVAL, pretty(item).split(" ", 2)[1], 1, ExchangeType.SELL, String.valueOf(price), "");
                                    } catch (Exception ex) {
                                        MenuUtils.handleException(viewer, StringUtils.getPrefix("Jobs"), ex);
                                    }
                                })
                                .onFinally(e2 -> refresh())
                                .open(viewer);
                }));
            });

            paginator().items(items).perPage(18).build();
        }
    }

    public static void formatInventoryContents(InventoryContents contents, ItemStack[] inventory) {
        formatInventoryContents(contents, inventory, true);
    }

    public static void formatInventoryContents(InventoryContents contents, ItemStack[] inventory, boolean editable) {
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        contents.set(4, 4, ClickableItem.empty(redPane.clone(), "&eArmor ➝"));
        contents.set(4, 1, ClickableItem.empty(redPane.clone(), "&e← Offhand"));
        contents.outline(4, 2, 4, 3, ClickableItem.empty(redPane.clone(), "&e⬇ Hot Bar ⬇"));

        if (inventory == null || inventory.length == 0)
            return;

        // Hotbar
        for (int i = 0; i < 9; i++) {
            if (editable)
                contents.setEditable(5, i, true);

            if (inventory[i] == null)
                continue;

            contents.set(5, i, ClickableItem.empty(inventory[i]));
        }

        // Inventory
        int row = 1;
        int column = 0;
        for (int i = 9; i < 36; i++) {
            if (editable)
                contents.setEditable(row, column, true);

            if (inventory[i] != null)
                contents.set(row, column, ClickableItem.empty(inventory[i]));

            if (column != 8)
                ++column;
            else {
                column = 0;
                ++row;
            }
        }

        // Offhand
        if (editable)
            contents.setEditable(4, 0, true);

        if (inventory[40] != null)
            contents.set(4, 0, ClickableItem.empty(inventory[40]));

        // Armor
        column = 8;
        for (int i = 36; i < 40; i++) {
            if (editable)
                contents.setEditable(4, column, true);

            if (inventory[i] != null)
                contents.set(4, column, ClickableItem.empty(inventory[i]));
            --column;
        }
    }

}
