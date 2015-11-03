package net.teamcarbon.carbonkit.utils.CarbonEssentials;

import org.bukkit.Location;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class TeleportRequest {

	public enum TeleportRequestType { TO_REQUESTING_PLAYER, TO_REQUESTED_PLAYER, TO_HOME }

	private Location target;
	private Player requestedPlayer, requestingPlayer;
	private TeleportRequestType tpType;

	/**
	 * Represents a teleport request
	 * @param to The Player the request was sent to
	 * @param from The Player whom sent the request
	 * @param loc The Location the teleport will send the player to
	 * @param type The type of the request
	 */
	public TeleportRequest(Player to, Player from, Location loc, TeleportRequestType type) {
		requestedPlayer = to;
		requestingPlayer = from;
		target = loc;
		tpType = type;
	}
}
