package net.teamcarbon.carbonkit.commands.CarbonTools;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.TypeUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.modules.CarbonToolsModule;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonkit.utils.Messages.Clr;
import net.teamcarbon.carbonkit.utils.MiscUtils;

@SuppressWarnings("UnusedDeclaration")
public class FreezeCommand extends ModuleCmd {

	public FreezeCommand(Module module) { super(module, "freeze"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!mod.perm(sender, "freeze")) {
			sender.sendMessage(mod.getCoreMsg("no-perm", false));
			return;
		}
		if (args.length < 1) {
			sender.sendMessage(Clr.RED + "/freeze [player]");
		} else {
			OfflinePlayer pl = MiscUtils.getPlayer(args[0], CarbonKit.checkOffline);
			if (pl == null) {
				sender.sendMessage(mod.getCoreMsg("player-not-found", false));
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
					String msg = mod.getMsg("unfreeze-message", false);
					if (pl.isOnline() && !msg.equals("")) ((Player) pl).sendMessage(msg);
				} else {
					CarbonToolsModule.freezePlayer(pl, duration);
					sender.sendMessage(Clr.AQUA + "Player " + Clr.GOLD + pl.getName() + Clr.AQUA + " has been frozen.");
					String msg = mod.getMsg("freeze-message", false);
					if (pl.isOnline() && !msg.equals("")) ((Player) pl).sendMessage(msg);
				}
			} else {
				sender.sendMessage(Clr.RED + "This player cannot be frozen");
			}
		}
	}
}
