package net.projectmythos.argos.hooks.glowapi;

import net.projectmythos.argos.hooks.IHook;
import net.projectmythos.argos.utils.GlowUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class GlowAPIHook extends IHook<GlowAPIHook> {

	public void setGlowing(Collection<? extends Entity> entities, GlowUtils.GlowColor color, Collection<? extends Player> receivers) {}

}
