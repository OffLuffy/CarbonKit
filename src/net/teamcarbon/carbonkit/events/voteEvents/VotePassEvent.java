package net.teamcarbon.carbonkit.events.voteEvents;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.votetypes.Vote;
import net.teamcarbon.carbonkit.utils.votetypes.Vote.VoteType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("UnusedDeclaration")
public class VotePassEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private Vote vote;
	private VoteType vtype;
	private boolean cancelled;
	public VotePassEvent(Vote v) {
		CarbonKit.log.debug(getEventName() + " called");
		vote = v;
		vtype = v.getType();
	}
	public Vote getVote() { return vote; }
	public VoteType getVoteType() { return vtype; }
	public boolean isCancelled() { return cancelled; }
	public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
