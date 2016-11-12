package net.teamcarbon.carbonkit.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import net.teamcarbon.carbonlib.Misc.MiscUtils;

import static org.bukkit.entity.EntityType.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class EntHelper {
	public enum EntityGroup {
		HOSTILE("hostile", "hostiles", "hostilemob", "hostilemobs", "hmob", "hmobs", "aggressive", "h"),
		NEUTRAL("neutral", "neutrals", "neutralmob", "neutralmobs", "nmob", "nmobs", "n"),
		PASSIVE("passive", "passives", "passivemob", "passivemobs", "pmob", "pmobs", "peaceful", "p"),
		TAMED("tamed", "tame", "pets", "tamedmob", "tamedmobs", "tamemob", "tamemobs", "petmob", "petmobs", "t"),
		PLAYER("player", "players", "person", "people", "human", "humans"),
		DROP("drop", "drops", "item", "items", "itemdrop", "itemdrops", "grounditem", "grounditems", "d", "i");
		String[] aliases;
		EntityGroup(String ... aliases) { this.aliases = aliases; }
		public String[] getAliases() { return aliases; }
		public static EntityGroup getGroup(String query) {
			for (EntityGroup eg : EntityGroup.values())
				if (MiscUtils.eq(query, eg.getAliases()))
					return eg;
			return null;
		}
		public String lname() { return name().toLowerCase(); }
	}
	public static List<EntityType> getGroups(EntityGroup ... groups) {
		List<EntityType> eGroups = new ArrayList<>();
		for (EntityType t : EntityType.values()) {
			if (t != null) {
				for (EntityGroup g : groups) {
					if (g.equals(EntityGroup.HOSTILE) && isHostile(t)) eGroups.add(t);
					if (g.equals(EntityGroup.NEUTRAL) && isNeutral(t)) eGroups.add(t);
					if (g.equals(EntityGroup.PASSIVE) && isPassive(t)) eGroups.add(t);
					if (g.equals(EntityGroup.PLAYER) && isPlayer(t)) eGroups.add(t);
					if (g.equals(EntityGroup.DROP) && isDrop(t)) eGroups.add(t);
				}
			}
		}
		return eGroups;
	}
	public static boolean isHostile(EntityType type) { return (entEq(type, SPIDER, CAVE_SPIDER, SKELETON, GIANT, ZOMBIE, GHAST, ENDERMAN, SILVERFISH, BLAZE, MAGMA_CUBE, ENDER_DRAGON, WITHER, WITCH, CREEPER, SLIME, GUARDIAN, ENDERMITE)); }
	public static boolean isNeutral(EntityType type) { return (entEq(type, WOLF, IRON_GOLEM, PIG_ZOMBIE)); }
	public static boolean isPassive(EntityType type) { return (entEq(type, BAT, PIG, SHEEP, COW, CHICKEN, SQUID, MUSHROOM_COW, SNOWMAN, OCELOT, VILLAGER, HORSE, RABBIT)); }
	public static boolean isPlayer(EntityType type) { return type.equals(EntityType.PLAYER); }
	public static boolean isDrop(EntityType type) { return type.equals(EntityType.DROPPED_ITEM ); }
	public static boolean isHostile(LivingEntity ent) { return (isHostile(ent.getType())); }
	public static boolean isNeutral(LivingEntity ent) { return (isNeutral(ent.getType())); }
	public static boolean isPassive(LivingEntity ent) { return (isPassive(ent.getType())); }
	public static boolean isPlayer(LivingEntity ent) { return (isPlayer(ent.getType())); }
	public static boolean isTamed(LivingEntity ent) { return (ent instanceof Tameable && ((Tameable)ent).isTamed()); }
	@SuppressWarnings("deprecation")
	public static EntityType getType(String type) {
		if (EntityType.fromName(type) != null) return EntityType.fromName(type);
		else for (EntityType t : EntityType.values()) { if (MiscUtils.eq(type, t.getName())) return t; }
		if (MiscUtils.eq(type, "irongolem", "igolem", "golem"))
			return IRON_GOLEM;
		if (MiscUtils.eq(type, "ocelot", "ozelot", "cat", "kitten"))
			return OCELOT;
		if (MiscUtils.eq(type, "magmacube", "nethercube", "netherslime", "magmaslime", "lavacube", "lavaslime", "mcube", "mslime", "ncube", "nslime", "lcube", "lslime"))
			return MAGMA_CUBE;
		if (MiscUtils.eq(type, "wither", "witherboss", "wboss"))
			return WITHER;
		if (MiscUtils.eq(type, "mooshroom", "mushroomcow"))
			return MUSHROOM_COW;
		if (MiscUtils.eq(type, "snowgolem", "sgolem", "snowman"))
			return SNOWMAN;
		if (MiscUtils.eq(type, "villager", "npc", "human", "testificate"))
			return VILLAGER;
		if (MiscUtils.eq(type, "bunny"))
			return RABBIT;
		return null;
	}
	private static boolean entEq(EntityType ent, EntityType ... types) { for (EntityType type : types) if (ent.equals(type)) return true; return false; }
	public static List<LivingEntity> getTargets(Location l, long rad, List<EntityType> types) { return getTargets(l, rad, types.toArray(new EntityType[types.size()])); }
	public static List<LivingEntity> getTargets(Location l, long rad, EntityType ... types) {
		List<LivingEntity> entList = new ArrayList<>();
		List<LivingEntity> le = new ArrayList<>();
		for (Entity e : l.getWorld().getEntities()) {
			if (e instanceof LivingEntity) {
				LivingEntity lEnt = (LivingEntity)e;
				if (!le.contains(lEnt))
					for (EntityType type : types)
						if (lEnt.getType().equals(type))
							le.add(lEnt);
			}
		}
		Block b = l.getBlock();
		int bx = b.getX(),
				by = b.getY(),
				bz = b.getZ();
		Double	ex, ey, ez;
		for (LivingEntity e : le) {
			Location el = e.getLocation();
			ex = el.getX();
			ey = el.getY();
			ez = el.getZ();
			boolean inRange = ((bx-rad <= ex && bx+rad >= ex) && (by-rad <= ey && by+rad >= ey) && (bz-rad <= ez && bz+rad >= ez));
			if (inRange)
				entList.add(e);
		}
		return entList;
	}
}
