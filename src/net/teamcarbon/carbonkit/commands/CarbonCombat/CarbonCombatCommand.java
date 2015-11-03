package net.teamcarbon.carbonkit.commands.CarbonCombat;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("UnusedDeclaration")
public class CarbonCombatCommand extends ModuleCmd {

	public CarbonCombatCommand(Module module) { super(module, "carboncombat"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		sender.sendMessage(Clr.RED + "Not Yet Implemented");
	}

}
