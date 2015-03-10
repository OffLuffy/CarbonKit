package net.teamcarbon.carbonkit.commands.CmdBlockTools;

import net.teamcarbon.carbonkit.CarbonKit;
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
			Material item = MiscUtils.getMaterial(args[0]);
			if (item == null) {
				cmdNotify("The item specified was not valid", loc);
				return;
			}
			int amount = 1;
			if (args.length > 1) {
				try {
					amount = Integer.parseInt(args[1]);
					amount = MiscUtils.normalizeInt(amount, 1, getMod().getConfig().getInt("item-limit", 50));
				} catch (Exception e) {}
			}
			ItemStack is = new ItemStack(item, amount);
			if (is != null && is.getAmount() > 0) {
				world.dropItem(top, is);
				if (getMod().getConfig().getBoolean("log-messages.item", true))
					CarbonKit.log.info("Spawned " + amount + " item" + ((amount > 1) ? "s" : "") + ", type: " + is.getType().name() + " on " + world.getName() + " at (" + ((int) top.getX()) + ", " + ((int) top.getY()) + ", " + ((int) top.getZ()) + ")");
			}
		}
	}
}
