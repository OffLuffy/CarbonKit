package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleCmdBlockTools;
import me.offluffy.carbonkit.utils.EntityHelper;
import me.offluffy.carbonkit.utils.Log;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Clr;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;
import me.offluffy.carbonkit.utils.NumUtils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

public class CommandCmdBlockSpawn extends ModuleCmd {
	private ModuleCmdBlockTools cMod = (ModuleCmdBlockTools)mod;
	public CommandCmdBlockSpawn(Module module) {
		super(module, "cbspawn");
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof BlockCommandSender)) {
			sender.sendMessage(Clr.HEAD + "Usage: " + Clr.NORM + "/cbs [mob] <amount>");
			Messages.send(sender, Message.CMDBLOCK);
			return true;
		}
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
		
		if (args.length == 0)
			return true;
		else {
			EntityType mob = EntityHelper.getType(args[0]);
			int amount = 1;
			if (args.length < 1 || args.length > 2) {
				cmdNotify("Invalid arguments! (/cbs [mob] <amount>)", loc);
				return true;
			} else if (args.length == 2) {
				if (NumUtils.isInteger(args[1])) {
					try {
						amount = Integer.parseInt(args[1]);
						if (amount > CarbonKit.config.getInt(cMod.getName() + ".spawn-limit"))
							amount = CarbonKit.config.getInt(cMod.getName() + ".spawn-limit", 100);
					} catch (Exception e) {}
				} else {
					cmdNotify("Invalid amount!", loc);
					return true;
				}
			}
			if (mob != null) {
				for (int i = 0; i < amount; i++)
					world.spawnEntity(top, mob);
				if (CarbonKit.config.getBoolean(cMod.getName() + ".log-messages.spawn", true))
					Log.log("Spawned " + amount + " mob" + ((amount > 1)?"s":"") + ", type: " + mob.getName() + " on " + world.getName() + " at (" + ((int)top.getX()) + ", " + ((int)top.getY()) + ", " + ((int)top.getZ()) + ")");
				return true;
			} else {
				cmdNotify("An invalid mob was specified", loc);
				return true;
			}
		}
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		return new HashMap<String, List<String>>();
	}

}
