package net.teamcarbon.carbonkit.events.essentialsEvents;

import net.teamcarbon.carbonkit.utils.CarbonPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class EssentialsTeleportEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private Player player;
	private Location origin, destination;

	public EssentialsTeleportEvent(Player pl, Location fromLoc, Location toLoc) {
		player = pl;
		destination = toLoc;
		origin = fromLoc;
	}

	public Player getPlayer() { return player; }
	public CarbonPlayer getCarbonPlayer() { return CarbonPlayer.getCarbonPlayer(player, false); }
	public Location getDestination() { return destination; }
	public Location getOrigin() { return origin; }
	public void setDestination(Location loc) { destination = loc; }
	public boolean isCancelled() { return cancelled; }
	public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
