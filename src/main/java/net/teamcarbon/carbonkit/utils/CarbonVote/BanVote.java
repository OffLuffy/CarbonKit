package net.teamcarbon.carbonkit.utils.CarbonVote;

import net.teamcarbon.carbonkit.modules.CarbonVoteModule;
import net.teamcarbon.carbonkit.utils.MiscUtils;
import net.teamcarbon.carbonkit.utils.Module;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.CarbonKit;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("UnusedDeclaration")
public class BanVote extends TargetedVote {
	public BanVote(OfflinePlayer player, OfflinePlayer target) { super(player, target, TargetedVoteType.BAN); }
	protected void votePass() {
		HashMap<String, String> rep = new HashMap<>();
		long ts = 0L;
		if (CarbonKit.inst.getConfig().getLong("CarbonVote."+getTargetedVoteType().lname()+"-time-seconds", 300) > 0L) {
			long dura = CarbonKit.inst.getConfig().getLong("CarbonVote."+getTargetedVoteType().lname()+"-time-seconds", 300) * 1000L;
			long current = System.currentTimeMillis();
			ts = current+dura;
			dura /= 1000L;
			long m = dura/60,s = dura%60;
			String time = (m>0?m+" min"+(m!=1?"s":""):"")+(s>0?s+" sec"+(s!=1?"s":""):"");
			rep.put("{DURATION}", time);
		} else {
			rep.put("{DURATION}", "");
		}
		rep.put("{TARGET}", target.getName());
		Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(target.getName(), "Vote Banned by " + getVoteStarter().getName(), ((ts>0L)?new Date(ts):null), "CarbonVote");
		List<Player> plList = MiscUtils.quickList((Player) target);
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, plList, Module.getMsg("carbonvote", "ban-vote-passed", true, rep));
		if (target.isOnline()) ((Player)target).kickPlayer(Module.getMsg("carbonvote", "ban-message", false, rep));
	}
	protected void voteFail() {
		HashMap<String, String> rep = new HashMap<>();
		rep.put("{YESPERCENT}", String.format(Locale.ENGLISH, "%.2f", getAgreePercentage(true)));
		rep.put("{NOPERCENT}", String.format(Locale.ENGLISH, "%.2f", (100 - getAgreePercentage(true))));
		rep.put("{VOTETYPE}", "Ban");
		List<Player> plList = MiscUtils.quickList((Player) target);
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, plList, Module.getMsg("carbonvote", "vote-failed", true, rep));
	}
	protected void broadcastStart() {
		HashMap<String, String> rep = new HashMap<>();
		rep.put("{VOTETYPE}", getTargetedVoteType().lname());
		rep.put("{VOTEREASON}", "to " + getTargetedVoteType().lname() + " " + target.getName());
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, Module.getMsg("carbonvote", "vote-started", true, rep));
	}
}
