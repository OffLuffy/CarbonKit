package net.teamcarbon.carbonkit.events.triviaEvents;

import net.teamcarbon.carbonkit.CarbonKit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called with someone correctly answers the active Question. This event will trigger if the user was blacklisted
 *  for this answer, but not if the user was previously blacklisted and attempted to answer. If the user was
 *  blacklisted for this answer, a TriviaUserBlacklistedEvent triggers immediately after.
 */
@SuppressWarnings("UnusedDeclaration")
public class TriviaAnswerEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private Player player;
	private String answer;
	private boolean blacklisted;
	public TriviaAnswerEvent(Player p, String a, boolean b) {
		CarbonKit.inst().logDebug(getEventName() + " called");
		player = p;
		answer = a;
		blacklisted = b;
	}
	public Player getPlayer() { return player; }
	public String getAnswer() { return answer; }
	public boolean isBlacklisted() { return blacklisted; }
	public boolean isCancelled() { return cancelled; }
	public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
