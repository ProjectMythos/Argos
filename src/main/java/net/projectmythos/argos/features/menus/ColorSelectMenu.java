package net.projectmythos.argos.features.menus;

import lombok.RequiredArgsConstructor;
import net.projectmythos.argos.features.menus.api.ClickableItem;
import net.projectmythos.argos.features.menus.api.ItemClickData;
import net.projectmythos.argos.features.menus.api.annotations.Title;
import net.projectmythos.argos.features.menus.api.content.InventoryProvider;
import net.projectmythos.argos.utils.ColorType;
import org.bukkit.Material;

import java.util.function.Consumer;

import static net.projectmythos.argos.utils.StringUtils.camelCase;

@Title("Select Color")
@RequiredArgsConstructor
public class ColorSelectMenu extends InventoryProvider {
	private final Material type;
	private final Consumer<ItemClickData> onClick;

	@Override
	public void init() {
		addCloseItem();

		int row = 1;
		int column = 0;

		for (ColorType color : ColorType.values()) {
			if (color.getDyeColor() == null) continue;
			contents.set(row, column, ClickableItem.of(color.switchColor(type), "&e" + camelCase(color.getName()), onClick));
			if (column == 8) {
				column = 0;
				row++;
			} else
				column++;
		}
	}
}
