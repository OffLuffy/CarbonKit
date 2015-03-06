package net.teamcarbon.carbonkit.events.triviaEvents;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.TriviaRound;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This even is called as a TriviaRound is ended (not cancelled) - Maps and lists are still in tact at this point.
 */
@SuppressWarnings("UnusedDeclaration")
public class TriviaEndEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private TriviaRound round;
	public TriviaEndEvent(TriviaRound round) {
		CarbonKit.log.debug(getEventName() + " called");
		this.round = round;
	}
	public TriviaRound getTriviaRound() { return round; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
