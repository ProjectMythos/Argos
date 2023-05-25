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

package net.projectmythos.argos.features.menus.api.opener;

import com.google.common.base.Preconditions;
import net.projectmythos.argos.features.menus.api.InventoryManager;
import net.projectmythos.argos.features.menus.api.SmartInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class ChestInventoryOpener implements InventoryOpener {

	@Override
	public Inventory open(SmartInventory inv, Player player) {
		Preconditions.checkArgument(inv.getColumns() == 9,
			"The column count for the chest inventory must be 9, found: %s.", inv.getColumns());
		Preconditions.checkArgument(inv.getRows() >= 1 && inv.getRows() <= 6,
			"The row count for the chest inventory must be between 1 and 6, found: %s", inv.getRows());

		InventoryManager manager = inv.getManager();
		Inventory inventory = Bukkit.createInventory(inv.getProvider().getHolder(), inv.getRows() * inv.getColumns(), inv.getTitle());
		inv.getProvider().setBukkitInventory(inventory);

		manager.getContents(player).ifPresent(contents -> fill(inventory, contents, player));
		manager.getSelfContents(player).ifPresent(selfContents -> {
			if (selfContents.anyPresent()) {
				manager.setRealContents(player, player.getInventory().getContents());
				player.getInventory().clear();
				fill(player.getInventory(), selfContents, player);
				final int firstEmpty = player.getInventory().firstEmpty();
				if (firstEmpty < 9) {
					manager.setHeldSlot(player, player.getInventory().getHeldItemSlot());
					player.getInventory().setHeldItemSlot(firstEmpty);
				}
			}
		});

		player.openInventory(inventory);
		return inventory;
	}

	@Override
	public boolean supports(InventoryType type) {
		return type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST;
	}

}
