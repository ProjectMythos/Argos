package net.projectmythos.argos.features.afk;

import net.projectmythos.argos.framework.commands.models.CustomCommand;
import net.projectmythos.argos.framework.commands.models.annotations.Aliases;
import net.projectmythos.argos.framework.commands.models.annotations.Arg;
import net.projectmythos.argos.framework.commands.models.annotations.Description;
import net.projectmythos.argos.framework.commands.models.annotations.Path;
import net.projectmythos.argos.utils.TimeUtils.Timespan;
import net.projectmythos.argos.framework.commands.models.events.CommandEvent;
import org.bukkit.entity.Player;

@Aliases("timeafk")
public class AFKTimeCommand extends CustomCommand {

    public AFKTimeCommand(CommandEvent event) {
        super(event);
    }

    @Path("[player]")
    @Description("View how long a player has been AFK")
    void timeAfk(@Arg("self") Player player) {
        String timespan = Timespan.of(AFK.get(player).getTime()).format();
        send(PREFIX + "&3" + nickname(player) + " has been AFK for &e" + timespan);
    }

}
