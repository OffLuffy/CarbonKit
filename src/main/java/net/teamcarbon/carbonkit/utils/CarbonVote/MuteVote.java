package net.teamcarbon.carbonkit.utils.CarbonVote;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.modules.CarbonVoteModule;
import net.teamcarbon.carbonkit.utils.MiscUtils;
import net.teamcarbon.carbonkit.utils.Module;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("UnusedDeclaration")
public class MuteVote extends TargetedVote {
	public MuteVote(OfflinePlayer player, OfflinePlayer target) { super(player, target, TargetedVoteType.MUTE); }
	protected void votePass() {
		com.earth2me.essentials.Essentials ess = (com.earth2me.essentials.Essentials) MiscUtils.getPlugin("Essentials", true);
		if (ess != null) {
			com.earth2me.essentials.User eUser = ess.getUser(target.getUniqueId());
			eUser.setMuted(true);
			HashMap<String, String> rep = new HashMap<>();
			if (CarbonKit.inst.getConfig().getLong("CarbonVote." + getTargetedVoteType().lname() + "-time-seconds", 300) > 0L) {
				long dura = CarbonKit.inst.getConfig().getLong("CarbonVote." + getTargetedVoteType().lname() + "-time-seconds", 300) * 1000L;
				long current = System.currentTimeMillis();
				eUser.setMuteTimeout(current + dura);
				dura /= 1000L;
				long m = dura / 60, s = dura % 60;
				String time = (m > 0 ? m + " min" + (m != 1 ? "s" : "") : "") + (s > 0 ? s + " sec" + (s != 1 ? "s" : "") : "");
				rep.put("{DURATION}", time);
			} else {
				rep.put("{DURATION}", "");
			}
			rep.put("{TARGET}", target.getName());
			List<Player> plList = MiscUtils.quickList((Player) target);
			MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, plList, Module.getMsg("carbonvote", "mute-vote-passed", true, rep));
			if (target.isOnline()) ((Player) target).sendMessage(Module.getMsg("carbonvote", "mute-message", false, rep));
		}
	}
	protected void voteFail() {
		HashMap<String, String> rep = new HashMap<>();
		rep.put("{YESPERCENT}", String.format(Locale.ENGLISH, "%.2f", getAgreePercentage(true)));
		rep.put("{NOPERCENT}", String.format(Locale.ENGLISH, "%.2f", (100 - getAgreePercentage(true))));
		rep.put("{VOTETYPE}", "Mute");
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, MiscUtils.quickList((Player)target), Module.getMsg("carbonvote", "vote-failed", true, rep));
	}
	protected void broadcastStart() {
		HashMap<String, String> rep = new HashMap<>();
		rep.put("{VOTETYPE}", getTargetedVoteType().lname());
		rep.put("{VOTEREASON}", "to " + getTargetedVoteType().lname() + " " + target.getName());
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, Module.getMsg("carbonvote", "vote-started", true, rep));
	}
}
