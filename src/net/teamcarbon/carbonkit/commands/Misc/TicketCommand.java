package net.teamcarbon.carbonkit.commands.Misc;

import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;

@SuppressWarnings("UnusedDeclaration")
public class TicketCommand extends ModuleCmd {

	public TicketCommand(Module module) { super(module, "ticket"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!MiscUtils.perm(sender, "carbonkit.ticket.create")) {
			
		}
		sender.sendMessage("Not yet implemented");
	}

}
