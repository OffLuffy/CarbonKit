package net.teamcarbon.carbonkit.commands.Misc;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.modules.MiscModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@SuppressWarnings("UnusedDeclaration")
public class OnlineTimeCommand extends ModuleCmd {

	public OnlineTimeCommand(Module module) { super(module, "onlinetime"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0) {
			OfflinePlayer p = MiscUtils.getPlayer(args[0], CarbonKit.checkOffline);
			if (p != null) {
				if (sender.equals(p)) {
					if (!MiscUtils.perm(sender, "carbonkit.misc.onlinetime.self")) {
						sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
						return;
					}
					displayOnlineTime(sender, p);
				} else {
					if (!MiscUtils.perm(sender, "carbonkit.misc.onlinetime.others")) {
						sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
						return;
					}
					displayOnlineTime(sender, p);
				}
			} else {
				if (MiscUtils.perm(sender, "carbonkit.misc.onlinetime.others")) {
					sender.sendMessage(CustomMessage.GEN_PLAYER_NOT_FOUND.noPre());
				} else {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				}
			}
		} else {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Clr.RED + "Console usage: /" + label + " <user>");
				return;
			}
			if (!MiscUtils.perm(sender, "carbonkit.misc.onlinetime.self")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			displayOnlineTime(sender, (Player) sender);
			if (MiscUtils.perm(sender, "carbonkit.misc.onlinetime.others")) {
				sender.sendMessage(Clr.NOTE + "To view another user's online time, use "
						+ Clr.fromChars("3o") + "/ot <user>");
			}
		}
	}

	public void displayOnlineTime(CommandSender sender, OfflinePlayer p) {
		UUID id = p.getUniqueId();
		MiscModule.inst.updateOnlineTime(id, true, false);
		String pre = "online-time." + id + ".";
		CustomMessage.printHeader(sender, "Online Time for " + p.getName());
		sender.sendMessage(Clr.AQUA + "First seen: " + Clr.DARKAQUA + parseMillis(getMod().getData().getLong(pre + "first-seen", -1L), true));
		sender.sendMessage(Clr.AQUA + "Last online: " + Clr.DARKAQUA + parseMillis(getMod().getData().getLong(pre + "last-online", -1L), true));
		sender.sendMessage(Clr.AQUA + "Overall online time: " + Clr.DARKAQUA + parseMillis(getMod().getData().getLong(pre + "overall-time", -1), false));
		sender.sendMessage(Clr.AQUA + "Time this month: " + Clr.DARKAQUA + parseMillis(getMod().getData().getLong(pre + "this-month", -1), false));
		sender.sendMessage(Clr.AQUA + "Time last month: " + Clr.DARKAQUA + parseMillis(getMod().getData().getLong(pre + "last-month", -1), false));
		sender.sendMessage(Clr.AQUA + "Average monthly time: " + Clr.DARKAQUA + parseMillis(getMod().getData().getLong(pre + "monthly-avg", -1), false));
		sender.sendMessage(Clr.AQUA + "Average session time: " + Clr.DARKAQUA + parseMillis(getMod().getData().getLong(pre + "average-session", -1), false));
		CustomMessage.printFooter(sender);
	}

	private String parseMillis(long millis, boolean asDate) {
		if (asDate) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(millis));
			String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
			return months[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR)
					+ " - " + cal.get(Calendar.HOUR) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0":"") + cal.get(Calendar.MINUTE)
					+ (cal.get(Calendar.AM_PM) == Calendar.AM ? "AM":"PM");
		} else {
			String time = "";
			long mpyr = 31556900000L, mpmt = 2630000000L, mpdy = 86400000L, mphr = 3600000L, mpmn = 60000L, mpsc = 1000;
			boolean syr = getMod().getConfig().getBoolean("online-time.show-years", true),
					smt = getMod().getConfig().getBoolean("online-time.show-months", true),
					sdy = getMod().getConfig().getBoolean("online-time.show-days", true),
					shr = getMod().getConfig().getBoolean("online-time.show-hours", true),
					smn = getMod().getConfig().getBoolean("online-time.show-minutes", true),
					ssc = getMod().getConfig().getBoolean("online-time.show-seconds", false);
			boolean addSpace = false;
			if (millis <= 0) { return "N/A"; }
			if (millis >= mpyr && syr) { // years
				long yr = millis / mpyr;
				millis %= mpyr;
				time += yr + " year" + ((yr != 1) ? "s" : "");
				addSpace = true;
			}
			if (millis >= mpmt && smt) { // months
				long mt = millis / mpmt;
				millis %= mpmt;
				if (addSpace) time += ", ";
				time += mt + " month" + ((mt != 1) ? "s" : "");
				addSpace = true;
			}
			if (millis >= mpdy && sdy) { // days
				long dy = millis / mpdy;
				millis %= mpdy;
				if (addSpace) time += ", ";
				time += dy + " day" + ((dy != 1) ? "s" : "");
				addSpace = true;
			}
			if (millis >= mphr && shr) { // hours
				long hr = millis / mphr;
				millis %= mphr;
				if (addSpace) time += ", ";
				time += hr + " hr" + ((hr != 1) ? "s" : "");
				addSpace = true;
			}
			if (millis >= mpmn && smn) { // minutes
				long mn = millis / mpmn;
				millis %= mpmn;
				if (addSpace) time += ", ";
				time += mn + " min" + ((mn != 1) ? "s" : "");
				addSpace = true;
			}
			if (millis >= mpsc && ssc) { // seconds
				long sc = millis / mpsc;
				if (addSpace) time += ", ";
				time += sc + " sec" + ((sc != 1) ? "s" : "");
			}
			return time;
		}
	}

}
