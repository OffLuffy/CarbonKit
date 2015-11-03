package net.teamcarbon.carbonkit.commands.CarbonEssentials;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonlib.Misc.TypeUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.modules.CarbonToolsModule;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;

@SuppressWarnings("UnusedDeclaration")
public class FreezeCommand extends ModuleCmd {

	public FreezeCommand(Module module) { super(module, "freeze"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!mod.perm(sender, "freeze")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		if (args.length < 1) {
			sender.sendMessage(Clr.RED + "/freeze [player]");
		} else {
			OfflinePlayer pl = MiscUtils.getPlayer(args[0], CarbonKit.checkOffline);
			if (pl == null) {
				sender.sendMessage(CustomMessage.GEN_PLAYER_NOT_FOUND.noPre());
				return ;
			}
			boolean release = CarbonToolsModule.isFrozen(pl, true);
			long duration = -1;
			if (args.length > 1 && TypeUtils.isBoolean(args[1]))
				release = TypeUtils.toBoolean(args[1]);
			if (!mod.perm(pl, "freeze.immune")) {
				if (release) {
					CarbonToolsModule.unfreezePlayer(pl);
					sender.sendMessage(Clr.AQUA + "Player " + Clr.GOLD + pl.getName() + Clr.AQUA + " has been released.");
					if (pl.isOnline() && !CustomMessage.CE_FREEZE.noPre().equals(""))
						((Player) pl).sendMessage(CustomMessage.CE_UNFREEZE.noPre());
				} else {
					CarbonToolsModule.freezePlayer(pl, duration);
					sender.sendMessage(Clr.AQUA + "Player " + Clr.GOLD + pl.getName() + Clr.AQUA + " has been frozen.");
					if (pl.isOnline() && !CustomMessage.CE_FREEZE.noPre().equals(""))
						((Player) pl).sendMessage(CustomMessage.CE_FREEZE.noPre());
				}
			} else {
				sender.sendMessage(Clr.RED + "This player cannot be frozen");
			}
		}
	}

}
