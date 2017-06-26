package net.teamcarbon.carbonkit.commands.CarbonWatcher;

import net.teamcarbon.carbonkit.utils.TypeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.modules.CarbonWatcherModule;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonkit.utils.Messages.Clr;
import net.teamcarbon.carbonkit.utils.MiscUtils;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class WatcherCommand extends ModuleCmd {
	public WatcherCommand(Module module) { super(module, "carbonwatcher"); }
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!mod.perm(sender, "toggle", "toggle.others")) {
			sender.sendMessage(mod.getCoreMsg("no-perm", false));
			return;
		}
		if (args.length > 0) {
			if (MiscUtils.eq(args[0], "help", "h", "?")) {
				MiscUtils.printHeader(sender, mod.getName() + " Help");
				if (sender instanceof Player && mod.perm(sender, "toggle"))
					sender.sendMessage(Clr.AQUA + "/cw <on|off>" + Clr.DARKAQUA + " - Toggle your watching state, optionally specify on or off");
				if (mod.perm(sender, "toggle.others"))
					sender.sendMessage(Clr.AQUA + "/cw [player] <on|off>" + Clr.DARKAQUA + " - Toggle a user's command watcher");
			} else if (TypeUtils.isBoolean(args[0])) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(mod.getCoreMsg("not-online", false));
					return;
				}
				if (!mod.perm(sender, "toggle")) {
					sender.sendMessage(mod.getCoreMsg("no-perm", false));
					return;
				}
				CarbonWatcherModule.setWatching((Player) sender, TypeUtils.toBoolean(args[0]));
				sender.sendMessage(mod.getMsg("watch-" + (CarbonWatcherModule.isWatching((Player) sender) ? "enabled" : "disabled"), true));
			} else if (Bukkit.getPlayer(args[0]) != null) {
				if (!mod.perm(sender, "toggle.others")) {
					sender.sendMessage(mod.getCoreMsg("no-perm", false));
					return;
				}
				Player target = Bukkit.getPlayer(args[0]);
				HashMap<String, String> rep = new HashMap<>();
				rep.put("{PLAYER}", target.getName());
				if (args.length > 1 && TypeUtils.isBoolean(args[1]))
					CarbonWatcherModule.setWatching(target, TypeUtils.toBoolean(args[1]));
				else CarbonWatcherModule.toggleWatching(target);
				boolean targetWatching = CarbonWatcherModule.isWatching(target);
				sender.sendMessage(mod.getMsg("watch-" + (targetWatching ? "enabled" : "disabled") + " -others", true, rep));
				target.sendMessage(mod.getMsg("watch-" + (targetWatching ? "enabled" : "disabled"), true));
			}
		} else {
			if (!(sender instanceof Player)) {
				MiscUtils.printHeader(sender, mod.getName() + " Help");
				if (mod.perm(sender, "toggle.others"))
					sender.sendMessage(Clr.AQUA + "/cw [player] <on|off>" + Clr.DARKAQUA + " - Toggle a user's command watcher");
				return;
			}
			if (!mod.perm(sender, "toggle")) {
				sender.sendMessage(mod.getCoreMsg("no-perm", false));
				return;
			}
			CarbonWatcherModule.toggleWatching((Player) sender);
			sender.sendMessage(mod.getMsg("watch-" + (CarbonWatcherModule.isWatching((Player) sender) ? "enabled" : "disabled"), true));
		}
	}
}
