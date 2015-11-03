package net.teamcarbon.carbonkit.utils.CarbonEssentials;

import net.teamcarbon.carbonkit.utils.CarbonPlayer;
import net.teamcarbon.carbonlib.Misc.LocUtils;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class TeleportHelper {
	public enum TeleportCheckResult{
		/**
		 * Represents that the teleport location is safe
		 */
		SAFE,
		/**
		 * Represents that the teleport location is too close to fire
		 */
		FIRE,
		/**
		 * Represents that the teleport location is too close to explosives
		 */
		EXPLOSIVE,
		/**
		 * Represents that the teleport location is missing a floor
		 */
		FLOOR,
		/**
		 * Represents that the teleport location doesn't have enough room to fit
		 */
		ROOM
	}

	public static TeleportCheckResult checkTeleport(CarbonPlayer cp, Location loc) {
		if (cp == null) return null;
		GameMode gm = ((Player) cp.getPlayer()).getGameMode();
		if (gm != GameMode.SPECTATOR && !hasRoom(loc)) {
			return TeleportCheckResult.ROOM;
		} else if (!cp.hasFlyEnabled() || LocUtils.findFloor(loc) == null) {
			return TeleportCheckResult.FLOOR;
		} else if (gm != GameMode.SPECTATOR && gm != GameMode.CREATIVE && !cp.hasGodEnabled()) {
			if (nearFlammable(loc, 2)) {
				return TeleportCheckResult.FIRE;
			} else if (nearExplosive(loc, 6)) {
				return TeleportCheckResult.EXPLOSIVE;
			}
		}
		return TeleportCheckResult.SAFE;
	}

	/**
	 * Checks if the specified location is safe to teleport to (has ground, no lava, etc)
	 * @param cp The CarbonPlayer that corresponds to the Player teleporting
	 * @param loc The Location to start
	 * @return Returns a Location if a safe Location is found, null otherwise
	 */
	public static Location getSafeTeleportLoc(CarbonPlayer cp, Location loc) {
		BlockFace[] bf = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		int stepDir = 0, steps = 1;
		boolean incSteps = false, foundSafe = false;
		for (int counter = 0; counter < 30; counter++) {
			for (int step = 0; step < steps && !foundSafe; step++) {
				if (TeleportHelper.checkTeleport(cp, loc) != TeleportCheckResult.SAFE) {
					loc = loc.getBlock().getRelative(bf[stepDir]).getLocation();
				} else { foundSafe = true; break; }
			}
			if (foundSafe) { break;}
			if (stepDir >= bf.length - 1) stepDir = 0;
			else stepDir++;
			incSteps = !incSteps;
			if (incSteps) steps++;
		}
		return foundSafe ? loc : null;
	}

	/**
	 * Checks if there is at least 2 blocks of non-suffocating materials at the given Location and the block above.
	 * @param loc The Location to check
	 * @return Returns true if a player can fit in the specified Location, false otherwise
	 */
	private static boolean hasRoom(Location loc) {
		return MiscUtils.isPermeable(loc.getBlock().getType()) && MiscUtils.isPermeable(loc.getBlock().getRelative(BlockFace.UP).getType());
	}

	/**
	 * Checks if the given Location is within the specified radius of lava or fire
	 * @param loc The Location to check
	 * @param radius The radius around the Location to check
	 * @return Returns true if lava or fire is found, false otherwise
	 */
	private static boolean nearFlammable(Location loc, int radius) {
		return LocUtils.locRadiusCheck(loc, radius, Material.FIRE, Material.STATIONARY_LAVA, Material.LAVA);
	}

	/**
	 * Checks if the given Location is within the specified radius of explosive entities
	 * @param loc The Location to check
	 * @param radius The radius around the Location to check
	 * @return Returns true if primted TNT, ender crystals, creepers, or fireballs are found, false otherwise
	 */
	private static boolean nearExplosive(Location loc, int radius) {
		return LocUtils.locRadiusCheck(loc, radius, EntityType.PRIMED_TNT, EntityType.ENDER_CRYSTAL, EntityType.CREEPER, EntityType.FIREBALL);
	}
}
