package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleGoldenSmite;
import me.offluffy.carbonkit.modules.ModuleGoldenSmite.EntType;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;
import me.offluffy.carbonkit.utils.Smite;
import me.offluffy.carbonkit.utils.Smite.SmiteType;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGoldenSmite extends ModuleCmd {
	private Player pl;
	private Location l;
	private List<String> enabled;
	private ModuleGoldenSmite sMod = (ModuleGoldenSmite)Module.getModule("GoldenSmite");
	public CommandGoldenSmite(Module module) {
		super(module, "GoldenSmite");
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This can only be used in game!");
			return true;
		}
		enabled = sMod.kill.getStringList(sender.getName());
		if (args.length == 0) {
			if (!Lib.perm(sender, "gsmite.use.cmd")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			int range = CarbonKit.config.getInt(sMod.getName() + ".range");
			pl = (Player)sender;
			try {
				l = pl.getTargetBlock(null, range).getLocation();
				if (pl.getTargetBlock(null, range).getType() == Material.AIR) {
					sender.sendMessage(ChatColor.RED + "Too far away!");
					return true;
				}
			} catch (Exception e) {
				return true;
			}
			Smite.createSmite(pl, l, SmiteType.CMD);
			return true;
		} else {
			if (Lib.eq(args[0],"arrow","a","bow")) {
				if (!CarbonKit.perms.has(sender, "gsmite.use.arrow")) {
					Messages.send(sender, Message.NO_PERM);
					return true;
				}
				pl = (Player)sender;
				if (sMod.enabledArrowList.contains((pl.getName()))) {
					sMod.enabledArrowList.remove(pl.getName());
					if (CarbonKit.data.getStringList(sMod.getName() + ".enabled-arrow").contains(pl.getName())) {
						List<String> gsa = CarbonKit.data.getStringList(sMod.getName() + ".enabled-arrow");
						gsa.remove(pl.getName());
						CarbonKit.data.set(sMod.getName() + ".enabled-arrow", gsa);
					}
					sender.sendMessage(Messages.Clr.NORM + "You've disabled GSmite arrows");
				} else {
					sMod.enabledArrowList.add(pl.getName());
					if (!CarbonKit.data.getStringList(sMod.getName() + ".enabled-arrow").contains(pl.getName())) {
						List<String> gsa = CarbonKit.data.getStringList(sMod.getName() + ".enabled-arrow");
						gsa.add(pl.getName());
						CarbonKit.data.set(sMod.getName() + ".enabled-arrow", gsa);
					}
					sender.sendMessage(Messages.Clr.HEAD + "You've enabled GSmite arrows");
				}
				Lib.saveFile(CarbonKit.data, "data.yml");
				return true;
			} else if (Lib.eq(args[0],"here")) {
				if (!CarbonKit.perms.has(sender, "gsmite.use.cmd")) {
					Messages.send(sender, Message.NO_PERM);
					return true;
				}
				pl = (Player)sender;
				l = pl.getLocation();
				Smite.createSmite(pl, l, SmiteType.CMD);
				return true;
			} else if (Lib.eq(args[0],"list","l","check","c","status","s")) {
				if (!canExec(sender, false, "check")) {
					Messages.send(sender, Message.NO_PERM);
					return true;
				}
				sender.sendMessage(Messages.Clr.HEAD + "GSmite Toggles");
				for (EntType e : EntType.values()) {
					boolean d = sMod.kill.getStringList(sender.getName()).contains(e.getName());
					sender.sendMessage(Messages.Clr.NORM + e.getName() + ": " + (d?"\u00A7aEnabled":"\u00A7bDisabled"));
				}
				return true;
			} else if (Lib.eq(args[0], "help")) {
				if (!canExec(sender, false, "help")) {
					Messages.send(sender, Message.NO_PERM);
					return true;
				}
				if (canExec(sender, false, ""))
					sender.sendMessage(Messages.Clr.HEAD + "/gs" + Messages.Clr.NORM + " - GSmite command");
				if (canExec(sender, false, "check"))
					sender.sendMessage(Messages.Clr.HEAD + "/gs list" + Messages.Clr.NORM + " - List what GSmite destroys");
				for (EntType e : EntType.values())
					if (canExec(sender, false, e.getName()))
						sender.sendMessage(Messages.Clr.HEAD + "/gs " + e.getName().toLowerCase() + Messages.Clr.NORM + " - Toggle destroy for " + e.getName().toLowerCase());
				return true;
			} else {
				for (EntType e : EntType.values()) {
					if (Lib.eq(args[0], e.getAliases())) {
						toggleType(e, sender);
						return true;
					}
				}
				if (!canExec(sender, false, "")) {
					sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
					return true;
				}
				Player targetPlayer = pl.getServer().getPlayer(args[0]);
				if (targetPlayer == null) {
					sender.sendMessage(ChatColor.RED + "Could not find " + args[0]);
					return true;
				} else {
					Smite.smitePlayer(sender, targetPlayer);
					return true;
				}
			}
		}
	}
	
	private void toggleType(EntType type, CommandSender sender) {
		if (!canExec(sender, false, type.getName())) {
			Messages.send(sender, Message.NO_PERM);
			return;
		}
		boolean destroy = enabled.contains(type.getName());
		if (destroy)
			enabled.remove(type.getName());
		else
			enabled.add(type.getName());
		sMod.kill.set(sender.getName(), enabled);
		CarbonKit.data.set(sMod.getName() + ".enabled-kills", sMod.kill);
		Lib.saveFile(CarbonKit.data, "data.yml");
		sender.sendMessage(ChatColor.AQUA + "[GSmite] Destroy " + type.getName().toLowerCase() + " now " + ((!destroy)?"enabled":"disabled"));
		return;
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("gsmite.use.cmd"));
		perms.put("check", toList("gsmite.toggle.check"));
		perms.put("help", toList("gsmite.help"));
		for (EntType e : EntType.values())
			perms.put(e.getName(), toList("gsmite.toggle." + e.getName().toLowerCase()));
		return perms;
	}

}
