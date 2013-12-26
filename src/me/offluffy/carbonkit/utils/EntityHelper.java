package me.offluffy.carbonkit.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

public class EntityHelper {
	public enum EntityGroup {
		HOSTILE, NEUTRAL, PASSIVE, PLAYER, LIVING;
	}
	
	public static List<EntityType> getGroups(EntityGroup ... groups) {
		List<EntityType> eGroups = new ArrayList<EntityType>();
		for (EntityType t : EntityType.values()) {
			if (t != null) {
				for (EntityGroup g : groups) {
					switch (g) {
					case HOSTILE:
						if (isHostile(t)) eGroups.add(t);
						break;
					case NEUTRAL:
						if (isNeutral(t)) eGroups.add(t);
						break;
					case PASSIVE:
						if (isPassive(t)) eGroups.add(t);
						break;
					case PLAYER:
						if (isPlayer(t)) eGroups.add(t);
						break;
					case LIVING:
						if (t.isAlive() && !isPlayer(t)) eGroups.add(t);
						break;
					}
				}
			}
		}
		return eGroups;
	}
	
	public static boolean isHostile(EntityType type) {
		return (entEq(type,
			EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SKELETON,
			EntityType.GIANT, EntityType.ZOMBIE, EntityType.GHAST,
			EntityType.ENDERMAN, EntityType.SILVERFISH, EntityType.BLAZE,
			EntityType.MAGMA_CUBE, EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.WITCH,
			EntityType.CREEPER, EntityType.SLIME
		));
	}
	
	public static boolean isNeutral(EntityType type) {
		return (entEq(type,
			EntityType.WOLF, EntityType.IRON_GOLEM, EntityType.PIG_ZOMBIE
		));
	}
	
	public static boolean isPassive(EntityType type) {
		return (entEq(type,
			EntityType.BAT, EntityType.PIG, EntityType.SHEEP, EntityType.COW,
			EntityType.CHICKEN, EntityType.SQUID, EntityType.MUSHROOM_COW,
			EntityType.SNOWMAN, EntityType.OCELOT, EntityType.VILLAGER, EntityType.HORSE
		));
	}
	
	public static boolean isPlayer(EntityType type) {
		return (type.equals(EntityType.PLAYER));
	}
	
	public static boolean isDrop(EntityType type) {
		return (entEq(type,
			EntityType.DROPPED_ITEM
		));
	}
	
	public static boolean isHostile(LivingEntity ent) {
		return (isHostile(ent.getType()));
	}
	
	public static boolean isNeutral(LivingEntity ent) {
		return (isNeutral(ent.getType()));
	}
	
	public static boolean isPassive(LivingEntity ent) {
		return (isPassive(ent.getType()));
	}
	
	public static boolean isPlayer(LivingEntity ent) {
		return (isPlayer(ent.getType()));
	}
	
	public static boolean isTamed(LivingEntity ent) {
		if (ent instanceof Tameable) {
			Tameable t = (Tameable)ent;
			if (t.isTamed())
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static EntityType getType(String type) {
		if (EntityType.fromName(type) != null)
			return EntityType.fromName(type);
		else {
			for (EntityType t : EntityType.values()) {
				if (type.equalsIgnoreCase(t.getName())) {
					return t;
				} else if (type.equalsIgnoreCase(t.toString())) {
					return t;
				} else if (type.equalsIgnoreCase(t.toString().replace("_", ""))) {
					return t;
				}
			}
		}

		switch(type.toLowerCase().replace("_","")) {
			case "irongolem":
			case "igolem":
			case "golem":
				return EntityType.IRON_GOLEM;
			case "ocelot":
			case "cat":
			case "kitten":
				return EntityType.OCELOT;
			case "magmacube":
			case "nethercube":
			case "netherslime":
			case "magmaslime":
			case "lavacube":
			case "lavaslime":
			case "mcube":
			case "mslime":
			case "ncube":
			case "nslime":
			case "lcube":
			case "lslime":
				return EntityType.MAGMA_CUBE;
			case "wither":
			case "witherboss":
				return EntityType.WITHER;
			case "mooshroom":
			case "mushroomcow":
				return EntityType.MUSHROOM_COW;
			case "snowgolem":
			case "sgolem":
			case "snowman":
				return EntityType.SNOWMAN;
			case "villager":
			case "npc":
			case "human":
			case "testificate":
				return EntityType.VILLAGER;
		}
		return null;
	}
	
	private static boolean entEq(EntityType ent, EntityType ... types) {
		for (EntityType type : types)
			if (ent.equals(type))
				return true;
		return false;
	}
	
	public static List<LivingEntity> getTargets(Location l, List<EntityType> types, long rad) {
		List<LivingEntity> entList = new ArrayList<LivingEntity>();
		List<LivingEntity> le = new ArrayList<LivingEntity>();
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
