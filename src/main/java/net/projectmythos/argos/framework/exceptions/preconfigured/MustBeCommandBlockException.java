package net.projectmythos.argos.framework.exceptions.preconfigured;

public class MustBeCommandBlockException extends PreConfiguredException {

	public MustBeCommandBlockException() {
		super("You must be a command block to use this command");
	}

}
