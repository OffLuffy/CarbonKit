package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.CarbonNews.CarbonNewsCommand;
import net.teamcarbon.carbonkit.utils.CarbonNews.FormattedMessage;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonkit.tasks.BroadcastTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnusedDeclaration")
public class CarbonNewsModule extends Module {

	public static final String NAME = "CarbonNews";

	public CarbonNewsModule() throws DuplicateModuleException {
		super(NAME, "cnews", "cn");
		reqVer = "1_8_R3";
	}
	public void initModule() {

		ConfigurationSection setDefaults = CarbonKit.getConfig(ConfType.NEWS).getConfigurationSection("setDefaults");
		setDefaults.set("setEnabled", false);
		setDefaults.set("messages", new ArrayList<String>());

		Set<String> keys = CarbonKit.getConfig(ConfType.NEWS).getConfigurationSection("MessageSets").getKeys(false);
		for (String k : keys) {
			BroadcastTask bt = new BroadcastTask(k);
			if (bt.isEnabled()) bt.startBroadcasts();
		}
		addCmd(new CarbonNewsCommand(this));
		registerListeners();
	}
	public void disableModule() {
		unregisterListeners();
		BroadcastTask.disableAllTasks();
		BroadcastTask.removeAllTasks();
	}
	public void reloadModule() {
		disableModule();
		initModule();
	}
	protected boolean needsListeners() { return true; }
	
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
					ArrayList<Player> pls = new ArrayList<Player>();
					pls.add(e.getPlayer());
					CarbonNewsModule.broadcastFormatted(message, pls, true, false, false, perm, "carbonnews.welcome");
				}
			}
		}, getConfig().getLong("welcomeMessage.delaySeconds", 2L) * 20L);
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	public static void broadcastNormal(String msg, Collection<? extends Player> pls, boolean toPlayers, boolean toConsole, boolean colorConsole, boolean needsPerm, String... perms) {
		if (!toPlayers && !toConsole) return;
		if (toPlayers)
			for (Player pl : pls) { if (pl.isOnline() && (!needsPerm || MiscUtils.perm(pl, perms))) pl.sendMessage(msg); }
		if (toConsole)
			Bukkit.getServer().getConsoleSender().sendMessage(colorConsole ? msg : ChatColor.stripColor(msg));
	}

	public static void broadcastFormatted(String msg, Collection<? extends Player> pls, boolean toPlayers, boolean toConsole, boolean colorConsole, boolean needsPerm, String ... perms) {
		if (!toPlayers && !toConsole) return;
		FormattedMessage fm = new FormattedMessage("");
		msg = Clr.trans(msg);
		Pattern pattern = Pattern.compile("\\{((?:(?:LNK|TTP|CMD|STY|CLR)~[^~|]+?\\|)*TXT~[^~|]+?(?:(?:\\||})(?:(?:LNK|TTP|CMD|STY|CLR)~[^~|]+?))*)}");
		Matcher matcher = pattern.matcher(msg);
		if (!matcher.find()) {
			broadcastNormal(msg, pls, toPlayers, toConsole, colorConsole, needsPerm, perms);
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
		if (toPlayers)
			for (Player pl : pls)
				if (pl.isOnline() && (!needsPerm || MiscUtils.perm(pl, perms)))
					fm.send(pl);
		if (toConsole) Bukkit.getServer().getConsoleSender().sendMessage(colorConsole ? fm.toString() : ChatColor.stripColor(fm.toString()));
	}

	public static void deleteSet(String setName) {
		if (CarbonKit.getConfig(ConfType.NEWS).contains("MessageSets." + setName))
			CarbonKit.getConfig(ConfType.NEWS).set("MessageSets." + setName, null);
		CarbonKit.saveConfig(ConfType.NEWS);
		BroadcastTask.removeTask(setName);
	}
}
