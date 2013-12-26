package me.offluffy.carbonkit.cmds;

import java.util.ArrayList;
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
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class CommandCmdBlockClear extends ModuleCmd {
	private ModuleCmdBlockTools cMod = (ModuleCmdBlockTools)mod;
	public CommandCmdBlockClear(Module module) {
		super(module, "cbclear");
	}
	
	enum EntType {
		ITEM(EntityType.DROPPED_ITEM , "item", "items", "drop", "drops"),
		ARROW(EntityType.ARROW , "arrow", "arrows"),
		BOAT(EntityType.BOAT , "boat", "boats"),
		CART(EntityType.MINECART , "cart", "carts", "minecart", "minecarts"),
		CHESTCART(EntityType.MINECART_CHEST , "ccart", "chestcart", "chestcarts", "cartchest", "cartchests"),
		TNTCART(EntityType.MINECART_TNT , "tcart", "tntcart", "tntcarts", "tntminecart", "tntminecarts", "carttnt", "carttnts", "minecarttnt", "minecarttnts"),
		SPAWNERCART(EntityType.MINECART_MOB_SPAWNER , "scart", "spawnercart", "spawnercarts", "spawnerminecart", "spawnerminecarts", "cartspawner", "cartspawners", "minecartspawner", "minecartspawners"),
		HOPPERCART(EntityType.MINECART_HOPPER , "hcart", "hoppercart", "hoppercarts", "hopperminecart", "hopperminecarts", "carthopper", "carthoppers", "minecarthopper", "minecarthoppers"),
		FURNACECART(EntityType.MINECART_FURNACE , "fcart", "furnacecart", "furnacecarts", "furnaceminecart", "furnaceminecarts", "cartfurnace", "furnacecarts", "minecartfurnace", "minecartfurnaces"),
		ORB(EntityType.EXPERIENCE_ORB , "orb", "orbs", "xp", "xporb", "xporbs", "exp", "exporb", "exporbs");
		String[] aliases;
		EntityType type;
		EntType(EntityType type, String ... aliases) {
			this.aliases = aliases;
			this.type = type;
		}
		public String[] getAliases() { return aliases; }
		public EntityType getEntityType() { return type; }
		public static boolean isValid(String type) {
			for (EntType t : values())
				for (String s : t.getAliases())
					if (s.equalsIgnoreCase(type))
						return true;
			return false;
		}
		public static EntType getType(String type) {
			for (EntType t : values())
				for (String s : t.getAliases())
					if (s.equalsIgnoreCase(type))
						return t;
			return null;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof BlockCommandSender)) {
			sender.sendMessage(Clr.HEAD + "Usage: " + Clr.NORM + "/cbc [type] [radius]");
			sender.sendMessage(Clr.HEAD + "Types: " + Clr.NORM + "item, arrow, boat, cart, chestcart, tntcart, spawnercart, hoppercart, furnacecart, orb");
			Messages.send(sender, Message.CMDBLOCK);
			return true;
		}
		BlockCommandSender bcs = (BlockCommandSender)sender;
		Location loc = bcs.getBlock().getLocation();
		int entCount = 0;
		long radCount = 0;
		if (args.length == 2) {
			for (String s : args[0].split(",")) {
				EntType type = null;
				if (EntType.isValid(s))
					type = EntType.getType(s);
				else {
					cmdNotify("Invalid entity type was specified", loc);
					return true;
				}
				if (type == null) {
					cmdNotify("Entity type is null", loc);
					return true;
				}
				long radius = 0;
				if(NumUtils.isInteger(args[1]))
					radius = Long.parseLong(args[1]);
				else {
					cmdNotify("", loc);
					return true;
				}
				if (radius <= 0) {
					cmdNotify("Radius is too small or negative (Must be greater than 0)", loc);
					return true;
				}
				List<Entity> targets = getTargets(loc, radius, type);
				radCount = radius;
				entCount += targets.size();
				for (Entity ent : targets)
					ent.remove();
			}
			if (CarbonKit.config.getBoolean(cMod.getName() + ".log-messages.clear"))
				Log.log("Executed CmdBlockClear (/cbc) at (" + ((int)loc.getX()) + ", " + ((int)loc.getY()) + ", " + ((int)loc.getZ()) + ")");
			if (CarbonKit.config.getBoolean(cMod.getName() + ".log-messages.clear"))
				Log.log("Removed " + entCount + " items in a radius of " + radCount);
			return true;
		}
		cmdNotify("Not enough arguments (/cbc [type] [radius])", loc);
		return true;
	}
	
	private List<Entity> getTargets(Location l, long rad, EntType type) {
		List<Entity> targets = new ArrayList<Entity>();
		List<Entity> ents = l.getWorld().getEntities();
		int x = (int)l.getX(),
			y = (int)l.getY(),
			z = (int)l.getZ();
		Double	ix, iy, iz;
		for (Entity e : ents) {
			if (e.getType().equals(type.getEntityType())) {
				Location il = e.getLocation();
				ix = il.getX();
				iy = il.getY();
				iz = il.getZ();
				if ((x-rad <= ix && x+rad >= ix) && (y-rad <= iy && y+rad >= iy) && (z-rad <= iz && z+rad >= iz))
					targets.add(e);
			}
		}
		return targets;
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		return new HashMap<String, List<String>>();
	}

}
