package net.teamcarbon.carbonkit.utils.votetypes;

import org.bukkit.OfflinePlayer;

@SuppressWarnings("UnusedDeclaration")
public abstract class TargetedVote extends Vote {
	/**
	 * Represents the types of TargetedVote types
	 */
	public enum TargetedVoteType {
		JAIL, BAN, KICK, MUTE;
		public String lname() { return name().toLowerCase(); }
	}
	OfflinePlayer target;
	TargetedVoteType tvtype;
	public TargetedVote(OfflinePlayer player, OfflinePlayer target, TargetedVoteType tvtype) {
		super(player, VoteType.TARGETED);
		this.target = target;
		this.tvtype = tvtype;
	}
	/**
	 * Fetches the target OfflinePlayer associated with this Vote
	 * @return Returns the OfflinePlayer targeted by the Vote
	 */
	public OfflinePlayer getTarget() { return target; }
	/**
	 * Fetches which TargetedVoteType this TargetedVote is
	 * @return Returns the TargetedVoteType, which can be JAIL, BAN, KICK, or MUTE
	 */
	public TargetedVoteType getTargetedVoteType() { return tvtype; }
}
