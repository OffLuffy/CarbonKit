package net.teamcarbon.carbonkit.utils.CarbonVote;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.events.voteEvents.VoteFailEvent;
import net.teamcarbon.carbonkit.modules.CarbonVoteModule;
import net.teamcarbon.carbonlib.Misc.NumUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import net.teamcarbon.carbonkit.events.voteEvents.VotePassEvent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public abstract class Vote {
	/**
	 * Represents the Vote types
	 */
	public enum VoteType {
		TARGETED, WEATHER, TIME, TRIVIA;
		public String lname() { return name().toLowerCase(); }
	}
	private OfflinePlayer player;
	private List<OfflinePlayer> agrees, disagrees;
	private VoteType type;
	private int voteTask;
	private String typeName;
	public Vote(OfflinePlayer player, VoteType type) {
		this.player = player;
		this.type = type;
		agrees = new ArrayList<>();
		disagrees = new ArrayList<>();
		agrees.add(player);

		typeName = getType().lname();
		if (getType() == VoteType.TARGETED) { typeName = ((TargetedVote)this).getTargetedVoteType().lname(); }
	}
	protected abstract void votePass();
	protected abstract void voteFail();
	protected abstract void broadcastStart();

	public void end() {
		if (Bukkit.getScheduler().isCurrentlyRunning(voteTask) || Bukkit.getScheduler().isQueued(voteTask))
			Bukkit.getScheduler().cancelTask(voteTask);
		if (CarbonVoteModule.getActiveVote() != null) {
			int maj = NumUtils.normalizeInt(CarbonKit.getDefConfig().getInt("CarbonVote.majority-percent." + typeName, 70), 0, 100);
			Vote v = CarbonVoteModule.getActiveVote();
			if (v.getAgreePercentage(true) > maj) {
				VotePassEvent vwe = new VotePassEvent(v);
				CarbonKit.pm().callEvent(vwe);
				if (!vwe.isCancelled()) {
					v.votePass();
					CarbonKit.log.debug("Vote passed, " + getAgreePercentage(true) + "% agreed ("
							+ getAgrees().size() + " y, " + getDisagrees().size() + " n, "
							+ (Bukkit.getOnlinePlayers().size() - (getAgrees().size() + getDisagrees().size())) + " x)");
				}
			} else {
				VoteFailEvent vfe = new VoteFailEvent(v);
				CarbonKit.pm().callEvent(vfe);
				v.voteFail();
				CarbonKit.log.debug("Vote failed, " + getAgreePercentage(true) + "% agreed ("
						+ getAgrees().size() + " y, " + getDisagrees().size() + " n, "
						+ (Bukkit.getOnlinePlayers().size() - (getAgrees().size() + getDisagrees().size())) + " x)");
			}
			CarbonVoteModule.endVote();
		}
	}
	public void cancel() {
		if (Bukkit.getScheduler().isCurrentlyRunning(voteTask) || Bukkit.getScheduler().isQueued(voteTask))
			Bukkit.getScheduler().cancelTask(voteTask);
		CarbonKit.log.debug("Vote cancelled");
	}
	public void start() {
		broadcastStart();
		long voteTimeout = (long)(NumUtils.normalizeInt(CarbonKit.getDefConfig().getInt("CarbonVote.vote-timeout-seconds", 20), 10, 300)*20);
		voteTask = Bukkit.getScheduler().scheduleSyncDelayedTask(CarbonKit.inst, new Runnable() {
			@Override
			public void run() { end(); }
		}, Bukkit.getOnlinePlayers().size() < 2 ? 1 : voteTimeout);
	}
	public int getTaskID() { return voteTask; }
	public OfflinePlayer getVoteStarter() { return player; }
	public VoteType getType() { return type; }
	public List<OfflinePlayer> getAgrees() { return agrees; }
	public List<OfflinePlayer> getDisagrees() { return disagrees; }
	public void addVoter(OfflinePlayer voter, boolean yn) {
		(yn?agrees:disagrees).add(voter);
		int maj = NumUtils.normalizeInt(CarbonKit.getDefConfig().getInt("CarbonVote.majority-percent." + typeName, 66), 0, 100);
		if (getAgreePercentage(false) > maj) end();
	}
	public float getAgreePercentage(boolean weighted) {
		if (agrees.isEmpty()) return 0f;
		float as = agrees.size(), ds = disagrees.size();
		float nonVoteWeight = (float)NumUtils.normalizeDouble(CarbonKit.getDefConfig().getDouble("CarbonVote.nonvote-weight-percent." + typeName,-30), -1, 1);
		if (nonVoteWeight == 0.0)
			return Vote.calc(as, ds, 0, 0);
		float nonVoters = 0;
		for (OfflinePlayer p : Bukkit.getOnlinePlayers())
			if (!agrees.contains(p) && !disagrees.contains(p))
				nonVoters++;
		// If checking un-weighted percent, treat non-voters as full no votes (prevent passing if it can pass if the rest vote no)
		return Vote.calc(as, ds, nonVoters, weighted ? nonVoteWeight : -100);
	}
	public String getTypeName() { return typeName; }

	/**
	 * Calculates the percentage of yes votes with weighted votes for non-voters
	 * @param y The number of yes votes
	 * @param n The number of no votes
	 * @param x The number of non-voters
	 * @param xw The percent weight of a non-vote (-100 for a no vote, 100 for a yes vote, 0 for no weight)
	 * @return Returns the percentage of yes votes
	 */
	public static float calc(float y, float n, float x, float xw) {
		if (xw == 0f) x = 0f; else x *= (Math.abs(xw) / 100f);
		return (y + ((xw > 0) ? x : 0)) / (y + n + x) * 100f;
	}
}
