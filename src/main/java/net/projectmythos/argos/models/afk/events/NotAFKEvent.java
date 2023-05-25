package net.projectmythos.argos.models.afk.events;

import net.projectmythos.argos.models.afk.AFKUser;

public class NotAFKEvent extends AFKEvent {

	public NotAFKEvent(AFKUser user) {
		super(user);
	}

}
