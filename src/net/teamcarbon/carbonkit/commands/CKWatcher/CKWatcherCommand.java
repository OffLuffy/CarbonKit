package net.teamcarbon.carbonkit.commands.CKWatcher;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.modules.CKWatcherModule;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class CKWatcherCommand extends ModuleCmd {
	public CKWatcherCommand(Module module) { super(module, "carbonkitwatcher"); }
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!MiscUtils.perm(sender, "carbonkit.ckwatcher.toggle", "carbonkit.ckwatcher.toggle.others")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		if (args.length > 0) {
			if (MiscUtils.eq(args[0], "help", "h", "?")) {
				CustomMessage.printHeader(sender, "CKWatcher Help");
				if (sender instanceof Player && MiscUtils.perm(sender, "carbonkit.ckwatcher.toggle"))
					sender.sendMessage(Clr.AQUA + "/cw <on|off>" + Clr.DARKAQUA + " - Toggle your watching state, optionally specify on or off");
				if (MiscUtils.perm(sender, "carbonkit.ckwatcher.toggle.others"))
					sender.sendMessage(Clr.AQUA + "/cw [player] <on|off>" + Clr.DARKAQUA + " - Toggle another user's watching state");
			} else if (MiscUtils.isBoolean(args[0])) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
					return;
				}
				if (!MiscUtils.perm(sender, "carbonkit.ckwatcher.toggle")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				CKWatcherModule.setWatching((Player) sender, MiscUtils.toBoolean(args[0]));
				sender.sendMessage(CKWatcherModule.isWatching((Player) sender) ? CustomMessage.CW_WATCH_ENABLED.pre() : CustomMessage.CW_WATCH_DISABLED.pre());
			} else if (Bukkit.getPlayer(args[0]) != null) {
				if (!MiscUtils.perm(sender, "carbonkit.ckwatcher.toggle.others")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				Player target = Bukkit.getPlayer(args[0]);
				HashMap<String, String> rep = new HashMap<String, String>();
				rep.put("{PLAYER}", target.getName());
				if (args.length > 1 && MiscUtils.isBoolean(args[1]))
					CKWatcherModule.setWatching(target, MiscUtils.toBoolean(args[1]));
				else
					CKWatcherModule.toggleWatching(target);
				if (CKWatcherModule.isWatching(target)) {
					sender.sendMessage(MiscUtils.massReplace(CustomMessage.CW_WATCH_ENABLED_OTHER.pre(), rep));
					target.sendMessage(CustomMessage.CW_WATCH_ENABLED.pre());
				} else {
					sender.sendMessage(MiscUtils.massReplace(CustomMessage.CW_WATCH_DISABLED_OTHER.pre(), rep));
					target.sendMessage(CustomMessage.CW_WATCH_DISABLED.pre());
				}
			}
		} else {
			if (!(sender instanceof Player)) {
				CustomMessage.printHeader(sender, "CKWatcher Help");
				sender.sendMessage(Clr.RED + "/cw [player] <on|off> - Toggle another user's watching state, optionally specify on or off");
				return;
			}
			if (!MiscUtils.perm(sender, "carbonkit.ckwatcher.toggle")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			CKWatcherModule.toggleWatching((Player) sender);
			sender.sendMessage(CKWatcherModule.isWatching((Player)sender)?CustomMessage.CW_WATCH_ENABLED.pre():CustomMessage.CW_WATCH_DISABLED.pre());
		}
	}
}
