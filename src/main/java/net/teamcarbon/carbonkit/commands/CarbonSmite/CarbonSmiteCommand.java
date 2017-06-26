package net.teamcarbon.carbonkit.commands.CarbonSmite;

import net.teamcarbon.carbonkit.modules.CarbonSmiteModule;
import net.teamcarbon.carbonkit.utils.EntHelper.EntityGroup;
import net.teamcarbon.carbonkit.utils.MiscUtils;
import net.teamcarbon.carbonkit.utils.Messages.Clr;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonkit.utils.GoldenSmite.Smite;
import net.teamcarbon.carbonkit.utils.GoldenSmite.Smite.SmiteType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.HashMap;

// TODO Update to support each EntityType toggle and group toggles

@SuppressWarnings("UnusedDeclaration")
public class CarbonSmiteCommand extends ModuleCmd {
	public CarbonSmiteCommand(Module module) {
		super(module, "carbonsmite");
	}

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(mod.getCoreMsg("not-online", false));
			return;
		}
		HashMap<String, String> rep = new HashMap<>();
		if (args.length > 0) {
			if (EntityGroup.getGroup(args[0]) != null) {
				EntityGroup eg = EntityGroup.getGroup(args[0]);
				if (eg == null) {
					sender.sendMessage(mod.getMsg("no-group-found", true));
					return;
				}
				if (!mod.perm(sender, "toggle." + eg.lname())) {
					sender.sendMessage(mod.getCoreMsg("not-online", false));
					return;
				}
				CarbonSmiteModule.toggleGroup((Player) sender, eg);
				rep.put("{STATUS}", CarbonSmiteModule.isGroupEnabled((Player) sender, eg) ? "enabled" : "disabled");
				rep.put("{GROUP}", eg.lname());
				sender.sendMessage(mod.getMsg("toggled-group", true, rep));
			} else if (MiscUtils.eq(args[0], "list", "l")) {
				MiscUtils.printHeader(sender, "Enabled Types");
				for (EntityGroup eg : EntityGroup.values())
					sender.sendMessage((CarbonSmiteModule.isGroupEnabled((Player) sender, eg) ? Clr.LIME + "[ON]" : Clr.RED + "[OFF]") + Clr.RESET + " " + eg.lname());
			} else if (MiscUtils.eq(args[0], "arrow", "a", "bow")) {
				if (!mod.perm(sender, "use.arrows")) {
					sender.sendMessage(mod.getCoreMsg("not-online", false));
					return;
				}
				CarbonSmiteModule.toggleArrows((Player) sender);
				rep.put("{STATUS}", CarbonSmiteModule.hasArrowsEnabled((Player) sender) ? "enabled" : "disabled");
				sender.sendMessage(mod.getMsg("toggled-arrow", true, rep));
			} else if (MiscUtils.eq(args[0], "snowball", "sb")) {
				if (!mod.perm(sender, "use.snowballs")) {
					sender.sendMessage(mod.getCoreMsg("not-online", false));
					return;
				}
				CarbonSmiteModule.toggleSnowballs((Player) sender);
				rep.put("{STATUS}", CarbonSmiteModule.hasSnowballsEnabled((Player) sender) ? "enabled" : "disabled");
				sender.sendMessage(mod.getMsg("toggled-snowball", true, rep));
			} else if (MiscUtils.eq(args[0], "here")) {
				if (!mod.perm(sender, "here")) {
					sender.sendMessage(mod.getCoreMsg("not-online", false));
					return;
				}
				Smite.createSmite((Player) sender, ((Player) sender).getLocation(), SmiteType.CMD);
			} else {
				sender.sendMessage(Clr.RED + "Unrecognized sub-command");
			}
		} else {
			if (!mod.perm(sender, "use.cmd")) {
				sender.sendMessage(mod.getCoreMsg("not-online", false));
				return;
			}
			BlockIterator bi = new BlockIterator((Player) sender, 100);
			Block b = null;
			while (bi.hasNext() && (b == null || b.getType().equals(Material.AIR))) { b = bi.next(); }
			if (b != null) Smite.createSmite((Player) sender, b.getLocation(), SmiteType.CMD);
		}
	}
}
