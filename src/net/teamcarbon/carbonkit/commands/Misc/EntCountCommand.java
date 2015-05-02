package net.teamcarbon.carbonkit.commands.Misc;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.EntHelper;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class EntCountCommand extends ModuleCmd {

	public EntCountCommand(Module module) { super(module, "entitycount"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		Player p = (Player)sender;

		if (CarbonKit.pm.isPluginEnabled("WorldEdit")) {
			if (args.length > 0) {
				if (MiscUtils.isDouble(args[0])) {
					if (!MiscUtils.perm(p, "carbonkit.misc.entcount.radius")) {
						sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
						return;
					}
					double r = Double.parseDouble(args[0]);
					double mr = CarbonKit.getDefConfig().getDouble(mod.getName() + ".entity-count-max-radius", 500);
					if (r > mr || r <= 0) r = MiscUtils.normalizeDouble(r, 0, mr);
					List<Entity> ents = (p).getNearbyEntities(r,r,r);
					p.sendMessage(Clr.GRAY + "Entity count in radius: " + r);
					printResults(p, ents);
				} else if (Bukkit.getWorld(args[0]) != null) {
					if (!MiscUtils.perm(p, "carbonkit.misc.entcount.world")) {
						sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
						return;
					}
					World w = Bukkit.getWorld(args[0]);
					List<Entity> ents = w.getEntities();
					p.sendMessage(Clr.GRAY + "Entity count in world: " + w.getName());
					printResults(p, ents);
				} else {
					p.sendMessage(Clr.RED + "Usage: /ecount [radius|world]");
				}
			} else {
				WorldEditPlugin we = (WorldEditPlugin)CarbonKit.pm.getPlugin("WorldEdit");
				Selection sel = we.getSelection(p);
				if (sel != null) {
					if (!MiscUtils.perm(p, "carbonkit.misc.entcount.selection")) {
						sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
						return;
					}
					List<Entity> selEnts = sel.getWorld().getEntities();
					List<Entity> ents = new ArrayList<Entity>();
					for (Entity ent : selEnts) {
						int x = ent.getLocation().getBlockX(), y = ent.getLocation().getBlockY(), z = ent.getLocation().getBlockZ(),
								mx = sel.getMinimumPoint().getBlockX(), my = sel.getMinimumPoint().getBlockY(), mz = sel.getMinimumPoint().getBlockZ(),
								xx = sel.getMaximumPoint().getBlockX(), xy = sel.getMaximumPoint().getBlockY(), xz = sel.getMaximumPoint().getBlockZ();
						if (mx > xx || my > xy || mz > xz) {
							p.sendMessage(Clr.RED + "Vars backwards");
							return;
						}
						if (x > mx && x <= xx && y > my && y <= xy && z > mz && z <= xz)
							ents.add(ent);
					}
					p.sendMessage(Clr.GRAY + "Entity count in selection");
					printResults(p, ents);
				} else {
					if (!MiscUtils.perm(p, "carbonkit.misc.entcount.world")) {
						sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
						return;
					}
					List<Entity> ents = p.getWorld().getEntities();
					p.sendMessage(Clr.GRAY + "Entity count in world: " + p.getWorld().getName());
					printResults(p, ents);
				}
			}
		} else {
		}
	}

	private void printResults(Player p, List<Entity> ents) {
		HashMap<EntityType, Integer> counts = new HashMap<EntityType, Integer>();
		HashMap<String, Integer> gCounts = new HashMap<String, Integer>();
		for (Entity ent : ents) {
			counts.put(ent.getType(), (counts.containsKey(ent.getType())?counts.get(ent.getType())+1:1));
			if (CarbonKit.getDefConfig().getBoolean(mod.getName() + ".entity-group-count", true)) {
				if(EntHelper.isPassive(ent.getType()))
					gCounts.put("p", (gCounts.containsKey("p")?gCounts.get("p")+1:1));
				else if(EntHelper.isNeutral(ent.getType()))
					gCounts.put("n", (gCounts.containsKey("n")?gCounts.get("n")+1:1));
				else if(EntHelper.isHostile(ent.getType()))
					gCounts.put("h", (gCounts.containsKey("h")?gCounts.get("h")+1:1));
				else if(EntHelper.isPlayer(ent.getType()))
					gCounts.put("pl", (gCounts.containsKey("pl")?gCounts.get("pl")+1:1));
				else
					gCounts.put("o", (gCounts.containsKey("o")?gCounts.get("o")+1:1));
			}
		}

		for (EntityType type : counts.keySet()) {
			String pre = Clr.GRAY + "[?] ";
			if(EntHelper.isPassive(type)) pre = Clr.LIME + "[P] ";
			else if(EntHelper.isNeutral(type)) pre = Clr.DARKAQUA + "[N] ";
			else if(EntHelper.isHostile(type)) pre = Clr.RED + "[H] ";
			else if(EntHelper.isPlayer(type)) pre = Clr.GOLD + "[PL] ";
			if (CarbonKit.getDefConfig().getBoolean(mod.getName() + ".entity-type-count", false))
				p.sendMessage(pre + Clr.GOLD + type.toString() + ": " + Clr.AQUA + counts.get(type));
		}

		if (CarbonKit.getDefConfig().getBoolean(mod.getName() + ".entity-group-count")) {
			for (String s : gCounts.keySet()) {
				if (s.equals("p"))
					p.sendMessage(Clr.AQUA + "Passive: " + Clr.DARKAQUA + gCounts.get("p"));
				else if (s.equals("n"))
					p.sendMessage(Clr.AQUA + "Neutral: " +Clr.DARKAQUA + gCounts.get("n"));
				else if (s.equals("h"))
					p.sendMessage(Clr.AQUA + "Hostile: " +Clr.DARKAQUA + gCounts.get("h"));
				else if (s.equals("pl"))
					p.sendMessage(Clr.AQUA + "Players: " +Clr.DARKAQUA + gCounts.get("pl"));
				else if (s.equals("o"))
					p.sendMessage(Clr.AQUA + "Other: " +Clr.DARKAQUA + gCounts.get("o"));
			}
		}

		if (CarbonKit.getDefConfig().getBoolean(mod.getName() + ".entity-total-count"))
			p.sendMessage(Clr.DARKAQUA + "" + ChatColor.BOLD + "TOTAL: " +Clr.DARKAQUA + ents.size());
	}
}