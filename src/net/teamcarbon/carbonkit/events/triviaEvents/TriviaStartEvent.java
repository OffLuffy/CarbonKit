package net.teamcarbon.carbonkit.events.triviaEvents;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.TriviaRound;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("UnusedDeclaration")
public class TriviaStartEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private TriviaRound round;
	public TriviaStartEvent(TriviaRound round) {
		CarbonKit.log.debug(getEventName() + " called");
		this.round = round;
	}
	public TriviaRound getTriviaRound() { return round; }
	public boolean isCancelled() { return cancelled; }
	public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
