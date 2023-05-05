package net.projectmythos.argos.framework.exceptions;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.ComponentLike;
import net.projectmythos.argos.utils.JsonBuilder;

@Data
@NoArgsConstructor
public class ArgosException extends MythosException {
	private JsonBuilder json;

	public ArgosException(JsonBuilder json) {
		super(json.toString());
		this.json = json;
	}

	public ArgosException(ComponentLike component) {
		this(new JsonBuilder(component));
	}

	public ArgosException(String message) {
		this(new JsonBuilder(message));
	}

	public ComponentLike withPrefix(String prefix) {
		return new JsonBuilder(prefix).next(getJson());
	}

}
