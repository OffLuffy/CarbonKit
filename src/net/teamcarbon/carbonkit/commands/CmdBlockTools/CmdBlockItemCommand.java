package net.teamcarbon.carbonkit.commands.CmdBlockTools;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.modules.CmdBlockToolsModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import net.teamcarbon.carbonkit.utils.CBModuleCmd;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;

@SuppressWarnings("UnusedDeclaration")
public class CmdBlockItemCommand extends CBModuleCmd {
	public CmdBlockItemCommand(Module module) { super(module, "commandblockitem"); }
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof BlockCommandSender)) {
			if (MiscUtils.perm(sender, "carbonkit.cmdblocktools.help.item"))
				sender.sendMessage(Clr.AQUA + "Usage: " + Clr.DARKAQUA + "/cbi [item] <amount>");
			sender.sendMessage(CustomMessage.CB_NOT_CMD_BLOCK.pre());
			return;
		}
		CmdBlockToolsModule mod = (CmdBlockToolsModule) Module.getModule(getMod().getName());
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
		} else {
			Material item;
			if (MiscUtils.isInteger(args[0])) {
				item = Material.getMaterial(Integer.parseInt(args[0]));
			} else {
				String itemName = args[0];
				boolean cont = true;
				for (int i = 1; i < args.length && cont; i++) {
					if (MiscUtils.isInteger(args[i]))
						cont = false;
					else
						itemName += args[i];
				}
				item = MiscUtils.getMaterial(itemName);
			}
			if (item == null) {
				cmdNotify("The item specified was not valid", loc);
				return;
			}
			ItemStack is = new ItemStack(item);
			int amount = 1;
			if (args.length > 1) {
				try {
					amount = Integer.parseInt(args[1]);
					if (amount > mod.getConfig().getInt("item-limit", 50))
						amount = mod.getConfig().getInt("item-limit", 50);
				} catch (Exception e) {}
				is.setAmount(amount);
			}
			if (is != null && is.getAmount() > 0) {
				world.dropItem(top, is);
				if (mod.getConfig().getBoolean("log-messages.item", true))
					CarbonKit.log.info("Spawned " + amount + " item" + ((amount > 1) ? "s" : "") + ", type: " + is.getType().name() + " on " + world.getName() + " at (" + ((int) top.getX()) + ", " + ((int) top.getY()) + ", " + ((int) top.getZ()) + ")");
			}
		}
	}
}
