package net.projectmythos.argos.framework.exceptions.preconfigured;

import net.md_5.bungee.api.ChatColor;
import net.projectmythos.argos.framework.exceptions.ArgosException;

public class PreConfiguredException extends ArgosException {

	public PreConfiguredException(String message) {
		super(ChatColor.RED + message);
	}
}
