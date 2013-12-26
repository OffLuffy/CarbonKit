package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleCKWatcher;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCKWatcher extends ModuleCmd {
	ModuleCKWatcher cMod = (ModuleCKWatcher)mod;
	public CommandCKWatcher(Module module) {
		super(module, "ckwatcher");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> cw = ((ModuleCKWatcher)Module.getModule("CommandWatcher")).watchers;
		if (!canExec(sender, false, "")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return true;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You can only use this in-game!");
			return true;
		}
		Boolean sp = cw.contains(sender.getName());
		if (sp)
			cw.remove(sender.getName());
		else
			cw.add(sender.getName());
		sender.sendMessage(((!sp)?ChatColor.AQUA:ChatColor.LIGHT_PURPLE) + "CKWatcher " + ((!sp)?"enabled":"disabled"));
		CarbonKit.config.set(cMod.getName() + ".watchers", cw);
		Lib.saveFile(CarbonKit.config, "config.yml");
		return true;
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.watcher"));
		return perms;
	}

}
