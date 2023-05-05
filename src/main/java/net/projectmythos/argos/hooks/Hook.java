package net.projectmythos.argos.hooks;

import lombok.Getter;
import lombok.SneakyThrows;
import net.projectmythos.argos.hooks.glowapi.GlowAPIHook;
import net.projectmythos.argos.hooks.glowapi.GlowAPIHookImpl;
import net.projectmythos.argos.utils.Utils;
import org.bukkit.Bukkit;

import static net.projectmythos.argos.Argos.singletonOf;


@Getter
public class Hook {

	//TODO: uncomment if doing viaversion/citizens hooks

//	public static final ViaVersionHook VIAVERSION = hook("ViaVersion", ViaVersionHook.class, ViaVersionHookImpl.class);
	public static final GlowAPIHook GLOWAPI = hook("GlowAPI", GlowAPIHook.class, GlowAPIHookImpl.class);
//	public static final CitizensHook CITIZENS = hook("Citizens", CitizensHook.class, CitizensHookImpl.class);

	@SneakyThrows
	private static <T extends IHook<?>> T hook(String plugin, Class<? extends IHook<T>> defaultImpl, Class<? extends IHook<T>> workingImpl) {
		final IHook<T> hook;

		if (isEnabled(plugin))
			hook = singletonOf(workingImpl);
		else
			hook = singletonOf(defaultImpl);

		Utils.tryRegisterListener(hook);
		return (T) hook;
	}

	public static boolean isEnabled(String plugin) {
		return Bukkit.getServer().getPluginManager().isPluginEnabled(plugin);
	}

}
