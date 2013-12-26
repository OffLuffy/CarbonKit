package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleEssAssist;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class CommandToggleInteract extends ModuleCmd {
	private ModuleEssAssist eMod = ((ModuleEssAssist)Module.getModule("EssentialsAssist"));
	public CommandToggleInteract(Module module) {
		super(module, "toggleinteract");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Messages.send(sender, Message.NOT_ONLINE);
			return true;
		}
		if (!canExec(sender, false, "")) {
			Messages.send(sender, Message.NO_PERM);
			return true;
		}
		
		if (!eMod.isAntiInteractEnabled(sender.getName())) {
			if (CarbonKit.pm.isPluginEnabled("Essentials")) {
				Essentials ess = (Essentials)CarbonKit.pm.getPlugin("Essentials");
				User u = ess.getUserMap().getUser(sender.getName());
				if (u.isVanished()) {
					eMod.addAntiInteract(sender.getName());
					sender.sendMessage(Messages.Clr.HEAD + "Anti-interact has been enabled!");
					return true;
				} else {
					sender.sendMessage(Messages.Clr.ERR + "You must be vanished to use this!");
					return true;
				}
			} else {
				sender.sendMessage(Messages.Clr.ERR + "Essentials is not enabled on this server!");
				return true;
			}
		} else {
			eMod.removeAntiInteract(sender.getName());
			sender.sendMessage(Messages.Clr.HEAD + "Anti-interact has been disabled!");
			return true;
		}
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.toggleinteract"));
		return perms;
	}

}
