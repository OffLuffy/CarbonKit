package net.teamcarbon.carbonkit.commands.GoldenSmite;

import net.teamcarbon.carbonkit.modules.GoldenSmiteModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.EntHelper.EntityGroup;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonkit.utils.Smite;
import net.teamcarbon.carbonkit.utils.Smite.SmiteType;
import net.teamcarbon.carbonlib.Messages.Clr;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class GoldenSmiteCommand extends ModuleCmd {
	public GoldenSmiteCommand(Module module) {
		super(module, "goldensmite");
	}

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		HashMap<String, String> rep = new HashMap<String, String>();
		if (args.length > 0) {
			if (EntityGroup.getGroup(args[0]) != null) {
				EntityGroup eg = EntityGroup.getGroup(args[0]);
				if (!MiscUtils.perm(sender, "carbonkit.goldensmite.toggle." + eg.lname())) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				GoldenSmiteModule.toggleGroup((Player) sender, eg);
				rep.put("{STATUS}", GoldenSmiteModule.isGroupEnabled((Player) sender, eg) ? "enabled" : "disabled");
				rep.put("{GROUP}", eg.lname());
				sender.sendMessage(MiscUtils.massReplace(CustomMessage.GS_TOGGLED_GROUP.pre(), rep));
			} else if (MiscUtils.eq(args[0], "list", "l")) {
				CustomMessage.printHeader(sender, "GSmite Enabled Types");
				for (EntityGroup eg : EntityGroup.values())
					sender.sendMessage((GoldenSmiteModule.isGroupEnabled((Player) sender, eg) ? Clr.LIME + "[ON]" : Clr.RED + "[OFF]") + Clr.RESET + " " + eg.lname());
			} else if (MiscUtils.eq(args[0], "arrow", "a", "bow")) {
				if (!MiscUtils.perm(sender, "carbonkit.goldensmite.use.arrows")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				GoldenSmiteModule.toggleArrows((Player) sender);
				rep.put("{STATUS}", GoldenSmiteModule.hasArrowsEnabled((Player) sender) ? "enabled" : "disabled");
				sender.sendMessage(MiscUtils.massReplace(CustomMessage.GS_TOGGLED_ARROW.pre(), rep));
			} else if (MiscUtils.eq(args[0], "snowball", "sb")) {
				if (!MiscUtils.perm(sender, "carbonkit.goldensmite.use.snowballs")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				GoldenSmiteModule.toggleSnowballs((Player) sender);
				rep.put("{STATUS}", GoldenSmiteModule.hasSnowballsEnabled((Player) sender) ? "enabled" : "disabled");
				sender.sendMessage(MiscUtils.massReplace(CustomMessage.GS_TOGGLED_SNOWBALL.pre(), rep));
			} else if (MiscUtils.eq(args[0], "here")) {
				if (!MiscUtils.perm(sender, "carbonkit.goldensmite.here")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				Smite.createSmite((Player) sender, ((Player) sender).getLocation(), SmiteType.CMD);
			} else {
				sender.sendMessage(Clr.RED + "Unrecognized sub-command");
			}
		} else {
			if (!MiscUtils.perm(sender, "carbonkit.goldensmite.use.cmd")) {
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
