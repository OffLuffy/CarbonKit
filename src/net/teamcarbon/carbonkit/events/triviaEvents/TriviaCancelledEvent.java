package net.teamcarbon.carbonkit.events.triviaEvents;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.CarbonTrivia.TriviaRound;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This even is called as a TriviaRound is cancelled - Maps and lists are still in tact at this point. Rewards
 *  for this TriviaRound are not distributed after this event is called like TriviaEndEvent
 */
@SuppressWarnings("UnusedDeclaration")
public class TriviaCancelledEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private TriviaRound round;
	public TriviaCancelledEvent(TriviaRound round) {
		CarbonKit.inst().logDebug(getEventName() + " called");
		this.round = round;
	}
	public TriviaRound getTriviaRound() { return round; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
