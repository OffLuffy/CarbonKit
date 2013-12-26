package me.offluffy.carbonkit.cmds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleCmdBlockTools;
import me.offluffy.carbonkit.utils.EntityHelper;
import me.offluffy.carbonkit.utils.EntityHelper.EntityGroup;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Log;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Clr;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;
import me.offluffy.carbonkit.utils.NumUtils;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

public class CommandCmdBlockKill extends ModuleCmd {
	private ModuleCmdBlockTools cMod = (ModuleCmdBlockTools)mod;
	public CommandCmdBlockKill(Module module) {
		super(module, "cbkill");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof BlockCommandSender)) {
			sender.sendMessage(Clr.HEAD + "Usage: " + Clr.NORM + "/cbk [mobTypes] [radius]");
			sender.sendMessage(Clr.HEAD + "Mobs: " + Clr.NORM + "Any mob name, hostile, passive, neutral, tame, or all");
			Messages.send(sender, Message.CMDBLOCK);
			return true;
		}
		
		BlockCommandSender bcs = (BlockCommandSender)sender;
		Location loc = bcs.getBlock().getLocation();
		List<EntityType> mobs = new ArrayList<EntityType>();
		boolean tamed = false;
		
		// Check if there are enough / too many arguments
		if (args.length != 2) {
			cmdNotify("Invalid arguments! (/cbk [mob] [radius])", loc);
			return true;
		}
		
		// Parse mob types
		for (String s : args[0].split(",")) {
			if (Lib.eq(s, "hostile","hostiles","hmob","hmobs","hm","h")) {
				mobs.addAll(EntityHelper.getGroups(EntityGroup.HOSTILE));
			} else if (Lib.eq(s, "neutral","neutrals","nmob","nmobs","nm","n")) {
				mobs.addAll(EntityHelper.getGroups(EntityGroup.NEUTRAL));
			} else if (Lib.eq(s, "passive","passives","pmob","pmobs","pm","p")) {
				mobs.addAll(EntityHelper.getGroups(EntityGroup.PASSIVE));
			} else if (Lib.eq(s, "tame","tamed","pet","pets","tmob","tmobs","t")) {
				tamed = true;
			} else if (Lib.eq(s, "all","everything")) {
				mobs.addAll(EntityHelper.getGroups(EntityGroup.LIVING));
			} else if (EntityHelper.getType(s) != null) {
				if (!mobs.contains(EntityHelper.getType(s)))
					mobs.add(EntityHelper.getType(s));
			} else {
				cmdNotify("Invalid entity type: \"" + s + "\"", loc);
				return true;
			}
		}
		
		// Set the radius
		long rad = 0;
		if (NumUtils.isLong(args[1])) {
			rad = Long.parseLong(args[1]);
		} else {
			cmdNotify("Invalid radius!", loc);
			return true;
		}
		
		List<LivingEntity> targets = EntityHelper.getTargets(loc, mobs, rad);
		
		for (LivingEntity lEnt : targets) {
			if (lEnt instanceof Tameable) {
				if (!((Tameable)lEnt).isTamed() || tamed)
					lEnt.remove();
			} else { lEnt.remove(); }
		}
		
		if (CarbonKit.config.getBoolean(cMod.getName() + ".log-messages.kill")) {
			Log.log("Executed CmdBlockKill (/cbk) at (" + ((int)loc.getX()) + ", " + ((int)loc.getY()) + ", " + ((int)loc.getZ()) + ")");
			Log.log("Removed " + targets.size() + " entities in a radius of " + rad);
		}
		return true;
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		return new HashMap<String, List<String>>();
	}

}
