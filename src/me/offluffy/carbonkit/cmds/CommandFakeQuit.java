package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleMisc;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;
import me.offluffy.carbonkit.utils.NumUtils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class CommandFakeQuit extends ModuleCmd {
	private ModuleMisc mMod = (ModuleMisc)Module.getModule("Misc");
	public CommandFakeQuit(Module module) {
		super(module, "fakequit");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Location loc = CarbonKit.inst.getServer().getWorlds().get(0).getSpawnLocation();
		String user = sender.getName();
		String qm = CarbonKit.config.getString(mMod.getName() + ".quit-message", "&6&l[&r&c-&r&6&l]&r &e{PLAYER}&r&6 logged out!");
		String qme = CarbonKit.config.getString(mMod.getName() + ".quit-message-extended", "&6&l[&r&c-&r&6&l]&r &e{PLAYER}&r&6 logged out!");
		
		if (args.length > 0) {
			if (!CarbonKit.perms.has(sender, "carbonkit.fakequit.others")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			Player pl = CarbonKit.inst.getServer().getPlayer(args[0]);
			if (pl != null) {
				user = pl.getName();
				loc = pl.getLocation();
			} else
				user = args[0];
			if (!mMod.addressMap.containsKey(user))
				mMod.addressMap.put(user, NumUtils.rand(16,255) + "." + NumUtils.rand(16,255) + "." + NumUtils.rand(16,255) + "." + NumUtils.rand(16,255));
			qm = mMod.translateVars(qm, user, loc);
			qme = mMod.translateVars(qme, user, loc);
		} else {
			if (!CarbonKit.perms.has(sender, "carbonkit.fakequit.self")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			
			if ((sender instanceof Player)) {
				user = sender.getName();
				loc = ((Player)sender).getLocation();
			} else
				user = "Herobrine";
			if (!mMod.addressMap.containsKey(user))
				mMod.addressMap.put(user, NumUtils.rand(16,255) + "." + NumUtils.rand(16,255) + "." + NumUtils.rand(16,255) + "." + NumUtils.rand(16,255));
			qm = mMod.translateVars(qm, user, loc);
			qme = mMod.translateVars(qme, user, loc);
		}
		Player p = (Player)sender;
		String statuses = "";
		
		// Custom join message
		for (Player opl : CarbonKit.inst.getServer().getOnlinePlayers()) {
			if(CarbonKit.perms.has(opl, "carbonkit.quitmsg.extended")) {
				if (CarbonKit.pm.isPluginEnabled("Essentials")) {
					Essentials ess = (Essentials)CarbonKit.pm.getPlugin("Essentials");
					User u = ess.getUserMap().getUser(p.getName());
					if (u.isJailed())
						statuses += "[J]";
					if (u.isMuted())
						statuses += "[M]";
				}
				opl.sendMessage(Messages.Clr.NOTE + statuses + ChatColor.RESET + qme);
			} else
				opl.sendMessage(qm);
		}
		
		mMod.addressMap.remove(user);
	
		return true;
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.permission"));
		return perms;
	}

}
