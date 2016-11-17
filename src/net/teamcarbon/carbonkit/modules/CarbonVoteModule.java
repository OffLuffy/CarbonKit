package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.utils.CarbonVote.TimeVote;
import net.teamcarbon.carbonlib.Misc.NumUtils;
import net.teamcarbon.carbonlib.Misc.TypeUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.CarbonVote.CarbonVoteCommand;
import net.teamcarbon.carbonkit.events.voteEvents.VoteCastEvent;
import net.teamcarbon.carbonkit.events.voteEvents.VoteFailEvent;
import net.teamcarbon.carbonkit.events.voteEvents.VoteStartEvent;
import net.teamcarbon.carbonkit.events.voteEvents.VotePassEvent;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.CarbonVote.TargetedVote.TargetedVoteType;
import net.teamcarbon.carbonkit.utils.CarbonVote.TimeVote.TimeTerm;
import net.teamcarbon.carbonkit.utils.CarbonVote.Vote;
import net.teamcarbon.carbonkit.utils.CarbonVote.Vote.VoteType;
import net.teamcarbon.carbonlib.Misc.MiscUtils;

@SuppressWarnings("UnusedDeclaration")
public class CarbonVoteModule extends Module {
	public static CarbonVoteModule inst;
	public CarbonVoteModule() throws DuplicateModuleException {
		super("CarbonVote", "cvote");
		addRequires("Essentials");
	}
	private static Vote activeVote;
	public static final String VMSG_PERM = "receive-messages";
	public void initModule() {
		inst = this;
		addCmd(new CarbonVoteCommand(this));
		endVote();
	}
	public void disableModule() {
		if (activeVote != null)
			if (Bukkit.getScheduler().isCurrentlyRunning(activeVote.getTaskID()) || Bukkit.getScheduler().isQueued(activeVote.getTaskID()))
				Bukkit.getScheduler().cancelTask(activeVote.getTaskID());
	}
	public void reloadModule() {
		disableModule();
		CarbonKit.inst().reloadConf();
		CarbonKit.reloadConfig(ConfType.DATA);
		initModule();
	}
	protected boolean needsListeners() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler
	public void voteStart(VoteStartEvent e) {
		if (!isEnabled()) return;
		CarbonKit.inst().logDebug(((e.isCancelled()) ? "[Cancelled] " : "") + "Vote started. Type: "
				+ e.getVote().getType().lname() + ", started by " + e.getStarter().getName());
		if (e.isCancelled()) {
			if (Bukkit.getOnlinePlayers().size() == 1) {

			}
		}
	}
	@EventHandler
	public void votePass(VotePassEvent e) {
		if (!isEnabled()) return;
		CarbonKit.inst().logDebug(((e.isCancelled())?"[Cancelled] ":"") + "Vote passed. Type: "
				+ e.getVote().getType().lname());
		if (e.isCancelled()) {

		}
	}
	@EventHandler
	public void voteFail(VoteFailEvent e) {
		if (!isEnabled()) return;
		CarbonKit.inst().logDebug("Vote failed. Type: " + e.getVote().getType().lname());
	}
	@EventHandler
	public void voteCast(VoteCastEvent e) {
		if (!isEnabled()) return;
		CarbonKit.inst().logDebug(((e.isCancelled())?"[Cancelled] ":"") + "Vote cast by " + e.getVoter().getName() +
				((e.agrees())?"in favor of":"against") + " the pending " + e.getVote().getType().lname() + " vote");
		if (e.isCancelled()) {

		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	/**
	 * Starts a new vote
	 * @param v The vote to start
	 */
	public static void startVote(Vote v) {
		if (activeVote != null)
			endVote();
		activeVote = v;
		v.start();
		String vt = v.getTypeName();
		CarbonKit.getConfig(ConfType.DATA).set(inst.getName() + ".last-" + vt + "-vote", System.currentTimeMillis());
		CarbonKit.saveConfig(ConfType.DATA);
	}
	/**
	 * Resets the cached vote object to null
	 */
	public static void endVote() {
		if (activeVote != null) {
			String vt = getActiveVote().getTypeName();
			CarbonKit.getConfig(ConfType.DATA).set(inst.getName() + ".last-" + vt + "-vote", System.currentTimeMillis() / 1000L);
			CarbonKit.saveConfig(ConfType.DATA);
			activeVote = null;
		}
	}
	/**
	 * Returns the cached vote object
	 * @return Returns the vote object if a vote is ongoing, null otherwise
	 */
	public static Vote getActiveVote() { return activeVote; }
	/**
	 * Indicates whether or not a vote is ongoing
	 * @return Returns true if a vote is ongoing, false otherwise
	 */
	public static boolean isVoteOngoing() { return activeVote!=null; }
	/**
	 * Indicates whether or not a VoteType is enabled
	 * @param vt The VoteType to check
	 * @return Returns true if the VoteType is enabled, false otherwise
	 */
	public static boolean isVoteTypeEnabled(VoteType vt) { return inst.getConfig().getBoolean("enabled-vote-types." + vt.lname(), false); }
	/**
	 * Indicates whether or not a TargetedVoteType is enabled
	 * @param vt The TargetedVoteType to check
	 * @return Returns true if the TargetedVoteType is enabled, false otherwise
	 */
	public static boolean isVoteTypeEnabled(TargetedVoteType vt) { return inst.getConfig().getBoolean("enabled-vote-types." + vt.lname(), false); }
	/**
	 * Indicates whether the VoteType has enough players online to start
	 * @param vt The VoteType to check
	 * @return Returns true if the VoteType has enough players to start, false otherwise
	 */
	public static boolean hasEnoughPlayers(VoteType vt) {
		int num = 5;
		if (vt.equals(VoteType.WEATHER))
			num = inst.getConfig().getInt("min-players.weather", 4);
		else if (vt.equals(VoteType.TIME))
			num = inst.getConfig().getInt("min-players.time", 4);
		else if (vt.equals(VoteType.TRIVIA))
			num = inst.getConfig().getInt("min-players.trivia", 6);
		return Bukkit.getOnlinePlayers().size() >= num;
	}
	/**
	 * Indicates whether the TargetedVoteType has enough players online to start
	 * @param vt The TargetedVoteType to check
	 * @return Returns true if the TargetedVoteType has enough players to start, false otherwise
	 */
	public static boolean hasEnoughPlayers(TargetedVoteType vt) {
		int num = 5;
		if (vt.equals(TargetedVoteType.BAN))
			num = inst.getConfig().getInt("min-players.ban", 5);
		else if (vt.equals(TargetedVoteType.KICK))
			num = inst.getConfig().getInt("min-players.kick", 5);
		else if (vt.equals(TargetedVoteType.MUTE))
			num = inst.getConfig().getInt("min-players.mute", 5);
		else if (vt.equals(TargetedVoteType.JAIL))
			num = inst.getConfig().getInt("min-players.jail", 5);
		return Bukkit.getOnlinePlayers().size() >= num;
	}
	/**
	 * Indicates how many more players are needed for a VoteType to start
	 * @param vt The VoteType to check
	 * @return Returns the number of additional players needed to start the VoteType, or 0 if no more are needed
	 */
	public static int additionalNeeded(VoteType vt) {
		if (hasEnoughPlayers(vt)) return 0;
		int needed = 5;
		if (vt.equals(VoteType.WEATHER))
			needed = inst.getConfig().getInt("min-players.weather", 4);
		else if (vt.equals(VoteType.TIME))
			needed = inst.getConfig().getInt("min-players.time", 4);
		else if (vt.equals(VoteType.TRIVIA))
			needed = inst.getConfig().getInt("min-players.trivia", 6);
		return needed - Bukkit.getOnlinePlayers().size();
	}
	/**
	 * Indicates how many more players are needed for a TargetedVoteType to start
	 * @param vt The TargetedVoteType to check
	 * @return Returns the number of additional players needed to start the TargetedVoteType, or 0 if no more are needed
	 */
	public static int additionalNeeded(TargetedVoteType vt) {
		if (hasEnoughPlayers(vt)) return 0;
		int needed = 5;
		if (vt.equals(TargetedVoteType.BAN))
			needed = inst.getConfig().getInt("min-players.ban", 5);
		else if (vt.equals(TargetedVoteType.KICK))
			needed = inst.getConfig().getInt("min-players.kick", 5);
		else if (vt.equals(TargetedVoteType.MUTE))
			needed = inst.getConfig().getInt("min-players.mute", 5);
		else if (vt.equals(TargetedVoteType.JAIL))
			needed = inst.getConfig().getInt("min-players.jail", 5);
		return needed - Bukkit.getOnlinePlayers().size();
	}
	/**
	 * Translates the time from a long to a readable format
	 * @param time The time in long format
	 * @return Returns a textual representation of the time as a term (day,night,etc), or a 12-hour format
	 */
	public static String stringifyTime(long time) {
		time = Math.abs(time%24000);
		for (TimeTerm tt : TimeTerm.values())
			if (time == tt.getTicks())
				return MiscUtils.capFirst(tt.name(), true);
		int hours = (int)((time/1000L) + 6L), mins = (int)Math.floor(((double)(time%1000))/(1000.0/60.0));
		if (hours > 24) hours -= 24;
		boolean pm = hours >= 12;
		return ((hours>12)?hours-12:hours) + ":" + ((mins<10)?"0"+mins:mins) + (pm?"PM":"AM");
	}
	/**
	 * Attempts to fetch a VoteType from a String query
	 * @param vt The name or alias of a VoteType
	 * @return Returns the VoteType if found, null otherwise
	 */
	public static VoteType getVoteType(String vt) {
		for (VoteType vtype : Vote.VoteType.values())
			if (vtype.name().equalsIgnoreCase(vt))
				return vtype;
		if (MiscUtils.eq(vt, "w")) {
			return VoteType.WEATHER;
		} else if (MiscUtils.eq(vt, "t")) {
			return VoteType.TIME;
		} else if (MiscUtils.eq(vt, "ban", "b", "kick", "k", "mute", "m", "silence", "silent", "jail", "j")) {
			return VoteType.TARGETED;
		} else if (MiscUtils.eq(vt, "tr", "quiz", "q")) {
			return VoteType.TRIVIA;
		}
		return null;
	}
	/**
	 * Attempts to fetch a TargetedVoteType from a String query
	 * @param tvt The name or alias of a TargetedVoteType
	 * @return Returns the TargetedVoteType if found, null otherwise
	 */
	public static TargetedVoteType getTargetedVoteType(String tvt) {
		for (TargetedVoteType tvtype : TargetedVoteType.values())
			if (tvtype.name().equalsIgnoreCase(tvt))
				return tvtype;
		if (MiscUtils.eq(tvt, "b")) {
			return TargetedVoteType.BAN;
		} else if (MiscUtils.eq(tvt, "k")) {
			return TargetedVoteType.KICK;
		} else if (MiscUtils.eq(tvt, "m", "silence", "silent")) {
			return TargetedVoteType.MUTE;
		} else if (MiscUtils.eq(tvt, "j")) {
			return TargetedVoteType.JAIL;
		}
		return null;
	}
	/**
	 * Attempts to parse a string to a long in terms of time on the server
	 * @param time Time as a long, a 12 or 24 hour format, or TimeTerm alias
	 * @return Returns the long value of the time parsed or 0 if not parseable
	 * @see TimeVote.TimeTerm
	 */
	public static long parseTime(String time) {
		// 1000 ticks per mc hour, 16.6 ticks per mc minute (round to whole tick)
		time = time.toLowerCase();
		if (MiscUtils.eq(time, "d", "dawn", "sunrise")) {
			return TimeTerm.DAWN.getTicks();
		} else if (MiscUtils.eq(time, "day", "daytime", "daylight")) {
			return TimeTerm.DAY.getTicks();
		} else if (MiscUtils.eq(time, "noon", "noontime", "midday")) {
			return TimeTerm.NOON.getTicks();
		} else if (MiscUtils.eq(time, "afternoon", "evening")) {
			return TimeTerm.EVENING.getTicks();
		} else if (MiscUtils.eq(time, "dusk", "sunset")) {
			return TimeTerm.DUSK.getTicks();
		} else if (MiscUtils.eq(time, "n", "night", "nighttime", "dark")) {
			return TimeTerm.NIGHT.getTicks();
		} else if (MiscUtils.eq(time, "midnight")) {
			return TimeTerm.MIDNIGHT.getTicks();
		}
		if (time.contains(":") || time.contains("p") || time.contains("a")) {
			try {
				int hours, mins = 0;
				if (time.contains(":")) {
					hours = NumUtils.normalizeInt(Integer.parseInt(time.split(":")[0]), 0, 24);
					mins = NumUtils.normalizeInt(Integer.parseInt(time.split(":")[1].replace("pm", "").replace("am", "").replace("p", "").replace("a", "")), 0, 60);
				} else {
					hours = NumUtils.normalizeInt(Integer.parseInt(time.replace("pm", "").replace("am", "").replace("p", "").replace("a", "")), 0, 24);
				}
				if (hours < 13 && time.contains("p"))
					hours += 12;
				hours = (hours - 6 < 0) ? 24 + (hours - 6) : hours - 6;
				return (hours * 1000L) + Math.round((double) mins * 16.6);
			} catch (Exception ignore) {}
		}
		if (TypeUtils.isLong(time))
			return Long.parseLong(time);
		return 0L;
	}
}
