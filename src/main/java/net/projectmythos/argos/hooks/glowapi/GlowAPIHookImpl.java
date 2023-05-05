package net.projectmythos.argos.hooks.glowapi;

import net.projectmythos.argos.utils.GlowUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.inventivetalent.glow.GlowAPI;

import java.util.Collection;

public class GlowAPIHookImpl extends GlowAPIHook {

	@Override
	public void setGlowing(Collection<? extends Entity> entities, GlowUtils.GlowColor color, Collection<? extends Player> receivers) {
		GlowAPI.setGlowing(entities, color == null ? null : GlowAPI.Color.valueOf(color.name()), receivers);
	}

}
