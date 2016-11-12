package net.teamcarbon.carbonkit.commands.CarbonSmite;

import net.teamcarbon.carbonkit.modules.CarbonSmiteModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.EntHelper.EntityGroup;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
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
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		HashMap<String, String> rep = new HashMap<>();
		if (args.length > 0) {
			if (EntityGroup.getGroup(args[0]) != null) {
				EntityGroup eg = EntityGroup.getGroup(args[0]);
				if (!mod.perm(sender, "toggle." + eg.lname())) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				CarbonSmiteModule.toggleGroup((Player) sender, eg);
				rep.put("{STATUS}", CarbonSmiteModule.isGroupEnabled((Player) sender, eg) ? "enabled" : "disabled");
				rep.put("{GROUP}", eg.lname());
				sender.sendMessage(CustomMessage.GS_TOGGLED_GROUP.pre(rep));
			} else if (MiscUtils.eq(args[0], "list", "l")) {
				CustomMessage.printHeader(sender, "Enabled Types");
				for (EntityGroup eg : EntityGroup.values())
					sender.sendMessage((CarbonSmiteModule.isGroupEnabled((Player) sender, eg) ? Clr.LIME + "[ON]" : Clr.RED + "[OFF]") + Clr.RESET + " " + eg.lname());
			} else if (MiscUtils.eq(args[0], "arrow", "a", "bow")) {
				if (!mod.perm(sender, "use.arrows")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				CarbonSmiteModule.toggleArrows((Player) sender);
				rep.put("{STATUS}", CarbonSmiteModule.hasArrowsEnabled((Player) sender) ? "enabled" : "disabled");
				sender.sendMessage(CustomMessage.GS_TOGGLED_ARROW.pre(rep));
			} else if (MiscUtils.eq(args[0], "snowball", "sb")) {
				if (!mod.perm(sender, "use.snowballs")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				CarbonSmiteModule.toggleSnowballs((Player) sender);
				rep.put("{STATUS}", CarbonSmiteModule.hasSnowballsEnabled((Player) sender) ? "enabled" : "disabled");
				sender.sendMessage(CustomMessage.GS_TOGGLED_SNOWBALL.pre(rep));
			} else if (MiscUtils.eq(args[0], "here")) {
				if (!mod.perm(sender, "here")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				Smite.createSmite((Player) sender, ((Player) sender).getLocation(), SmiteType.CMD);
			} else {
				sender.sendMessage(Clr.RED + "Unrecognized sub-command");
			}
		} else {
			if (!mod.perm(sender, "use.cmd")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			BlockIterator bi = new BlockIterator((Player) sender, 100);
			Block b;
			for (b = null; bi.hasNext() && (b == null || b.getType().equals(Material.AIR)); b = bi.next()) ;
			if (b != null)
				Smite.createSmite((Player) sender, b.getLocation(), SmiteType.CMD);
		}
	}
}
