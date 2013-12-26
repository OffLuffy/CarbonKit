package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleCmdBlockTools;
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
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class CommandCmdBlockItem extends ModuleCmd {
	private ModuleCmdBlockTools cMod = (ModuleCmdBlockTools)mod;
	public CommandCmdBlockItem(Module module) {
		super(module, "cbitem");
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof BlockCommandSender)) {
			sender.sendMessage(Clr.HEAD + "Usage: " + Clr.NORM + "/cbi [item] <amount>");
			Messages.send(sender, Message.CMDBLOCK);
			return true;
		}
		BlockCommandSender bcs = (BlockCommandSender)sender;
		Location loc = bcs.getBlock().getLocation();
		World world = loc.getWorld();
		Location top = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
		
		while (top.getBlock().getType() != Material.AIR)
			top.setY(top.getY()+.5d);

		top.setX(top.getX()+.5d);
		top.setZ(top.getZ()+.5d);
		
		if (args.length == 0) {
			cmdNotify("Not enough arguments (/cbi [item] <amount>)", loc);
			return true;
		} else {
			Material item = null;
			if (NumUtils.isInteger(args[0])) {
				item = Material.getMaterial(Integer.parseInt(args[0]));
			} else {
				String itemName = args[0];
				boolean cont = true;
				for (int i = 1; i < args.length && cont; i++) {
					if (NumUtils.isInteger(args[i])) {
						Integer.parseInt(args[i]);
						cont = false;
					} else {
						itemName += args[i];
					}
				}
				item = getItem(itemName);
			}
			if (item == null) {
				cmdNotify("The item specified was not valid (Consider using an ID instead)", loc);
				return true;
			}
			ItemStack is = new ItemStack(item);
			int amount = 1;
			if (args.length > 1) {
				try {
					amount = Integer.parseInt(args[1]);
					if (amount > CarbonKit.config.getInt(cMod.getName() + ".spawn-limit"))
						amount = CarbonKit.config.getInt(cMod.getName() + ".spawn-limit", 100);
				} catch (Exception e) {}
				is.setAmount(amount);
			}
			if (is != null && is.getAmount() > 0) {
				world.dropItem(top, is);
				if (CarbonKit.config.getBoolean(cMod.getName() + ".log-messages.item", true))
					Log.log("Spawned " + amount + " item" + ((amount > 1)?"s":"") + ", type: " + is.getType().name() + " on " + world.getName() + " at (" + ((int)top.getX()) + ", " + ((int)top.getY()) + ", " + ((int)top.getZ()) + ")");
			}
			return true;
		}
	}
	
	private Material getItem(String item) {
		Material[] items = Material.values();
		for (int i = 0; i < items.length; i++) {
			if (item.replace("_","").toLowerCase().equals(items[i].name().toLowerCase().replace("_","")))
				return items[i];
			if (item.replace(" ","_").toLowerCase().equals(items[i].name().toLowerCase()))
				return items[i];
		}
		return null;
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		return new HashMap<String, List<String>>();
	}

}
