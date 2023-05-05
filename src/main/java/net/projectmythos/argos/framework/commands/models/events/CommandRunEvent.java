package net.projectmythos.argos.framework.commands.models.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.projectmythos.argos.Argos;
import net.projectmythos.argos.framework.commands.Commands;
import net.projectmythos.argos.framework.commands.models.CustomCommand;
import net.projectmythos.argos.framework.commands.models.annotations.Description;
import net.projectmythos.argos.framework.commands.models.annotations.Path;
import net.projectmythos.argos.framework.exceptions.ArgosException;
import net.projectmythos.argos.framework.exceptions.MythosException;
import net.projectmythos.argos.framework.exceptions.preconfigured.MissingArgumentException;
import net.projectmythos.argos.utils.JsonBuilder;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static net.projectmythos.argos.utils.Nullables.isNullOrEmpty;

@Data
@RequiredArgsConstructor
public class CommandRunEvent extends CommandEvent {
	private Method method;
	private String usage;

	public CommandRunEvent(CommandSender sender, CustomCommand command, String aliasUsed, List<String> args, List<String> originalArgs) {
		super(sender, command, aliasUsed, args, originalArgs, false);
	}

	public void setUsage(Method method) {
		this.method = method;
		Path path = method.getAnnotation(Path.class);
		if (path != null) {
			this.usage = path.value();
			Description desc = method.getAnnotation(Description.class);
			if (desc != null)
				this.usage += " &7- " + desc.value();
		}
	}

	public String getUsageMessage() {
		return "Correct usage: /" + aliasUsed + " " + usage;
	}

	public void handleException(Throwable ex) {
		if (Argos.isDebug()) {
			Argos.debug("Handling command framework exception for " + getSender().getName());
			ex.printStackTrace();
		}

		String PREFIX = command.getPrefix();
		if (isNullOrEmpty(PREFIX))
			PREFIX = Commands.getPrefix(command);

		if (ex instanceof MissingArgumentException) {
			reply(PREFIX + "&c" + getUsageMessage());
			return;
		}

		if (ex.getCause() != null && ex.getCause() instanceof ArgosException nexusException) {
			reply(new JsonBuilder(PREFIX + "&c").next(nexusException.getJson()));
			return;
		}

		if (ex instanceof ArgosException nexusException) {
			reply(new JsonBuilder(PREFIX + "&c").next(nexusException.getJson()));
			return;
		}

		if (ex.getCause() != null && ex.getCause() instanceof MythosException edenException) {
			reply(PREFIX + "&c" + edenException.getMessage());
			return;
		}

		if (ex instanceof MythosException) {
			reply(PREFIX + "&c" + ex.getMessage());
			return;
		}

		if (ex instanceof IllegalArgumentException && ex.getMessage() != null && ex.getMessage().contains("type mismatch")) {
			reply(PREFIX + "&c" + getUsageMessage());
			return;
		}

		reply("&cAn internal error occurred while attempting to execute this command");

		if (ex.getCause() != null && ex instanceof InvocationTargetException)
			ex.getCause().printStackTrace();
		else
			ex.printStackTrace();
	}

}
