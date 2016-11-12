package net.teamcarbon.carbonkit.commands.CarbonTools;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.NumUtils;
import net.teamcarbon.carbonlib.Misc.TypeUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;

@SuppressWarnings("UnusedDeclaration")
public class DiceCommand extends ModuleCmd {

	public DiceCommand(Module module) { super(module, "dice"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!mod.perm(sender, "dice")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		int min = 1, max = 6;
		if (args.length > 0 ) {
			if (TypeUtils.isInteger(args[0])) {
				try {max = Integer.parseInt(args[0]); } catch (Exception ignore) {}
			} else {
				CustomMessage.printHeader(sender, "Dice Help");
				sender.sendMessage(Clr.AQUA + "/dice" + Clr.DARKAQUA + " - Rolls a number between 1 and 6");
				sender.sendMessage(Clr.AQUA + "/dice [max]" + Clr.DARKAQUA + " - Rolls a number between 1 and max");
				sender.sendMessage(Clr.AQUA + "/dice [max] [min]" + Clr.DARKAQUA + " - Rolls a number between min and max");
				CustomMessage.printFooter(sender);
				return;
			}
		}
		if (args.length > 1 && TypeUtils.isInteger(args[1])) { min = Integer.parseInt(args[1]); }
		int roll = NumUtils.rand(min, max);
		sender.sendMessage(Clr.fromChars("6l") + "[Dice] " + Clr.AQUA + "Between " + min + " and " + max + ", you rolled a " + roll);
	}
}
