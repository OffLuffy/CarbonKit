package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandReload extends ModuleCmd {

	public CommandReload(Module module) {
		super(module, "carbonkitreload");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!canExec(sender, false, "")) {
			Messages.send(sender, Message.NO_PERM);
			return true;
		}
		CarbonKit.config = Lib.reloadFile("config.yml");
		// XXX Reload all files?
		// TODO Re-load caches that load from config
		return true;
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.reload"));
		return perms;
	}

}
