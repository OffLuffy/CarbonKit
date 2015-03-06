package net.teamcarbon.carbonkit.commands.CmdBlockTools;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.modules.CmdBlockToolsModule;
import net.teamcarbon.carbonkit.utils.CBModuleCmd;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.EntHelper;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

@SuppressWarnings("UnusedDeclaration")
public class CmdBlockSpawnCommand extends CBModuleCmd {
	public CmdBlockSpawnCommand(Module module) { super(module, "commandblockspawn"); }
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof BlockCommandSender)) {
			if (MiscUtils.perm(sender, "carbonkit.cmdblocktools.help.spawn"))
				sender.sendMessage(Clr.AQUA + "Usage: " + Clr.DARKAQUA + "/cbs [mobtype] <amount>");
			sender.sendMessage(CustomMessage.CB_NOT_CMD_BLOCK.pre());
			return;
		}
		CmdBlockToolsModule mod = (CmdBlockToolsModule)Module.getModule(getMod().getName());
		BlockCommandSender bcs = (BlockCommandSender)sender;
		Location loc = bcs.getBlock().getLocation();
		World world = loc.getWorld();
		Location top = bcs.getBlock().getLocation();
		while (top.getBlock().getType() != Material.AIR) {
			if (top.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR)
				if (top.getBlock().getRelative(BlockFace.UP,2).getType() != Material.AIR)
					top.setY(top.getY()+3);
				else
					top.setY(top.getY()+2);
			else
				top.setY(top.getY()+1);
		}
		top.setX(top.getBlockX()+.5d);
		top.setZ(top.getBlockZ()+.5d);
		if (args.length > 0) {
			EntityType mob = EntHelper.getType(args[0]);
			int amount = 1;
			if (args.length < 1 || args.length > 2) {
				cmdNotify("Invalid arguments! (/cbs [mob] <amount>)", loc);
				return;
			} else if (args.length == 2) {
				if (MiscUtils.isInteger(args[1])) {
					try {
						amount = Integer.parseInt(args[1]);
						if (amount > mod.getConfig().getInt("spawn-limit", 50))
							amount = mod.getConfig().getInt("spawn-limit", 50);
					} catch (Exception e) {
						cmdNotify("Invalid amount!", loc);
						return;
					}
				} else {
					cmdNotify("Invalid amount!", loc);
					return;
				}
			}
			if (mob != null) {
				for (int i = 0; i < amount; i++)
					world.spawnEntity(top, mob);
				if (mod.getConfig().getBoolean("log-messages.spawn", true))
					CarbonKit.log.info("Spawned " + amount + " mob" + ((amount > 1) ? "s" : "") + ", type: " + mob.getName() + " @  (" + world.getName() + ", " + ((int) top.getX()) + ", " + ((int) top.getY()) + ", " + ((int) top.getZ()) + ")");
			} else {
				cmdNotify("An invalid mob was specified", loc);
			}
		}
	}
}
