package net.teamcarbon.carbonkit.commands.Misc;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.modules.MiscModule;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;

@SuppressWarnings("UnusedDeclaration")
public class FreezeCommand extends ModuleCmd {

	public FreezeCommand(Module module) { super(module, "freeze"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!MiscUtils.perm(sender, "carbonkit.misc.freeze")) {
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
			boolean release = MiscModule.isFrozen(pl, true);
			long duration = -1;
			if (args.length > 1 && MiscUtils.isBoolean(args[1]))
				release = MiscUtils.toBoolean(args[1]);
			if (!MiscUtils.perm(Bukkit.getWorlds().get(0), pl, "carbonkit.misc.freeze.immune")) {
				if (release) {
					MiscModule.unfreezePlayer(pl);
					sender.sendMessage(Clr.AQUA + "Player " + Clr.GOLD + pl.getName() + Clr.AQUA + " has been released.");
					if (pl.isOnline() && !CustomMessage.MISC_FREEZE.noPre().equals(""))
						((Player) pl).sendMessage(CustomMessage.MISC_UNFREEZE.noPre());
				} else {
					MiscModule.freezePlayer(pl, duration);
					sender.sendMessage(Clr.AQUA + "Player " + Clr.GOLD + pl.getName() + Clr.AQUA + " has been frozen.");
					if (pl.isOnline() && !CustomMessage.MISC_FREEZE.noPre().equals(""))
						((Player) pl).sendMessage(CustomMessage.MISC_FREEZE.noPre());
				}
			} else {
				sender.sendMessage(Clr.RED + "This player cannot be frozen");
			}
		}
	}

}
