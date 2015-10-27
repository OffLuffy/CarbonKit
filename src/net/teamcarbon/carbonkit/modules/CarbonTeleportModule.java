package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.Misc.LocUtils;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;

// TODO List:
// - Add Warps
// - Add Homes
// - Add TP Functions
// - - Requests
// - - Overrides
// - - Cooldowns
// - - Jump/Thru

@SuppressWarnings("UnusedDeclaration")
public class CarbonTeleportModule extends Module {

	private static HashMap<String, Location> warps = new HashMap<String, Location>();
	private static HashMap<Player, HashMap<String, Location>> homes = new HashMap<Player, HashMap<String, Location>>();

	public CarbonTeleportModule() throws DuplicateModuleException { super("CarbonTeleport", "teleport", "ctp", "tp", "warp", "warps", "home", "homes"); }
	public void initModule() {
		registerListeners();
	}
	public void disableModule() {
		unregisterListeners();
	}
	public void reloadModule() {
		disableModule();
		initModule();
	}
	protected boolean needsListeners() { return true; }
	public boolean hasAllDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler(ignoreCancelled = true)
	public void playerTeleport(PlayerTeleportEvent e) {
		boolean fly = e.getPlayer().isFlying();
		boolean vln = MiscUtils.objEq(e.getPlayer().getGameMode(), GameMode.SURVIVAL, GameMode.ADVENTURE);
		boolean spc = MiscUtils.objEq(e.getPlayer().getGameMode(), GameMode.SPECTATOR);

		// TO/DO Remove debug line:
		//fly = false; vln = true; spc = false;

		if (!isSafeTeleportLoc(e.getTo(), fly, vln, spc)) {
			e.setCancelled(true);
			if (LocUtils.findFloor(e.getTo()) == null)
				e.getPlayer().sendMessage(Clr.RED + "A floor couldn't be found under the teleport");
			else if (!spc && !hasRoom(e.getTo()))
				e.getPlayer().sendMessage(Clr.RED + "There's not enough space at the teleport");
			else if (vln && nearExplosive(e.getTo(), 6))
				e.getPlayer().sendMessage(Clr.RED + "There are explosives too close to the teleport!");
			else if (vln && nearFlammable(e.getTo(), 2))
				e.getPlayer().sendMessage(Clr.RED + "There is lava or fire too close to the teleport!");
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	/**
	 * Checks if the specified location is safe to teleport to (has ground, no lava, etc)
	 * @param loc The Location to check
	 * @param flying Whether or not the Player is flying
	 * @param vulnerable Whether or not the Player is vulnerable to fire damage or explosion damage
	 * @param specating Whether or not the Player's {@link GameMode} is set to {@link GameMode#SPECTATOR}
	 * @return Returns true if the Location is safe to teleport to, false otherwise
	 */
	private Location getSafeTeleportLoc(Location loc, boolean flying, boolean vulnerable, boolean specating) {
		BlockFace[] bf = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		int stepDir = 0, steps = 1;
		boolean incSteps = false, foundSafe = false;
		for (int counter = 0; counter < 30; counter++) {
			for (int step = 0; step < steps && !foundSafe; step++) {
				if (!isSafeTeleportLoc(loc, flying, vulnerable, specating)) {
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
	 * Checks that a teleport Location is safe for the Player, considering the Player's GameMode.
	 * This checks if there's enough space for the Player to fit (if not in Spectate mode), if there
	 * is a valid floor at the location (if not flying), and if there is fire/explosives nearby (if not in Spectate or Creative)
	 * @param loc The Location the Player is teleporting to
	 * @param flying Whether or not the Player is flying
	 * @param vulnerable Whether or not the Player is vulnerable to fire damage or explosion damage
	 * @param spectating Whether or not the Player's {@link GameMode} is set to {@link GameMode#SPECTATOR}
	 * @return Returns true if the location is safe to teleport to, false otherwise.
	 */
	private boolean isSafeTeleportLoc(Location loc, boolean flying, boolean vulnerable, boolean spectating) {
		// TODO Check for god mode when implemented
		return ((!spectating && hasRoom(loc)) && (!flying && LocUtils.findFloor(loc) != null) && (!vulnerable || (!nearFlammable(loc, 2) && !nearExplosive(loc, 6))));
	}

	/**
	 * Checks if there is at least 2 blocks of non-suffocating materials at the given Location and the block above.
	 * @param loc The Location to check
	 * @return Returns true if a player can fit in the specified Location, false otherwise
	 */
	private boolean hasRoom(Location loc) {
		return MiscUtils.isHollow(loc.getBlock().getType()) && MiscUtils.isHollow(loc.getBlock().getRelative(BlockFace.UP).getType());
	}

	/**
	 * Checks if the given Location is within the specified radius of lava or fire
	 * @param loc The Location to check
	 * @param radius The radius around the Location to check
	 * @return Returns true if lava or fire is found, false otherwise
	 */
	private boolean nearFlammable(Location loc, int radius) {
		return LocUtils.locRadiusCheck(loc, 1, Material.FIRE, Material.STATIONARY_LAVA, Material.LAVA);
	}

	/**
	 * Checks if the given Location is within the specified radius of explosive entities
	 * @param loc The Location to check
	 * @param radius The radius around the Location to check
	 * @return Returns true if primted TNT, ender crystals, creepers, or fireballs are found, false otherwise
	 */
	private boolean nearExplosive(Location loc, int radius) {
		return LocUtils.locRadiusCheck(loc, radius, EntityType.PRIMED_TNT, EntityType.ENDER_CRYSTAL, EntityType.CREEPER, EntityType.FIREBALL);
	}
}
