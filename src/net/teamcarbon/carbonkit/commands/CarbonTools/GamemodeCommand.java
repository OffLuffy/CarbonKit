package net.teamcarbon.carbonkit.commands.CarbonTools;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonlib.Misc.NumUtils;
import net.teamcarbon.carbonlib.Misc.TypeUtils;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class GamemodeCommand extends ModuleCmd {

	public GamemodeCommand(Module module) { super(module, "gamemode"); }

	public enum Mode {
		SURVIVAL(GameMode.SURVIVAL, "survive", "s"),
		CREATIVE(GameMode.CREATIVE, "c"),
		ADVENTURE(GameMode.ADVENTURE, "a"),
		SPECTATOR(GameMode.SPECTATOR, "spectate", "sp");

		GameMode mode;
		List<String> aliases = new ArrayList<String>();
		Mode(GameMode mode, String ... aliases) {
			this.mode = mode;
			Collections.addAll(this.aliases, aliases);
		}

		public static Mode getMode(String query) {
			if (TypeUtils.isInteger(query)) {
				return Mode.values()[NumUtils.normalizeInt(Integer.parseInt(query), 0, 3)];
			} else {
				for (Mode m : Mode.values()) {
					List<String> aliases = new ArrayList<String>();
					aliases.add(m.name().toLowerCase());
					aliases.addAll(m.getAliases());
					if (MiscUtils.eq(query, aliases)) { return m; }
				}
				return SURVIVAL;
			}
		}

		public List<String> getAliases() { return aliases; }
		public GameMode getGameMode() { return mode; }
		public String lname() { return name().toLowerCase(); }
	}

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0) {
			Mode m = Mode.getMode(args[0]);
			if (m == null) {
				sender.sendMessage(CustomMessage.MISC_INVALID_MODE.pre());
			} else {
				HashMap<String, String> rep = new HashMap<String, String>();
				rep.put("{MODE}", m.lname());
				if (args.length > 1) {
					if (MiscUtils.perm(sender, "carbonkit.gamemode.others." + m.lname())) {
						OfflinePlayer p = MiscUtils.getPlayer(args[1], false);
						if (p != null && p.isOnline()) {
							((Player) p).setGameMode(m.getGameMode());
							rep.put("{USER}", p.getName());
							if (!p.equals(sender)) {
								sender.sendMessage(MiscUtils.massReplace(CustomMessage.MISC_MODE_SET_OTHER.pre(), rep));
								((Player) p).sendMessage(MiscUtils.massReplace(CustomMessage.MISC_MODE_CHANGE.pre(), rep));
							} else {
								sender.sendMessage(MiscUtils.massReplace(CustomMessage.MISC_MODE_SET_SELF.pre(), rep));
							}
						} else {
							sender.sendMessage(CustomMessage.GEN_PLAYER_NOT_FOUND.noPre());
						}
					} else {
						sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					}
				} else {
					if (!(sender instanceof Player)) {
						sender.sendMessage(Clr.RED + "Usage: /gm [mode] [user]");
					} else {
						if (MiscUtils.perm(sender, "carbonkit.gamemode.self." + m.lname())) {
							((Player) sender).setGameMode(m.getGameMode());
							sender.sendMessage(MiscUtils.massReplace(CustomMessage.MISC_MODE_SET_SELF.pre(), rep));
						} else {
							sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
						}
					}
				}
			}
		} else {
			String pre = "carbonkit.gamemode.", o = "other.", s = "self.",
				m0 = "survival", m1 = "creative", m2 = "adventure", m3 = "spectator";
			if (MiscUtils.perm(sender, pre+s+m0, pre+s+m1, pre+s+m2, pre+s+m3)) {
				sender.sendMessage(Clr.AQUA + "/gm [mode]" + Clr.DARKAQUA + " - Set your own mode");
				sender.sendMessage(Clr.NOTE + "Allowed modes:"
						+ (MiscUtils.perm(sender, pre + s + m0) ? " survival" : "")
						+ (MiscUtils.perm(sender, pre + s + m1) ? " creative" : "")
						+ (MiscUtils.perm(sender, pre + s + m2) ? " adventure" : "")
						+ (MiscUtils.perm(sender, pre + s + m3) ? " spectator" : ""));
			}
			if (MiscUtils.perm(sender, pre+o+m0, pre+o+m1, pre+o+m2, pre+o+m3)) {
				sender.sendMessage(Clr.AQUA + "/gm [mode] [user]" + Clr.DARKAQUA + " - Set another user's mode");
				sender.sendMessage(Clr.NOTE + "Allowed modes:"
						+ (MiscUtils.perm(sender, pre+o+m0)?" survival":"")
						+ (MiscUtils.perm(sender, pre+o+m1)?" creative":"")
						+ (MiscUtils.perm(sender, pre+o+m2)?" adventure":"")
						+ (MiscUtils.perm(sender, pre+o+m3)?" spectator":""));
			}
		}
	}

}
