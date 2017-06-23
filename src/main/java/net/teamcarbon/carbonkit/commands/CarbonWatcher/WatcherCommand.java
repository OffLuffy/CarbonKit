package net.teamcarbon.carbonkit.commands.CarbonWatcher;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
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
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		if (args.length > 0) {
			if (MiscUtils.eq(args[0], "help", "h", "?")) {
				CustomMessage.printHeader(sender, mod.getName() + " Help");
				if (sender instanceof Player && mod.perm(sender, "toggle"))
					sender.sendMessage(Clr.AQUA + "/cw <on|off>" + Clr.DARKAQUA + " - Toggle your watching state, optionally specify on or off");
				if (mod.perm(sender, "toggle.others"))
					sender.sendMessage(Clr.AQUA + "/cw [player] <on|off>" + Clr.DARKAQUA + " - Toggle a user's command watcher");
			} else if (TypeUtils.isBoolean(args[0])) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
					return;
				}
				if (!mod.perm(sender, "toggle")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				CarbonWatcherModule.setWatching((Player) sender, TypeUtils.toBoolean(args[0]));
				sender.sendMessage(CarbonWatcherModule.isWatching((Player) sender) ? CustomMessage.CW_WATCH_ENABLED.pre() : CustomMessage.CW_WATCH_DISABLED.pre());
			} else if (Bukkit.getPlayer(args[0]) != null) {
				if (!mod.perm(sender, "toggle.others")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				Player target = Bukkit.getPlayer(args[0]);
				HashMap<String, String> rep = new HashMap<>();
				rep.put("{PLAYER}", target.getName());
				if (args.length > 1 && TypeUtils.isBoolean(args[1]))
					CarbonWatcherModule.setWatching(target, TypeUtils.toBoolean(args[1]));
				else
					CarbonWatcherModule.toggleWatching(target);
				if (CarbonWatcherModule.isWatching(target)) {
					sender.sendMessage(CustomMessage.CW_WATCH_ENABLED_OTHER.pre(rep));
					target.sendMessage(CustomMessage.CW_WATCH_ENABLED.pre());
				} else {
					sender.sendMessage(CustomMessage.CW_WATCH_DISABLED_OTHER.pre(rep));
					target.sendMessage(CustomMessage.CW_WATCH_DISABLED.pre());
				}
			}
		} else {
			if (!(sender instanceof Player)) {
				CustomMessage.printHeader(sender, mod.getName() + " Help");
				if (mod.perm(sender, "toggle.others"))
					sender.sendMessage(Clr.AQUA + "/cw [player] <on|off>" + Clr.DARKAQUA + " - Toggle a user's command watcher");
				return;
			}
			if (!mod.perm(sender, "toggle")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			CarbonWatcherModule.toggleWatching((Player) sender);
			sender.sendMessage(CarbonWatcherModule.isWatching((Player) sender)?CustomMessage.CW_WATCH_ENABLED.pre():CustomMessage.CW_WATCH_DISABLED.pre());
		}
	}
}
