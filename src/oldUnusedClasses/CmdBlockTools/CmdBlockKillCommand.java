package net.teamcarbon.carbonkit.commands.CmdBlockTools;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.modules.CmdBlockToolsModule;
import net.teamcarbon.carbonkit.utils.CBModuleCmd;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.EntHelper;
import net.teamcarbon.carbonkit.utils.EntHelper.EntityGroup;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class CmdBlockKillCommand extends CBModuleCmd {
	public CmdBlockKillCommand(Module module) { super(module, "commandblockkill"); }
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof BlockCommandSender)) {
			if (MiscUtils.perm(sender, "carbonkit.cmdblocktools.help.kill")) {
				sender.sendMessage(Clr.AQUA + "Usage: " + Clr.DARKAQUA + "/cbk [mobtypes] <radius>");
				sender.sendMessage(Clr.AQUA + "Types: " + Clr.DARKAQUA + "hostile, neutral, passive, tamed, player, drop, or specific mob names");
			}
			sender.sendMessage(CustomMessage.CB_NOT_CMD_BLOCK.pre());
			return;
		}
		CmdBlockToolsModule mod = (CmdBlockToolsModule)Module.getModule(getMod().getName());
		BlockCommandSender bcs = (BlockCommandSender)sender;
		Location loc = bcs.getBlock().getLocation();
		List<EntityType> mobs = new ArrayList<EntityType>();
		boolean tamed = false;
		// Check if there are enough / too many arguments
		if (args.length < 2) {
			cmdNotify("Invalid arguments! (/cbk [mob] [radius])", loc);
			return;
		}
		// Parse mob types
		for (String s : args[0].split(",")) {
			if (EntityGroup.getGroup(s) != null)
				mobs.addAll(EntHelper.getGroups(EntityGroup.getGroup(s)));
			else if (EntHelper.getType(s) != null) {
				if (!mobs.contains(EntHelper.getType(s)))
					mobs.add(EntHelper.getType(s));
			} else {
				cmdNotify("Invalid entity type: " + s, loc);
				return;
			}
		}
		// Set the radius
		int rad;
		if (MiscUtils.isInteger(args[1])) {
			rad = Integer.parseInt(args[1]);
		} else {
			cmdNotify("Invalid radius!", loc);
			return;
		}
		if (rad <= 0) {
			cmdNotify("Radius is too small or negative (Must be greater than 0)", loc);
			return;
		} else if (rad > mod.getConfig().getInt("kill-radius-limit", 1000)) {
			cmdNotify("Radius is too large! (Must be smaller than " + mod.getConfig().getInt("kill-radius-limit", 1000) + ")", loc);
			return;
		}
		List<LivingEntity> targets = EntHelper.getTargets(loc, rad, mobs);
		for (LivingEntity lEnt : targets) {
			if (lEnt instanceof Tameable) {
				if (!((Tameable)lEnt).isTamed() || tamed)
					lEnt.remove();
			} else { lEnt.remove(); }
		}
		if (mod.getConfig().getBoolean("log-messages.kill")) {
			CarbonKit.log.info("Executed CmdBlockKill (/cbk) at (" + ((int) loc.getX()) + ", " + ((int) loc.getY()) + ", " + ((int) loc.getZ()) + ")");
			CarbonKit.log.info("Removed " + targets.size() + " entities in a radius of " + rad);
		}
	}
}
