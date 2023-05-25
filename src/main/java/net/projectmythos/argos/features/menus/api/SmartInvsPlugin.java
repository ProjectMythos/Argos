/*
 * Copyright 2018-2020 Isaac Montagne
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.projectmythos.argos.features.menus.api;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.projectmythos.argos.features.menus.api.content.InventoryProvider;
import net.projectmythos.argos.features.menus.api.content.InventoryProvider.SmartInventoryHolder;
import net.projectmythos.argos.framework.features.Feature;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public class SmartInvsPlugin extends Feature {

	@Getter
	@Accessors(fluent = true)
	private static InventoryManager manager;

	public static void close(Player player) {
		manager().getInventory(player).ifPresent(inv -> inv.close(player));
	}

	public static boolean isOpen(Class<? extends InventoryProvider> menu, Player player) {
		final Optional<SmartInventory> inv = manager().getInventory(player);
		if (inv.isEmpty())
			return false;

		return menu.equals(inv.get().getProvider().getClass());
	}

	public static boolean isSmartInventory(Inventory inventory) {
		if (inventory.getHolder() instanceof SmartInventoryHolder)
			return true;

		for (SmartInventory smartInventory : SmartInvsPlugin.manager().getInventories().values())
			if (inventory.equals(smartInventory.getProvider().getBukkitInventory()))
				return true;
			else if (inventory == smartInventory.getProvider().getBukkitInventory())
				return true;

		return false;
	}

	@Override
	public void onStart() {
		manager = new InventoryManager();
		manager.init();
	}

	@Override
	public void onStop() {
		manager = null;
	}

}
