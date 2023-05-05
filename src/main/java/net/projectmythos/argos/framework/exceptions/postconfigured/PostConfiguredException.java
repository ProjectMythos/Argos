package net.projectmythos.argos.framework.exceptions.postconfigured;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.projectmythos.argos.framework.exceptions.ArgosException;
import net.projectmythos.argos.utils.JsonBuilder;

public class PostConfiguredException extends ArgosException {

	public PostConfiguredException(JsonBuilder json) {
		super(new JsonBuilder(NamedTextColor.RED).next(json));
	}

	public PostConfiguredException(ComponentLike component) {
		this(new JsonBuilder(component));
	}

	public PostConfiguredException(String message) {
		this(new JsonBuilder(message));
	}

}
