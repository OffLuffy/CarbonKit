package net.teamcarbon.carbonkit.events.coreEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class PlayerRejoinEvent extends Event {
	private Player player;
	public PlayerRejoinEvent(Player player) {
		this.player = player;
	}
	public Player getPlayer() { return player; }
	private static final HandlerList handlers = new HandlerList();
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}