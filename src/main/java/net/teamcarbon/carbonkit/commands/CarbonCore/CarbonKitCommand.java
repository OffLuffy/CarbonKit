package net.teamcarbon.carbonkit.commands.CarbonCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;

@SuppressWarnings("UnusedDeclaration")
public class CarbonKitCommand extends ModuleCmd {

	public CarbonKitCommand(Module module) { super(module, "carbonkit"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage("Not yet implemented");
	}

}
