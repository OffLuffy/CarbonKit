package net.teamcarbon.carbonkit.commands.CarbonEssentials;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;

@SuppressWarnings("UnusedDeclaration")
public class WarpCommand extends ModuleCmd {

	public WarpCommand(Module module) { super(module, "warp"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage("Not yet implemented");
	}

}
