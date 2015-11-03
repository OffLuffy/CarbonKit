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
					if (mod.perm(sender, "gamemode.others." + m.lname())) {
						OfflinePlayer p = MiscUtils.getPlayer(args[1], false);
						if (p != null && p.isOnline()) {
							((Player) p).setGameMode(m.getGameMode());
							rep.put("{USER}", p.getName());
							if (!p.equals(sender)) {
								sender.sendMessage(CustomMessage.MISC_MODE_SET_OTHER.pre(rep));
								((Player) p).sendMessage(CustomMessage.MISC_MODE_CHANGE.pre(rep));
							} else {
								sender.sendMessage(CustomMessage.MISC_MODE_SET_SELF.pre(rep));
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
						if (mod.perm(sender, "gamemode.self." + m.lname())) {
							((Player) sender).setGameMode(m.getGameMode());
							sender.sendMessage(CustomMessage.MISC_MODE_SET_SELF.pre(rep));
						} else {
							sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
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
				String modes = Clr.NOTE + "Allowed modes:";
				for (GameMode gm : GameMode.values()) {
					String gml = gm.name().toLowerCase();
					modes += (mod.perm(sender, "gamemode.self." + gml) ? " " + gml : "");
				}
				sender.sendMessage(modes);
			}
			if (mod.perm(sender, oPerms)) {
				sender.sendMessage(Clr.AQUA + "/gm [mode] [user]" + Clr.DARKAQUA + " - Set another user's mode");
				String modes = Clr.NOTE + "Allowed modes:";
				for (GameMode gm : GameMode.values()) {
					String gml = gm.name().toLowerCase();
					modes += (mod.perm(sender, "gamemode.other." + gml) ? " " + gml : "");
				}
				sender.sendMessage(modes);
			}
		}
	}

}
