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

public class CommandSkull extends ModuleCmd {
	private ModuleSkullShop sMod = (ModuleSkullShop)mod;
	public CommandSkull(Module module) {
		super(module, "skull");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Messages.send(sender, Message.NOT_ONLINE);
			return true;
		}
		String skullOwner = "Herobrine";
		if (args.length > 0) {
			if (!canExec(sender, false, "")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			if (((Player)sender).getInventory().firstEmpty() == -1) {
				sender.sendMessage(Messages.Clr.ERR + "You don't have room for this in your inventory.");
				return true;
			}
			String user = args[0];
			for (Player pl : CarbonKit.inst.getServer().getOnlinePlayers()) 
				if (pl.getName().equalsIgnoreCase(user))
					user = pl.getName();
			skullOwner = user;
		} else {
			if (!canExec(sender, false, "")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			double prc = CarbonKit.config.getDouble(sMod.getName() + ".price", 50000d);
			String price = "";
			if (prc > 0 && !Lib.perm(sender, "carbonkit.skull.free"))
				price = " for " + ( (prc%1.0==0) ? ""+((int)prc) : String.format("%.2f", prc) );
			sender.sendMessage(Messages.Clr.HEAD + "/" + label + " <Player> " + Messages.Clr.NORM + "- Get a player's head" + price);
			return true;
		}
		
		String owner = ((sender.getName().equalsIgnoreCase(skullOwner)) ? "your" : skullOwner+"'s");
		
		if (!Lib.perm(sender, "carbonkit.skull.free") && CarbonKit.config.getDouble(sMod.getName() + ".price") > 0) {
			double bal = CarbonKit.econ.getBalance(sender.getName());
			double prc = CarbonKit.config.getDouble(sMod.getName() + ".price");
			String price = ( (prc%1.0==0) ? ""+((int)prc) : String.format("%.2f", prc) );
			if (bal < prc) {
				sender.sendMessage(ChatColor.RED + "You don't have enough money! (" + price + ")");
				return true;
			} else {
				CarbonKit.econ.withdrawPlayer(sender.getName(), CarbonKit.config.getDouble(sMod.getName() + ".price"));
				sender.sendMessage(Messages.Clr.NORM + "You've paid " + Messages.Clr.NORM + price + Messages.Clr.NORM + " for " + Messages.Clr.HEAD + owner + Messages.Clr.NORM + " head.");
			}
		} else
			sender.sendMessage(Messages.Clr.NORM + "You've recieved " + Messages.Clr.HEAD + owner + Messages.Clr.NORM + " head");
		
		((Player)sender).getInventory().addItem(ModuleSkullShop.createSkull(skullOwner));
		
		return true;
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.skull"));
		return perms;
	}

}
