package net.projectmythos.argos.framework.exceptions.postconfigured;

import net.kyori.adventure.text.ComponentLike;
import net.projectmythos.argos.utils.JsonBuilder;

public class InvalidInputException extends PostConfiguredException {

	public InvalidInputException(JsonBuilder json) {
		super(json);
	}

	public InvalidInputException(ComponentLike component) {
		this(new JsonBuilder(component));
	}

	public InvalidInputException(String message) {
		this(new JsonBuilder(message));
	}

}
