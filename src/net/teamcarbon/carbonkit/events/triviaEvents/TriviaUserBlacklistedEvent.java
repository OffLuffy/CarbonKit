package net.teamcarbon.carbonkit.events.triviaEvents;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.TriviaRound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("UnusedDeclaration")
public class TriviaUserBlacklistedEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private Player player;
	public TriviaUserBlacklistedEvent(Player p) {
		CarbonKit.log.debug(getEventName() + " called");
		player = p;
	}
	public Player getPlayer() { return player; }
	public boolean isCancelled() { return cancelled; }
	public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
