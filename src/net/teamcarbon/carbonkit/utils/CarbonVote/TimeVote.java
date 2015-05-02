package net.teamcarbon.carbonkit.utils.CarbonVote;

import net.teamcarbon.carbonkit.modules.CarbonVoteModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import net.teamcarbon.carbonlib.MiscUtils;

import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("UnusedDeclaration")
public class TimeVote extends Vote {
	public enum TimeTerm {
		DAWN(22812), DAY(2000), NOON(6000), EVENING(10000), DUSK(13188), NIGHT(14000), MIDNIGHT(18000);
		long ticks;
		TimeTerm(long ticks) { this.ticks = ticks; }
		public long getTicks() { return ticks; }
	}
	private long time;
	private World world;
	public TimeVote(OfflinePlayer player, long time, World world) {
		super(player, VoteType.TIME);
		this.time = time;
		this.world = world;
	}
	public TimeVote(OfflinePlayer player, TimeTerm time, World world) {
		super(player, VoteType.TIME);
		this.time = time.getTicks();
		this.world = world;
	}
	/**
	 * Fetches the time associated with this Vote
	 * @return Returns a long value representing the time in ticks to set the time to
	 */
	public long getTime() { return time; }
	/**
	 * Fetches the World associated with this Vote
	 * @return Returns the World to set the time in
	 */
	public World getWorld() { return world; }
	protected void votePass() {
		world.setTime(time);
		HashMap<String, String> rep = new HashMap<String, String>();
		rep.put("{TIME}", CarbonVoteModule.stringifyTime(time));
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, MiscUtils.massReplace(CustomMessage.CV_TIME_VOTE_PASSED.pre(), rep));
	}
	protected void voteFail() {
		HashMap<String, String> rep = new HashMap<String, String>();
		rep.put("{YESPERCENT}", String.format(Locale.ENGLISH, "%.2f", getAgreePercentage(true)));
		rep.put("{NOPERCENT}", String.format(Locale.ENGLISH, "%.2f", (100 - getAgreePercentage(true))));
		rep.put("{VOTETYPE}", "Time");
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, MiscUtils.massReplace(CustomMessage.CV_VOTE_FAILED.pre(),rep));
	}
	protected void broadcastStart() {
		HashMap<String, String> rep = new HashMap<String, String>();
		rep.put("{VOTETYPE}", "time");
		rep.put("{VOTEREASON}", "for " + CarbonVoteModule.stringifyTime(time));
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, MiscUtils.massReplace(CustomMessage.CV_VOTE_STARTED.pre(), rep));
	}
}
