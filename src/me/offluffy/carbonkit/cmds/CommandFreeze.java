package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleMisc;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandFreeze extends ModuleCmd {
	ModuleMisc mMod = (ModuleMisc)mod;
	public CommandFreeze(Module module) {
		super(module, "freeze");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		ModuleMisc mod = ((ModuleMisc)Module.getModule("Misc"));
		if (!canExec(sender, false, "")) {
			Messages.send(sender, Message.NO_PERM);
			return true;
		} else {
			if (args.length != 1) {
				sender.sendMessage(Messages.Clr.ERR + "/freeze [player]");
				return true;
			} else {
				Player pl = CarbonKit.inst.getServer().getPlayer(args[0]);
				if (pl == null) {
					sender.sendMessage(Messages.Clr.ERR + "That player is not online");
					return true;
				} else {
					if (!CarbonKit.perms.has(pl,"carbonkit.freeze.immune")) {
						if (mod.freezeList.contains(pl.getName())) {
							mod.freezeList.remove(pl.getName());
							sender.sendMessage(Messages.Clr.NORM + "Player " + Messages.Clr.HEAD + pl.getName() + Messages.Clr.NORM + " has been released.");
							return true;
						} else {
							mod.freezeList.add(pl.getName());
							sender.sendMessage(Messages.Clr.NORM + "Player " + Messages.Clr.HEAD + pl.getName() + Messages.Clr.NORM + " has been frozen.");
							if (!CarbonKit.config.getString(mMod.getName() + ".freeze-message").equals(""))
								pl.sendMessage(ChatColor.translateAlternateColorCodes('&', CarbonKit.config.getString(mMod.getName() + ".freeze-message", ChatColor.AQUA + "Freeze!")));
							return true;
						}
					} else {
						sender.sendMessage(Messages.Clr.ERR + "This player cannot be frozen");
						return true;
					}
				}
			}
		}
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.freeze"));
		return perms;
	}

}
