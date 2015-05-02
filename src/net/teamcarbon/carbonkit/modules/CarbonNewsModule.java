package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.CarbonNews.CarbonNewsCommand;
import net.teamcarbon.carbonkit.utils.CarbonNews.FormattedMessage;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;
import net.teamcarbon.carbonkit.tasks.BroadcastTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnusedDeclaration")
public class CarbonNewsModule extends Module {

	public static List<BroadcastTask> tasks;
	public static ConfigurationSection setDefaults;

	public CarbonNewsModule() throws DuplicateModuleException { super("CarbonNews", "cnews", "cn"); }
	public void initModule() {
		tasks = new ArrayList<BroadcastTask>();

		setDefaults = CarbonKit.getConfig(ConfType.NEWS).getConfigurationSection("setDefaults");
		setDefaults.set("setEnabled", false);
		setDefaults.set("messages", new ArrayList<String>());

		Set<String> keys = CarbonKit.getConfig(ConfType.NEWS).getConfigurationSection("MessageSets").getKeys(false);
		for (String k : keys) {
			long delay = CarbonKit.getConfig(ConfType.NEWS).getLong("MessageSets." + k + ".delaySeconds", setDefaults.getLong("delaySeconds", 60L)) * 20L;
			BroadcastTask bt = new BroadcastTask(k);
			bt.runTaskTimer(CarbonKit.inst, delay, delay);
			tasks.add(bt);
		}
		addCmd(new CarbonNewsCommand(this));
		registerListeners();
	}
	public void disableModule() { unregisterListeners(); }
	public void reloadModule() {
		disableModule();
		initModule();
	}
	protected boolean needsListeners() { return true; }
	public boolean hasAllDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler
	public void loginEvent(final PlayerJoinEvent e) {
		if (!isEnabled()) return;
		Bukkit.getScheduler().scheduleSyncDelayedTask(CarbonKit.inst, new Runnable() {
			@Override
			public void run() {
				boolean perm =  getConfig().getBoolean("welcomeMessage.requirePermission", false);
				for (String message : getConfig().getStringList("welcomeMessage.messageLines")) {
					Player[] pls = new Player[] { e.getPlayer() };
					CarbonNewsModule.broadcastFormatted(message, pls, perm, "carbonnews.welcome");
				}
			}
		}, getConfig().getLong("welcomeMessage.delaySeconds", 2L) * 20L);
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	public static void broadcastNormal(String msg, Player[] pls, boolean needsPerm, String... perms) {
		for (Player pl : pls) { if (!needsPerm || MiscUtils.perm(pl, perms)) pl.sendMessage(msg); }
	}

	public static void broadcastFormatted(String msg, Player[] pls, boolean needsPerm, String ... perms) {
		FormattedMessage fm = new FormattedMessage("");
		msg = Clr.trans(msg);
		Pattern pattern = Pattern.compile("\\{((?:(?:LNK|TTP|CMD|STY|CLR)~[^~|]+?\\|)*TXT~[^~|]+?(?:(?:\\||})(?:(?:LNK|TTP|CMD|STY|CLR)~[^~|]+?))*)}");
		Matcher matcher = pattern.matcher(msg);
		if (!matcher.find()) {
			broadcastNormal(msg, pls, needsPerm, perms);
		} else {
			int pos1 = 0, pos2;
			matcher.reset();
			while (matcher.find()) {
				// Insert text between matches
				pos2 = msg.indexOf("{" + matcher.group(1) + "}");
				fm.then(msg.substring(pos1, pos2));
				pos1 = msg.indexOf("{" + matcher.group(1) + "}")+matcher.group(1).length()+2;

				String region = matcher.group(1);
				String txt = "", lnk = "", ttp = "", cmd = "";
				boolean flnk = false, fttp = false, fcmd = false;
				for (String s : region.split("\\|")) {
					String[] sa = s.split("~");
					String key = sa[0], value = Clr.trans(sa[1]);
					if (key.equals("TXT")) {
						txt = value;
					} else if (key.equals("LNK")) {
						lnk = value;
						if (!lnk.contains("http://") && !lnk.contains("https://"))
							lnk = "http://" + lnk;
						flnk = true;
					} else if (key.equals("TTP")) {
						ttp = value;
						fttp = true;
					} else if (key.equals("CMD")) {
						cmd = value;
						fcmd = true;
					}
				}
				fm.then(txt);
				if (fttp) fm.tooltip(ttp);
				if (fcmd) fm.command(cmd);
				if (flnk) fm.link(lnk);
			}
			if (!(pos1 > msg.length()-1)) fm.then(msg.substring(pos1)); // Add the rest of message
		}
		for (Player pl : pls)
			if (!needsPerm || MiscUtils.perm(pl, perms))
				fm.send(pl);
	}

	public static boolean isMessageSet(String setName) {
		for (BroadcastTask bt : tasks)
			if (bt.getSetName().equalsIgnoreCase(setName))
				return true;
		return false;
	}

	public static BroadcastTask getMessageSet(String setName) {
		for (BroadcastTask bt : tasks)
			if (bt.getSetName().equalsIgnoreCase(setName))
				return bt;
		return null;
	}
}
