package net.teamcarbon.carbonkit.commands.CarbonTools;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.modules.CarbonToolsModule;
import net.teamcarbon.carbonkit.utils.EntHelper;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonkit.utils.Messages.Clr;
import net.teamcarbon.carbonkit.utils.NumUtils;
import net.teamcarbon.carbonkit.utils.TypeUtils;
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

	private CarbonToolsModule modInst = CarbonToolsModule.inst;

	public EntCountCommand(Module module) { super(module, "entitycount"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(mod.getCoreMsg("not-online", false));
			return;
		}
		Player p = (Player)sender;

			if (args.length > 0) {
				if (TypeUtils.isDouble(args[0])) {
					if (!modInst.perm(p, "entcount.radius")) {
						sender.sendMessage(mod.getCoreMsg("no-perm", false));
						return;
					}
					double r = Double.parseDouble(args[0]);
					double mr = CarbonKit.inst.getConfig().getDouble(mod.getName() + ".entity-count-max-radius", 500);
					if (r > mr || r <= 0) r = NumUtils.normalizeDouble(r, 0, mr);
					List<Entity> ents = p.getNearbyEntities(r,r,r);
					p.sendMessage(Clr.GRAY + "Entity count in radius: " + r);
					printResults(p, ents);
				} else if (Bukkit.getWorld(args[0]) != null) {
					if (!modInst.perm(p, "entcount.world")) {
						sender.sendMessage(mod.getCoreMsg("no-perm", false));
						return;
					}
					World w = Bukkit.getWorld(args[0]);
					p.sendMessage(Clr.GRAY + "Entity count in world: " + w.getName());
					printResults(p, w.getEntities());
				} else {
					p.sendMessage(Clr.RED + "Usage: /ecount [radius|world]");
				}
			} else {
				if (CarbonKit.pm.isPluginEnabled("WorldEdit")) {
					WorldEditPlugin we = (WorldEditPlugin) CarbonKit.pm.getPlugin("WorldEdit");
					Selection sel = we.getSelection(p);
					if (sel != null) {
						if (!modInst.perm(p, "entcount.selection")) {
							sender.sendMessage(mod.getCoreMsg("not-online", false));
							return;
						}
						List<Entity> selEnts = sel.getWorld().getEntities();
						List<Entity> ents = new ArrayList<>();
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
					}
				}
				if (!modInst.perm(p, "entcount.world")) {
					sender.sendMessage(mod.getCoreMsg("not-online", false));
					return;
				}
				p.sendMessage(Clr.GRAY + "Entity count in world: " + p.getWorld().getName());
				printResults(p, p.getWorld().getEntities());
			}
	}

	private void printResults(Player p, List<Entity> ents) {
		HashMap<EntityType, Integer> counts = new HashMap<>();
		HashMap<String, Integer> gCounts = new HashMap<>();
		for (Entity ent : ents) {
			counts.put(ent.getType(), (counts.containsKey(ent.getType())?counts.get(ent.getType())+1:1));
			if (CarbonKit.inst.getConfig().getBoolean(mod.getName() + ".entity-group-count", true)) {
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
			if (CarbonKit.inst.getConfig().getBoolean(mod.getName() + ".entity-type-count", false))
				p.sendMessage(pre + Clr.GOLD + type.toString() + ": " + Clr.AQUA + counts.get(type));
		}

		if (CarbonKit.inst.getConfig().getBoolean(mod.getName() + ".entity-group-count")) {
			for (String s : gCounts.keySet()) {
				String msg = Clr.AQUA + "";
				switch (s) {
					case "p": msg += "Passive"; break;
					case "n": msg += "Neutral"; break;
					case "h": msg += "Hostile"; break;
					case "pl": msg += "Players"; break;
					case "o": msg += "Other"; break;
				}
				msg += ": " + Clr.DARKAQUA + gCounts.get(s);
				p.sendMessage(msg);
			}
		}

		if (CarbonKit.inst.getConfig().getBoolean(mod.getName() + ".entity-total-count"))
			p.sendMessage(Clr.DARKAQUA + "" + ChatColor.BOLD + "TOTAL: " +Clr.DARKAQUA + ents.size());
	}
}
