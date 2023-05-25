package net.projectmythos.argos.models.afk.events;

import net.projectmythos.argos.models.afk.AFKUser;

public class NowAFKEvent extends AFKEvent {

	public NowAFKEvent(AFKUser user) {
		super(user);
	}

}
