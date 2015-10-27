package net.teamcarbon.carbonkit.commands.CarbonPerks;

import net.teamcarbon.carbonkit.modules.CarbonPerksModule;
import net.teamcarbon.carbonkit.modules.CarbonPerksModule.TrailEffect;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonlib.Misc.NumUtils;
import net.teamcarbon.carbonlib.Misc.TypeUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrailCommand extends ModuleCmd {

	public TrailCommand(Module module) { super(module, "trail"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		boolean multiple = MiscUtils.perm(sender, "carbonkit.perks.trails.multiple");
		int maxTrails = getMod().getConfig().getInt("max-trails", 3);
		if (!multiple) maxTrails = 1;
		if (args.length > 0) {
			if (MiscUtils.eq(args[0], "set", "s")) {
				if (!MiscUtils.perm(sender, "carbonkit.perks.trails.set")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (args.length > 1) {
					List<TrailEffect> fx = new ArrayList<TrailEffect>();
					for (int i = 1; i < args.length && fx.size() < maxTrails; i++) {
						if (CarbonPerksModule.getEffect(args[i]) != null) {
							TrailEffect e = CarbonPerksModule.getEffect(args[i]);
							if (MiscUtils.perm(sender, "carbonkit.perks.trails.set." + e.name().toLowerCase())) {
								fx.add(e);
							} else {
								HashMap<String, String> rep = new HashMap<String, String>();
								rep.put("{EFFECT}", e.name().toLowerCase().replace("_", ""));
								sender.sendMessage(MiscUtils.massReplace(CustomMessage.CP_NO_EFFECT_PERM.pre(), rep));
							}
						} else {
							HashMap<String, String> rep = new HashMap<String, String>();
							rep.put("{QUERY}", args[i]);
							sender.sendMessage(MiscUtils.massReplace(CustomMessage.CP_INVALID_TRAIL.pre(), rep));
						}
					}
					if (fx.size() < 1) return;
					CarbonPerksModule.setTrailEffects((Player) sender, fx);
					HashMap<String, String> rep = new HashMap<String, String>();
					rep.put("{EFFECTCOUNT}", fx.size()+"");
					String[] fxNames = new String[fx.size()];
					for (int i = 0; i < fx.size(); i++) fxNames[i] = fx.get(i).name().toLowerCase().replace("_","");
					rep.put("{EFFECTLIST}", MiscUtils.stringFromArray(", ", fxNames));
					sender.sendMessage(MiscUtils.massReplace(CustomMessage.CP_SET_TRAIL.pre(), rep));
				} else {
					if (multiple) { sender.sendMessage(Clr.AQUA + "/trail set [type] [type] ..."); }
					else { sender.sendMessage(Clr.AQUA + "/trail set [type]"); }
					sender.sendMessage(Clr.AQUA + "/trail list" + Clr.DARKAQUA + " to view types");
				}
			} else if (MiscUtils.eq(args[0], "add", "a")) {
				if (!MiscUtils.perm(sender, "carbonkit.perks.trails.add")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (args.length > 1) {
					if (CarbonPerksModule.getTrailEffects((Player) sender).size() >= maxTrails) {
						sender.sendMessage(CustomMessage.CP_MAX_TRAILS.pre());
						return;
					}
					List<TrailEffect> fx = new ArrayList<TrailEffect>();
					for (int i = 1; i < args.length && fx.size() <= maxTrails; i++) {
						if (CarbonPerksModule.getEffect(args[i]) != null) {
							TrailEffect e = CarbonPerksModule.getEffect(args[i]);
							if (MiscUtils.perm(sender, "carbonkit.perks.trails.set." + e.name().toLowerCase())) {
								fx.add(CarbonPerksModule.getEffect(args[i]));
							} else {
								HashMap<String, String> rep = new HashMap<String, String>();
								rep.put("{EFFECT}", e.name().toLowerCase().replace("_", ""));
								sender.sendMessage(MiscUtils.massReplace(CustomMessage.CP_NO_EFFECT_PERM.pre(), rep));
							}
						} else {
							HashMap<String, String> rep = new HashMap<String, String>();
							rep.put("{QUERY}", args[i]);
							sender.sendMessage(MiscUtils.massReplace(CustomMessage.CP_INVALID_TRAIL.pre(), rep));
						}
					}
					if (fx.size() < 1) return;
					for (TrailEffect e : fx)
						CarbonPerksModule.addTrailEffect((Player) sender, e);
					HashMap<String, String> rep = new HashMap<String, String>();
					rep.put("{EFFECTCOUNT}", fx.size()+"");
					String[] fxNames = new String[fx.size()];
					for (int i = 0; i < fx.size(); i++) fxNames[i] = fx.get(i).name().toLowerCase().replace("_","");
					rep.put("{EFFECTLIST}", MiscUtils.stringFromArray(", ", fxNames));
					sender.sendMessage(MiscUtils.massReplace(CustomMessage.CP_ADD_TRAIL.pre(), rep));
				} else {
					if (multiple) { sender.sendMessage(Clr.AQUA + "/trail add [type] [type] ..."); }
					else { sender.sendMessage(Clr.AQUA + "/trail add [type]"); }
					sender.sendMessage(Clr.AQUA + "/trail list" + Clr.DARKAQUA + " to view types");
				}
			} else if (MiscUtils.eq(args[0], "remove", "rem")) {
				if (!MiscUtils.perm(sender, "carbonkit.perks.trails.remove")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (args.length > 1) {
					if (CarbonPerksModule.getEffect(args[1]) != null) {
						TrailEffect e = CarbonPerksModule.getEffect(args[1]);
						if (CarbonPerksModule.getTrailEffects((Player)sender).contains(e)) {
							CarbonPerksModule.removeTrailEffect((Player)sender, e);
							HashMap<String, String> rep = new HashMap<String, String>();
							rep.put("{EFFECT}", e.name().toLowerCase().replace("_",""));
							sender.sendMessage(MiscUtils.massReplace(CustomMessage.CP_REM_TRAIL.pre(), rep));
						} else {
							sender.sendMessage(CustomMessage.CP_REM_NOT_FOUND.pre());
						}
					} else {
						HashMap<String, String> rep = new HashMap<String, String>();
						rep.put("{QUERY}", args[1]);
						sender.sendMessage(MiscUtils.massReplace(CustomMessage.CP_INVALID_TRAIL.pre(), rep));
					}
				} else {
					sender.sendMessage(Clr.AQUA + "/trail rem [type]");
				}
			} else if (MiscUtils.eq(args[0], "random", "rand", "r")) {
				if (!MiscUtils.perm(sender, "carbonkit.perks.trails.random")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				int num = maxTrails;
				if (multiple && args.length > 1) {
					if (TypeUtils.isInteger(args[1])) {
						num = NumUtils.normalizeInt(Integer.parseInt(args[1]), 1, maxTrails);
					}
				} else { num = multiple ? NumUtils.rand(1, maxTrails) : 1; }
				List<TrailEffect> fx = new ArrayList<TrailEffect>(), randList = new ArrayList<TrailEffect>();
				for (TrailEffect e : TrailEffect.values())
					if (MiscUtils.perm(sender, "carbonkit.perks.trails.set." + e.lname().replace("_", "")))
						randList.add(e);
				for (int i = 0; i < num; i++) { fx.add(CarbonPerksModule.getRandomEffect(randList)); }
				if (fx.size() < 1) return;
				CarbonPerksModule.setTrailEffects((Player) sender, fx);
				HashMap<String, String> rep = new HashMap<String, String>();
				rep.put("{EFFECTCOUNT}", fx.size()+"");
				String[] fxNames = new String[fx.size()];
				for (int i = 0; i < fx.size(); i++) fxNames[i] = fx.get(i).name().toLowerCase().replace("_","");
				rep.put("{EFFECTLIST}", MiscUtils.stringFromArray(", ", fxNames));
				sender.sendMessage(MiscUtils.massReplace(CustomMessage.CP_SET_RANDOM.pre(), rep));
			} else if (MiscUtils.eq(args[0], "toggle", "t") || TypeUtils.isBoolean(args[0])) {
				if (!MiscUtils.perm(sender, "carbonkit.perks.trails.toggle")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (!MiscUtils.eq(args[0], "t") && TypeUtils.isBoolean(args[0])) {
					CarbonPerksModule.setTrailEnabled((Player) sender, TypeUtils.toBoolean(args[0]));
				} else if (args.length > 1 && TypeUtils.isBoolean(args[1])) {
					CarbonPerksModule.setTrailEnabled((Player) sender, TypeUtils.toBoolean(args[1]));
				} else { CarbonPerksModule.toggleTrailEnabled((Player) sender); }
				HashMap<String, String> rep = new HashMap<String, String>();
				rep.put("{TRAILSTATE}", CarbonPerksModule.isTrailEnabled((Player) sender) ? "enabled" : "disabled");
				sender.sendMessage(MiscUtils.massReplace(CustomMessage.CP_TOGGLED.pre(), rep));
			} else if (MiscUtils.eq(args[0], "clear", "reset", "c")) {
				if (!MiscUtils.perm(sender, "carbonkit.perks.trails.clear")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				CarbonPerksModule.clearCachedData((Player) sender);
				sender.sendMessage(CustomMessage.CP_CLEARED.pre());
			} else if (MiscUtils.eq(args[0], "list", "l")) {
				int typesPerPage = 12, curPage = 1, pages = (int)Math.ceil(CarbonPerksModule.getEffectNames().size()/10);
				if (args.length > 1 && TypeUtils.isInteger(args[1])) { curPage = NumUtils.normalizeInt(Integer.parseInt(args[1]), 1, pages); }
				CustomMessage.printHeader(sender, "Particle Trail Types [pg. "+curPage+"/"+pages+"]:");
				int disp = 0;
				for (TrailEffect te : TrailEffect.values()) {
					if (MiscUtils.perm(sender, "carbonkit.perks.trails.set." + te.lname().replace("_", ""))) {
						if (disp >= ((typesPerPage * curPage) - typesPerPage)) {
							String aliases = "";
							if (te.getAliases().size() > 0)
								aliases += te.getAliases().get(0);
							for (int i = 1; i < te.getAliases().size(); i++)
								aliases += ", " + te.getAliases().get(i);
							sender.sendMessage(Clr.AQUA + te.name().replace("_", "") + (!aliases.isEmpty() ? (" " + Clr.NOTE + "(" + aliases + ")") : ""));
						}
						if (++disp >= (typesPerPage * curPage)) break;
					}
				}
			}
		} else {
			if (!MiscUtils.perm(sender, "carbonkit.perks.trails.help")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			CustomMessage.printHeader(sender, "Particle Trails");
			if (MiscUtils.perm(sender, "carbonkit.perks.trails.set")) {
				if (multiple) {
					sender.sendMessage(Clr.AQUA + "/trail set <type> [type] ..." + Clr.DARKAQUA + " - Sets your particle trails");
				} else {
					sender.sendMessage(Clr.AQUA + "/trail set <type>" + Clr.DARKAQUA + " - Sets your particle trail");
				}
			}
			if (MiscUtils.perm(sender, "carbonkit.perks.trails.random")) {
				if (multiple) {
					sender.sendMessage(Clr.AQUA + "/trail random [#]" + Clr.DARKAQUA + " - Sets random particle trails");
				} else {
					sender.sendMessage(Clr.AQUA + "/trail random" + Clr.DARKAQUA + " - Sets a random particle trail");
				}
			}
			if (MiscUtils.perm(sender, "carbonkit.perks.trails.add")) {
				if (multiple) {
					sender.sendMessage(Clr.AQUA + "/trail add <type> [type] ..." + Clr.DARKAQUA + " - Adds particle trails");
				} else {
					sender.sendMessage(Clr.AQUA + "/trail add <type>" + Clr.DARKAQUA + " - Adds a particle trail");
				}
			}
			MiscUtils.permSend(sender, "carbonkit.perks.trails.remove", Clr.AQUA + "/trail rem <type>" + Clr.DARKAQUA + " - Removes a particle trail");
			MiscUtils.permSend(sender, "carbonkit.perks.trails.toggle", Clr.AQUA + "/trail toggle" + Clr.DARKAQUA + " - Toggles particle trails on/off");
			MiscUtils.permSend(sender, "carbonkit.perks.trails.clear", Clr.AQUA + "/trail clear" + Clr.DARKAQUA + " - Removes your particle trail(s)");
			MiscUtils.permSend(sender, "carbonkit.perks.trails.list", Clr.AQUA + "/trail list [page]" + Clr.DARKAQUA + " - Lists effect types");
		}
	}
}
