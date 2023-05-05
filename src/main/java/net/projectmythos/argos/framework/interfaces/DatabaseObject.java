package net.projectmythos.argos.framework.interfaces;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface DatabaseObject {
	UUID getUuid();

	default @NotNull UUID getUniqueId() {
		return getUuid();
	}
}
