package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleSkullShop;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGetSkull extends ModuleCmd {
	private ModuleSkullShop sMod = (ModuleSkullShop)mod;
	public CommandGetSkull(Module module) {
		super(module, "getskull");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!canExec(sender, false, "")) {
			Messages.send(sender, Message.NO_PERM);
			return true;
		}
		if (((Player)sender).getInventory().firstEmpty() == -1) {
			sender.sendMessage(Messages.Clr.ERR + "You don't have room for this in your inventory.");
			return true;
		}
		if (ModuleSkullShop.getQueuedSkull(sender.getName()) != null) {
			String skullOwner = ModuleSkullShop.getQueuedSkull(sender.getName());
			String owner = ((sender.getName().equalsIgnoreCase(skullOwner)) ? "your" : skullOwner+"'s");
			
			if (!Lib.perm(sender, "carbonkit.skull.free") && CarbonKit.config.getDouble(sMod.getName() + ".price") > 0) {
				double bal = CarbonKit.econ.getBalance(sender.getName());
				double prc = CarbonKit.config.getDouble(sMod.getName() + ".price", 50000d);
				String price = ( (prc%1.0==0) ? ""+((int)prc) : String.format("%.2f", prc) );
				if (bal < prc) {
					sender.sendMessage(ChatColor.RED + "You don't have enough money! (" + price + ")");
					return true;
				} else {
					CarbonKit.econ.withdrawPlayer(sender.getName(), prc);
					sender.sendMessage(Messages.Clr.NORM + "You've paid " + Messages.Clr.HEAD + price + Messages.Clr.NORM + " for " + Messages.Clr.HEAD + owner + Messages.Clr.NORM + " head. Type " + Messages.Clr.HEAD + "/getskull" + Messages.Clr.NORM + " to buy it again.");
				}
			} else
				sender.sendMessage(Messages.Clr.NORM + "You've recieved " + Messages.Clr.HEAD + owner + Messages.Clr.NORM + " head. Type " + Messages.Clr.HEAD + "/getskull" + Messages.Clr.NORM + " to buy it again.");
			
			((Player)sender).getInventory().addItem(ModuleSkullShop.createSkull(skullOwner));
			return true;
		} else {
			sender.sendMessage(Messages.Clr.ERR + "You have no skull in queue. Right click one first!");
			return true;
		}
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.skull"));
		return perms;
	}

}
