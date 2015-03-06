package net.teamcarbon.carbonkit.events.voteEvents;

import net.teamcarbon.carbonkit.CarbonKit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import net.teamcarbon.carbonkit.utils.votetypes.Vote;
import net.teamcarbon.carbonkit.utils.votetypes.Vote.VoteType;

@SuppressWarnings("UnusedDeclaration")
public class VoteFailEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Vote vote;
	private VoteType vtype;
	private boolean cancelled;
	public VoteFailEvent(Vote v) {
		CarbonKit.log.debug(getEventName() + " called");
		vote = v;
		vtype = v.getType();
	}
	public Vote getVote() { return vote; }
	public VoteType getVoteType() { return vtype; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
