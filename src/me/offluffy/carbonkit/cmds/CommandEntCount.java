package me.offluffy.carbonkit.cmds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleMisc;
import me.offluffy.carbonkit.utils.EntityHelper;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;
import me.offluffy.carbonkit.utils.NumUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class CommandEntCount extends ModuleCmd {
	private ModuleMisc mMod = (ModuleMisc)Module.getModule("Misc");
	public CommandEntCount(Module module) {
		super(module, "entitycount");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Messages.send(sender, Message.NOT_ONLINE);
			return true;
		}
		
		Player p = (Player)sender;
		
		if (CarbonKit.pm.isPluginEnabled("WorldEdit")) {
			if (args.length > 0) {
				if (!CarbonKit.perms.has(p, "carbonkit.entcount.radius")) {
					Messages.send(sender, Message.NO_PERM);
					return true;
				}
				if (NumUtils.isDouble(args[0])) {
					double r = Double.parseDouble(args[0]);
					List<Entity> ents = (p).getNearbyEntities(r,r,r);
					p.sendMessage(Messages.Clr.PRE + "Entity count in radius: " + r);
					printResults(p, ents);
					return true;
				} else {
					p.sendMessage(Messages.Clr.ERR + "Usage: /ecount [radius]");
					return true;
				}
			} else {
				WorldEditPlugin we = (WorldEditPlugin)CarbonKit.pm.getPlugin("WorldEdit");
				Selection sel = we.getSelection(p);
				if (sel != null) {
					if (!CarbonKit.perms.has(p, "carbonkit.entcount.selection")) {
						Messages.send(sender, Message.NO_PERM);
						return true;
					}
					List<Entity> selEnts = sel.getWorld().getEntities();
					List<Entity> ents = new ArrayList<Entity>();
					for (Entity ent : selEnts) {
						int x = ent.getLocation().getBlockX(), y = ent.getLocation().getBlockY(), z = ent.getLocation().getBlockZ(),
								mx = sel.getMinimumPoint().getBlockX(), my = sel.getMinimumPoint().getBlockY(), mz = sel.getMinimumPoint().getBlockZ(),
								xx = sel.getMaximumPoint().getBlockX(), xy = sel.getMaximumPoint().getBlockY(), xz = sel.getMaximumPoint().getBlockZ();
						if (mx > xx || my > xy || mz > xz) {
							p.sendMessage(Messages.Clr.ERR + "Vars backwards");
							return true;
						}
						if (x > mx && x <= xx && y > my && y <= xy && z > mz && z <= xz)
							ents.add(ent);
					}
					p.sendMessage(Messages.Clr.PRE + "Entity count in selection");
					printResults(p, ents);
					return true;
				} else {
					if (!CarbonKit.perms.has(p, "carbonkit.entcount.world")) {
						Messages.send(sender, Message.NO_PERM);
						return true;
					}
					List<Entity> ents = p.getWorld().getEntities();
					p.sendMessage(Messages.Clr.PRE + "Entity count in world: " + p.getWorld().getName());
					printResults(p, ents);
					return true;
				}
			}
		} else {
			return true;
		}
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.permission"));
		return perms;
	}
	
	private void printResults(Player p, List<Entity> ents) {
		HashMap<EntityType, Integer> counts = new HashMap<EntityType, Integer>();
		HashMap<String, Integer> gCounts = new HashMap<String, Integer>();
		for (Entity ent : ents) {
			counts.put(ent.getType(), (counts.containsKey(ent.getType())?counts.get(ent.getType())+1:1));
			if (CarbonKit.config.getBoolean(mMod.getName() + ".entity-group-count")) {
				if(EntityHelper.isPassive(ent.getType()))
					gCounts.put("p", (gCounts.containsKey("p")?gCounts.get("p")+1:1));
				else if(EntityHelper.isNeutral(ent.getType()))
					gCounts.put("n", (gCounts.containsKey("n")?gCounts.get("n")+1:1));
				else if(EntityHelper.isHostile(ent.getType()))
					gCounts.put("h", (gCounts.containsKey("h")?gCounts.get("h")+1:1));
				else if(EntityHelper.isPlayer(ent.getType()))
					gCounts.put("pl", (gCounts.containsKey("pl")?gCounts.get("pl")+1:1));
				else
					gCounts.put("o", (gCounts.containsKey("o")?gCounts.get("o")+1:1));
			}
		}
		
		for (EntityType type : counts.keySet()) {
			String pre = "";
			if(EntityHelper.isPassive(type))
				pre = ChatColor.GREEN + "[P] ";
			else if(EntityHelper.isNeutral(type)) 
				pre = ChatColor.DARK_AQUA + "[N] ";
			else if(EntityHelper.isHostile(type))
				pre = ChatColor.RED + "[H] ";
			else if(EntityHelper.isPlayer(type))
				pre = ChatColor.GOLD + "[PL] ";
			
			if (CarbonKit.config.getBoolean(mMod.getName() + ".entity-type-count"))
				p.sendMessage(pre + Messages.Clr.HEAD + type.toString() + ": " + Messages.Clr.NORM + counts.get(type));
		}
		
		if (CarbonKit.config.getBoolean(mMod.getName() + ".entity-group-count")) {
			for (String s : gCounts.keySet()) {
				if (s.equals("p"))
					p.sendMessage(Messages.Clr.HEAD + "Passive: " + Messages.Clr.NORM + gCounts.get("p"));
				else if (s.equals("n"))
					p.sendMessage(Messages.Clr.HEAD + "Neutral: " + Messages.Clr.NORM + gCounts.get("n"));
				else if (s.equals("h"))
					p.sendMessage(Messages.Clr.HEAD + "Hostile: " + Messages.Clr.NORM + gCounts.get("h"));
				else if (s.equals("pl"))
					p.sendMessage(Messages.Clr.HEAD + "Players: " + Messages.Clr.NORM + gCounts.get("pl"));
				else if (s.equals("o"))
					p.sendMessage(Messages.Clr.HEAD + "Other: " + Messages.Clr.NORM + gCounts.get("o"));
			}
		}
		
		if (CarbonKit.config.getBoolean(mMod.getName() + ".entity-total-count"))
			p.sendMessage(Messages.Clr.NORM + "" + ChatColor.BOLD + "TOTAL: " + Messages.Clr.NORM + ents.size());
		return;
	}

}
