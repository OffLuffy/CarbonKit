package net.teamcarbon.carbonkit.commands.CarbonEssentials;

import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@SuppressWarnings("UnusedDeclaration")
public class HomeCommand extends ModuleCmd {

	public HomeCommand(Module module) { super(module, "home"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage("Not yet implemented");
	}

}
