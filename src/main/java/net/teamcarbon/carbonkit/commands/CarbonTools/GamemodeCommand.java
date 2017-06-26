package net.teamcarbon.carbonkit.commands.CarbonTools;

import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonkit.utils.Messages.Clr;
import net.teamcarbon.carbonkit.utils.MiscUtils;
import net.teamcarbon.carbonkit.utils.NumUtils;
import net.teamcarbon.carbonkit.utils.TypeUtils;
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
		List<String> aliases = new ArrayList<>();
		Mode(GameMode mode, String ... aliases) {
			this.mode = mode;
			Collections.addAll(this.aliases, aliases);
		}

		public static Mode getMode(String query) {
			if (TypeUtils.isInteger(query)) {
				return Mode.values()[NumUtils.normalizeInt(Integer.parseInt(query), 0, 3)];
			} else {
				for (Mode m : Mode.values()) {
					List<String> aliases = new ArrayList<>();
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
				sender.sendMessage(mod.getMsg("invalid-gamemode", true));
			} else {
				HashMap<String, String> rep = new HashMap<>();
				rep.put("{MODE}", m.lname());
				if (args.length > 1) {
					if (mod.perm(sender, "gamemode.others." + m.lname())) {
						OfflinePlayer p = MiscUtils.getPlayer(args[1], false);
						if (p != null && p.isOnline()) {
							((Player) p).setGameMode(m.getGameMode());
							rep.put("{USER}", p.getName());
							if (!p.equals(sender)) {
								sender.sendMessage(mod.getMsg("mode-set-other", true, rep));
								((Player) p).sendMessage(mod.getMsg("mode-change", true, rep));
							} else {
								sender.sendMessage(mod.getMsg("mode-set-self", true, rep));
							}
						} else {
							sender.sendMessage(mod.getCoreMsg("player-not-found", false));
						}
					} else {
						sender.sendMessage(mod.getCoreMsg("no-perm", false));
					}
				} else {
					if (!(sender instanceof Player)) {
						sender.sendMessage(Clr.RED + "Usage: /gm [mode] [user]");
					} else {
						if (mod.perm(sender, "gamemode.self." + m.lname())) {
							((Player) sender).setGameMode(m.getGameMode());
							sender.sendMessage(mod.getMsg("mode-set-self", true, rep));
						} else {
							sender.sendMessage(mod.getCoreMsg("no-perm", false));
						}
					}
				}
			}
		} else {
			String[] sPerms = new String[GameMode.values().length], oPerms = new String[GameMode.values().length];
			for (int i = 0; i < GameMode.values().length; i++) {
				sPerms[i] = "gamemode.self." + GameMode.values()[i].name().toLowerCase();
				oPerms[i] = "gamemode.other." + GameMode.values()[i].name().toLowerCase();
			}
			if (mod.perm(sender, sPerms)) {
				sender.sendMessage(Clr.AQUA + "/gm [mode]" + Clr.DARKAQUA + " - Set your own mode");
				StringBuilder sb = new StringBuilder(Clr.NOTE + "Allowed modes:");
				//String modes = Clr.NOTE + "Allowed modes:";
				for (GameMode gm : GameMode.values()) {
					String gml = gm.name().toLowerCase();
					sb.append((mod.perm(sender, "gamemode.self." + gml) ? " " + gml : ""));
					//modes += (mod.perm(sender, "gamemode.self." + gml) ? " " + gml : "");
				}
				sender.sendMessage(sb.toString());
			}
			if (mod.perm(sender, oPerms)) {
				sender.sendMessage(Clr.AQUA + "/gm [mode] [user]" + Clr.DARKAQUA + " - Set another user's mode");
				StringBuilder sb = new StringBuilder(Clr.NOTE + "Allowed modes:");
				//String modes = Clr.NOTE + "Allowed modes:";
				for (GameMode gm : GameMode.values()) {
					String gml = gm.name().toLowerCase();
					sb.append((mod.perm(sender, "gamemode.other." + gml) ? " " + gml : ""));
					//modes += (mod.perm(sender, "gamemode.other." + gml) ? " " + gml : "");
				}
				sender.sendMessage(sb.toString());
			}
		}
	}
}
