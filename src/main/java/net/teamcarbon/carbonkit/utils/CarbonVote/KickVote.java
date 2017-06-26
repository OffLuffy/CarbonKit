package net.teamcarbon.carbonkit.utils.CarbonVote;

import net.teamcarbon.carbonkit.modules.CarbonVoteModule;
import net.teamcarbon.carbonkit.utils.Module;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.utils.MiscUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("UnusedDeclaration")
public class KickVote extends TargetedVote {
	public KickVote(OfflinePlayer player, OfflinePlayer target) {
		super(player, target, TargetedVoteType.KICK);
	}
	protected void votePass() {
		HashMap<String, String> rep = new HashMap<>();
		rep.put("{TARGET}", target.getName());
		List<Player> plList = MiscUtils.quickList((Player) target);
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, plList, Module.getMsg("carbonvote", "kick-vote-passed", true, rep));
		if (target.isOnline()) ((Player)target).kickPlayer(Module.getMsg("carbonvote", "kick-message", false, rep));
	}
	protected void voteFail() {
		HashMap<String, String> rep = new HashMap<>();
		rep.put("{YESPERCENT}", String.format(Locale.ENGLISH, "%.2f", getAgreePercentage(true)));
		rep.put("{NOPERCENT}", String.format(Locale.ENGLISH, "%.2f", (100 - getAgreePercentage(true))));
		rep.put("{VOTETYPE}", "Kick");
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
