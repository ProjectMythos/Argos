package net.projectmythos.argos.framework.exceptions.postconfigured;

import gg.projecteden.api.interfaces.HasUniqueId;

import java.util.UUID;

public class PlayerNotOnlineException extends PostConfiguredException {

	public PlayerNotOnlineException(UUID uuid) {
		super(Nickname.of(uuid) + " is not online");
	}

	public PlayerNotOnlineException(HasUniqueId player) {
		super(Nickname.of(player) + " is not online");
	}

}
