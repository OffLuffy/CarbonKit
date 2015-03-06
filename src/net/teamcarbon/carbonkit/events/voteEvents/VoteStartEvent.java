package net.teamcarbon.carbonkit.events.voteEvents;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.votetypes.Vote;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("UnusedDeclaration")
public class VoteStartEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private Player player;
	private Vote vote;
	public VoteStartEvent(Player player, Vote vote) {
		CarbonKit.log.debug(getEventName() + " called");
		this.player = player;
		this.vote = vote;
	}
	public Player getStarter() { return player; }
	public Vote getVote() { return vote; }
	public boolean isCancelled() { return cancelled; }
	public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
