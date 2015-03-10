package net.teamcarbon.carbonkit.commands.CmdBlockTools;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.CBModuleCmd;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class CmdBlockClearCommand extends CBModuleCmd {
	public CmdBlockClearCommand(Module module) { super(module, "commandblockclear"); }
	enum EntType {
		TNT(EntityType.PRIMED_TNT),
		ITEM(EntityType.DROPPED_ITEM, "drop", "itemdrop"),
		ARROW(EntityType.ARROW),
		BOAT(EntityType.BOAT),
		CART(EntityType.MINECART),
		CHESTCART(EntityType.MINECART_CHEST, "ccart", "chestminecart", "cartchest"),
		TNTCART(EntityType.MINECART_TNT, "tcart", "tntminecart", "carttnt"),
		SPAWNERCART(EntityType.MINECART_MOB_SPAWNER, "scart", "spawnerminecart", "cartspawner", "minecartspawner"),
		HOPPERCART(EntityType.MINECART_HOPPER, "hcart", "hopperminecart", "carthopper"),
		FURNACECART(EntityType.MINECART_FURNACE, "fcart", "furnaceminecart", "cartfurnace"),
		ORB(EntityType.EXPERIENCE_ORB, "xp", "xporb", "exp", "exporb");
		String[] aliases;
		EntityType type;
		EntType(EntityType type, String ... aliases) {
			this.aliases = aliases;
			this.type = type;
		}
		public String[] getAliases() { return aliases; }
		public EntityType getEntityType() { return type; }
		public static boolean isValid(String name) { return getType(name) != null; }
		public static EntType getType(String name) {
			for (EntType et : values()) {
				if (MiscUtils.eq(name, et.type.name(), et.name(), et.type.name()+"s", et.name() + "s")) return et;
				for (String s : et.getAliases()) if (MiscUtils.eq(name, s, s+"s")) return et;
			}
			return null;
		}
	}
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof BlockCommandSender)) {
			if (MiscUtils.perm(sender, "carbonkit.cmdblocktools.help.clear")) {
				sender.sendMessage(Clr.AQUA + "Usage: " + Clr.DARKAQUA + "/cbc [types] <radius>");
				sender.sendMessage(Clr.AQUA + "Types: " + Clr.DARKAQUA + "tnt, item, arrow, boat, cart, chestcart, tntcart, spawnercart, hoppercart, furnacecart, orb");
				sender.sendMessage(Clr.DARKAQUA + "Multiple types can be specified with commas (no spaces before or after the commas)");
			}
			sender.sendMessage(CustomMessage.CB_NOT_CMD_BLOCK.pre());
			return;
		}
		BlockCommandSender bcs = (BlockCommandSender)sender;
		Location loc = bcs.getBlock().getLocation();
		int entCount = 0;
		long radCount = 0;
		if (args.length >= 2) {
			List<Entity> targets = new ArrayList<Entity>();
			long radius;
			if(MiscUtils.isInteger(args[1]))
				radius = Integer.parseInt(args[1]);
			else {
				cmdNotify("Radius must be a valid number", loc);
				return;
			}
			if (radius <= 0) {
				cmdNotify("Radius is too small or negative (Must be greater than 0)", loc);
				return;
			} else if (radius > getMod().getConfig().getInt("clear-radius-limit", 1000)) {
				cmdNotify("Radius is too large! (Must be smaller than " + getMod().getConfig().getInt("clear-radius-limit", 1000) + ")", loc);
				return;
			}
			for (String s : args[0].split(",")) {
				EntType type;
				if (EntType.isValid(s))
					type = EntType.getType(s);
				else {
					cmdNotify("Invalid entity type was specified", loc);
					continue;
				}
				targets.addAll(getTargets(loc, radius, type));
				radCount = radius;
				entCount += targets.size();
			}
			for (Entity ent : targets)
				ent.remove();
			if (getMod().getConfig().getBoolean("log-messages.clear"))
				CarbonKit.log.info("Executed CmdBlockClear (/cbc) at (" + ((int) loc.getX()) + ", " + ((int) loc.getY()) + ", " + ((int) loc.getZ()) + ")");
			if (getMod().getConfig().getBoolean("log-messages.clear"))
				CarbonKit.log
						.info("Removed " + entCount + " items in a radius of " + radCount);
			return;
		}
		cmdNotify("Not enough arguments (/cbc [type] [radius])", loc);
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
}
