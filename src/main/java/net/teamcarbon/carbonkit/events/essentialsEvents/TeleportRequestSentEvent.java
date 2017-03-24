package net.teamcarbon.carbonkit.events.essentialsEvents;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class TeleportRequestSentEvent extends Event {
	private Location target;
	private Player requestedPlayer, requestingPlayer;

	public TeleportRequestSentEvent(Player to, Player from, Location loc) {
		requestedPlayer = to;
		target = loc;
	}

	public Location getLocation() { return target; }
	public Player getRequestedPlayer() { return requestedPlayer; }
	public Player getRequestingPlayer() { return requestingPlayer; }

	private static final HandlerList handlers = new HandlerList();
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
