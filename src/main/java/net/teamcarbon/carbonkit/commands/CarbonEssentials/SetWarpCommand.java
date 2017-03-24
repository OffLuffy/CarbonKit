package net.teamcarbon.carbonkit.commands.CarbonEssentials;

import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@SuppressWarnings("UnusedDeclaration")
public class SetWarpCommand extends ModuleCmd {

	public SetWarpCommand(Module module) { super(module, "setwarp"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage("Not yet implemented");
	}

}
