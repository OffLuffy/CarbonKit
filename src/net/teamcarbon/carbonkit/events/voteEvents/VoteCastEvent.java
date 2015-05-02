package net.teamcarbon.carbonkit.events.voteEvents;

import net.teamcarbon.carbonkit.CarbonKit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import net.teamcarbon.carbonkit.utils.CarbonVote.Vote;

@SuppressWarnings("UnusedDeclaration")
public class VoteCastEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private Player player;
	private Vote vote;
	private boolean agree;
	public VoteCastEvent(Player player, Vote vote, boolean agree) {
		CarbonKit.log.debug(getEventName() + " called");
		this.player = player;
		this.vote = vote;
		this.agree = agree;
	}
	public Player getVoter() { return player; }
	public Vote getVote() { return vote; }
	public boolean agrees() { return agree; }
	public boolean isCancelled() { return cancelled; }
	public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
