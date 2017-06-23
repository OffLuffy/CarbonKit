package net.teamcarbon.carbonkit.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public final class LocUtils {

	public static HashMap<Player, Location> lastLoc = new HashMap<>();

	/**
	 * Removes all entries from the cache of player locations used to simplify movement listeners
	 */
	public static void resetLastLocs() { if (lastLoc == null) lastLoc = new HashMap<>(); else lastLoc.clear(); }

	/**
	 * Indicates if the point specified is within the boundaries indicated with the lesser and greater corners
	 * @param point The Location to check if being inside the boundaries
	 * @param lesserCorner The lower boundary Location
	 * @param greaterCorner The greater boundary Location
	 * @return Returns true if the point is within the specified boundary Locations
	 */
	public static boolean isInside(Location point, Location lesserCorner, Location greaterCorner) {
		return (point.getWorld().equals(lesserCorner.getWorld())
				&& point.getX() > lesserCorner.getX() && point.getX() < greaterCorner.getX()
				&& point.getY() > lesserCorner.getY() && point.getY() < greaterCorner.getY()
				&& point.getZ() > lesserCorner.getZ() && point.getZ() < greaterCorner.getZ());
	}

	/**
	 * Checks to see if the two Locations specified have the same XYZ coordinates (including decimals)
	 * @param l1 The first Location to compare against the second
	 * @param l2 The second Location to compare against the first
	 * @return Returns true if the World, x, y, and z locations are exactly the same between both Locations
	 */
	public static boolean isSameLoc(Location l1, Location l2) {
		return l1.getWorld().equals(l2.getWorld())
				&& l1.getX() == l2.getX() && l1.getY() == l2.getY() && l1.getZ() == l2.getZ();
	}

	/**
	 * Checks to see if the two Locations specified have the same XYZ block coordinates (truncates decimals)
	 * @param l1 The first Location to comapre against the second
	 * @param l2 The second Location to compare against the first
	 * @return Return strue if the World, x, y, and z block locations are the same
	 */
	public static boolean isSameBlockLoc(Location l1, Location l2) {
		return l1.getWorld().equals(l2.getWorld())
				&& l1.getBlockX() == l2.getBlockX()
				&& l1.getBlockY() == l2.getBlockY()
				&& l1.getBlockZ() == l2.getBlockZ();
	}

	/**
	 * Attempts to fetch a Location point within the boundaries specified
	 * @param lesserBounds The lower boundary
	 * @param greaterBounds The greater boundary
	 * @param playerLoc A Location (inside or outside the boundaries) to re-adjust to be within then boundaries
	 * @return Returns a Location that resides within the boundary Locations, or the same Location if it is already
	 */
	public static Location getInsideLocation(Location lesserBounds, Location greaterBounds, Location playerLoc) {
		Location loc = playerLoc.clone();
		if (playerLoc.getBlockX() > greaterBounds.getBlockX()) loc.setX(greaterBounds.getBlockX() - 1);
		if (playerLoc.getBlockX() < lesserBounds.getBlockX()) loc.setX(lesserBounds.getBlockX() + 1);
		if (playerLoc.getBlockY() > greaterBounds.getBlockY()) loc.setY(greaterBounds.getBlockY() - 1);
		if (playerLoc.getBlockY() < lesserBounds.getBlockY()) loc.setY(lesserBounds.getBlockY() + 1);
		if (playerLoc.getBlockZ() > greaterBounds.getBlockZ()) loc.setZ(greaterBounds.getBlockZ() - 1);
		if (playerLoc.getBlockZ() < lesserBounds.getBlockZ()) loc.setZ(lesserBounds.getBlockZ() + 1);
		return loc;
	}

	/**
	 * Formats a Location in a String format for easier storage. Can be parsed with the fromStr() method
	 * @param loc The Location to format as String
	 * @param blockCoords Whether or not to treat the Location like Block coordinates (truncates decimals)
	 * @return Returns a String of the given Location. Values that fail to parse are replaced with defaults.
	 * This should only return null if the Location fails to format (there are no worlds on the server).
	 */
	public static String toStr(Location loc, boolean blockCoords) {
		World w = loc!=null ? loc.getWorld() : null;
		if (w == null) { try { w = Bukkit.getWorlds().get(0); } catch (Exception ignore) {} }
		if (w == null) { return null; }
		double x = 0, y = 64, z = 0;
		float yaw = 0, pitch = 0;
		if (loc != null) {
			x = blockCoords?loc.getBlockX():loc.getX();
			y = blockCoords?loc.getBlockY():loc.getY();
			z = blockCoords?loc.getBlockZ():loc.getZ();
			yaw = loc.getYaw();
			pitch = loc.getPitch();
		}
		return w.getName() + "," + (blockCoords?((int)x + "," + (int)y + "," + (int)z):(x + "," + y + "," + z)) + "," + yaw + "," + pitch;
	}

	/**
	 * Attempts to parse a Location from a String following the format from the toStr() method
	 * @param locStr The String representing the Location
	 * @return Returns a Location. If values fail to parse, they are replaced with default values,
	 * or null if it fails to parse (which should only happen if there's somehow no worlds)
	 */
	public static Location fromStr(String locStr) {
		if (locStr == null || locStr.isEmpty()) return null;
		String[] s = locStr.split(",");
		World w = Bukkit.getWorld(s[0]);
		if (w == null) { try { w = Bukkit.getWorlds().get(0); } catch (Exception ignore) {} }
		if (w == null) { return null; }
		double x = 0, y = 64, z = 0;
		float yaw = 0, pitch = 0;
		try { x = Double.parseDouble(s[1]); } catch (Exception ignore) {}
		try { y = Double.parseDouble(s[2]); } catch (Exception ignore) {}
		try { z = Double.parseDouble(s[3]); } catch (Exception ignore) {}
		try { yaw = Float.parseFloat(s[4]); } catch (Exception ignore) {}
		try { pitch = Float.parseFloat(s[5]); } catch (Exception ignore) {}
		return new Location(w, x, y, z, yaw, pitch);
	}

	/**
	 * Calculates the distance between the two locations. This calculation ignores World differences, uses only x,y,z.
	 * @param l1 The first Location
	 * @param l2 The second Location
	 * @return Returns a double indicating the distance between the two points specified, or -1 if either Location is null
	 */
	public static double distance(Location l1, Location l2) {
		if (l1 == null || l2 == null) return -1;
		double dx = l1.getX() - l2.getX();
		double dy = l1.getY() - l2.getY();
		double dz = l1.getZ() - l2.getZ();
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}

	/**
	 * Converts the Player's angle to a string. i.e., North, South, etc.
	 * @param pl The Player to base the angle on
	 * @param acro Whether or not to return an acronym instead (NE instead of North-East, etc)
	 * @param compDir Whether or not to only show compass directions (false will also return Up and Down or U and D acronyms)
	 * @param secondary Whether or not to include secondary inter-cardinal directions (NNE, SSW, WSW, etc)
	 * @return Returns the string value of the angle in terms of cardinal directions
	 */
	public static String getCardinalDir(Player pl, boolean acro, boolean compDir, boolean secondary) {
		String[] prim = {"South", "South-West", "West", "North-West", "North", "North-East", "East", "South-East"};
		String[] scnd = {"South", "South South-West", "South-West", "West South-West", "West", "West North-West",
				"North-West", "North North-West", "North", "North North-East", "North-East", "East North-East",
				"East", "East South-East", "South-East", "South South-East"};
		double rot = pl.getLocation().getYaw(), pit = pl.getLocation().getPitch(), rotInt = secondary ? 22.5 : 45;
		if (rot < 0) rot += 360;
		if (!compDir && (pit > 45 || pit < -45)) { return (pit < -45) ? (acro ? "U" : "Up") : (acro ? "D" : "Down"); }
		String dir = secondary ? scnd[(int)((rot+(rotInt/2))/rotInt)%16] : prim[(int)((rot+(rotInt/2))/rotInt)%8];
		return acro ? dir.replaceAll("orth|ast|outh|est| |-", "") : dir; // .replaceAll acronyms the direction
	}

	/**
	 * Attempts to find a solid block under the given Location (not including the block at the Location)
	 * @param loc The Location to look under for a solid block;
	 * @return Returns a Block if a solid block is found under the Location, null otherwise
	 */
	public static Block findFloor(Location loc) {
		if (loc == null || loc.getBlock() == null) return null;
		Block b = loc.getBlock().getRelative(BlockFace.DOWN);
		for (int y = loc.getBlockY(); y > 5 && !MiscUtils.isSupporting(b.getType()); y--) {
			b = b.getRelative(BlockFace.DOWN);
			if (MiscUtils.isSupporting(b.getType())) return b;
		}
		return null;
	}

	/**
	 * Checks if the given Material is found within a radius of the Player
	 * @param pl = The Player to search around
	 * @param rad The radius around the Player to search
	 * @param mats The Materials to look for
	 * @return Returns true if any of the Materials are found, false otherwise.
	 */
	public static boolean plrRadiusCheck(Player pl, int rad, Material... mats) {
		Location loc = centerLoc(pl.getLocation());
		for (int x = loc.getBlockX()-rad; x <= loc.getBlockX()+rad; x++) {
			for (int z = loc.getBlockZ()-rad; z <= loc.getBlockZ()+rad; z++) {
				for (int y = loc.getBlockY()-rad; y <= loc.getBlockY()+rad+1; y++) {
					Block b = pl.getWorld().getBlockAt(x, y, z);
					if (b != null && MiscUtils.objEq(b.getType(), mats)) return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the given Material is found within a radius of the Location
	 * @param loc = The Location to search around
	 * @param rad The radius around the Location to search
	 * @param mats The Materials to look for
	 * @return Returns true if any of the Materials are found, false otherwise.
	 */
	public static boolean locRadiusCheck(Location loc, int rad, Material... mats) {
		loc = centerLoc(loc);
		for (int x = loc.getBlockX()-rad; x <= loc.getBlockX()+rad; x++) {
			for (int z = loc.getBlockZ()-rad; z <= loc.getBlockZ()+rad; z++) {
				for (int y = loc.getBlockY()-rad; y <= loc.getBlockY()+rad+1; y++) {
					Block b = loc.getWorld().getBlockAt(x, y, z);
					if (b != null && MiscUtils.objEq(b.getType(), mats)) return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the given EntityType is found within a radius of the Player
	 * @param pl The Player to search around
	 * @param radius The radius around the Player to search
	 * @param types The EntityTypes to look for
	 * @return Returns true if any of the EntityTypes are found, false otherwise
	 */
	public static boolean plrRadiusCheck(Player pl, int radius, EntityType... types) {
		for (Entity ent : pl.getNearbyEntities(radius, radius, radius))
			if (MiscUtils.objEq(ent.getType(), types)) return true;
		return false;
	}

	/**
	 * Checks if the given EntityType is found within a radius of the Location
	 * @param loc The Location to search around
	 * @param radius The radius around the Location to search
	 * @param types The EntityTypes to look for
	 * @return Returns true if any of the EntityTypes are found, false otherwise
	 */
	public static boolean locRadiusCheck(Location loc, int radius, EntityType ... types) {
		loc = centerLoc(loc);
		for (Entity ent : getNearbyEntities(loc, radius))
			if (MiscUtils.objEq(ent.getType(), types)) return true;
		return false;
	}

	/**
	 * Fetches entities within a radius of a given Location (fetches entities per chunk rather than world)
	 * @param l The Location to search around
	 * @param radius The radius around the Location to search
	 * @return Returns an Entity array containing all Entities found
	 */
	public static Entity[]  getNearbyEntities(Location l, int radius){
		int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16))/16;
		List<Entity> radiusEntities = new ArrayList<>();
		for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++){
			for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++){
				int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
				for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()){
					Location el = e.getLocation();
					if (el.distance(l) <= radius && el.getBlock() != l.getBlock()) radiusEntities.add(e);
				}
			}
		}
		return radiusEntities.toArray(new Entity[radiusEntities.size()]);
	}

	/**
	 * Converts the given Location to another centered on the block coordinates
	 * @param loc The Location to center
	 * @return Returns a new Location with centered coordinates
	 */
	public static Location centerLoc(Location loc) {
		return new Location(loc.getWorld(), loc.getBlockX()+0.5, loc.getBlockY(), loc.getBlockZ()+0.5);
	}

	/**
	 * Fetches the Location above the given Location
	 * @param loc The Location to look above from
	 * @param above How many blocks above to fetch
	 * @return Returns the Location requested
	 */
	public static Location getLocAbove(Location loc, int above) {
		Location nl = loc.clone();
		nl.setY(loc.getBlockY()+above);
		return nl;
	}

	/**
	 * Fetches the Location above the given Block
	 * @param block The Block to look above from
	 * @param above How many blocks above to fetch
	 * @return Returns the Location requested
	 */
	public static Location getLocAbove(Block block, int above) { return getLocAbove(block.getLocation(), above); }

	/**
	 * Checks the equality of Locations within a given distance
	 * @param l1 The first Location to compare
	 * @param l2 The second Location to compare
	 * @param graceDistance The max amount of distance between the points before returning false
	 * @return Returns true if the Locations are equal or within the grace distance of each other, false otherwise
	 */
	public static boolean softEquals(Location l1, Location l2, float graceDistance) {
		return l1.equals(l2) || l1.getWorld().equals(l2.getWorld()) && distance(l1, l2) <= graceDistance;
	}
}
