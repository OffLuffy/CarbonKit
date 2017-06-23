package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.commands.CarbonWatcher.WatcherCommand;
import net.teamcarbon.carbonkit.utils.UserStore;
import net.teamcarbon.carbonkit.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.Messages.Clr;

@SuppressWarnings("UnusedDeclaration")
public class CarbonWatcherModule extends Module {
	public static CarbonWatcherModule inst;
	public CarbonWatcherModule() throws DuplicateModuleException { super(CarbonKit.inst, "CarbonWatcher", "commandwatcher", "cwatcher", "ckw", "cw"); }
	public void initModule() {
		inst = this;
		addCmd(new WatcherCommand(this));
		registerListeners();
	}
	public void disableModule() {
		unregisterListeners();
	}
	public void reloadModule() {
		disableModule();
		CarbonKit.inst.reloadConfig();
		CarbonKit.reloadConfig(ConfType.DATA);
		initModule();
	}
	protected boolean needsListeners() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		if (getData().getStringList("watchers").contains(e.getPlayer().getUniqueId().toString())) {
			setWatching(e.getPlayer(), true);
		}
	}

	@EventHandler
	public void cmd(PlayerCommandPreprocessEvent e) {
		if (!isEnabled()) return;
		Player sender = e.getPlayer();
		String label = e.getMessage().split(" ")[0].replace("/", "");
		if (getConfig().getStringList("exempt").contains(sender.getUniqueId().toString()) || isBlacklisted(label)) return;
		for (Player opl : Bukkit.getOnlinePlayers()) {
			if (isWatching(opl) && !sender.equals(opl)) {
				if (!perm(opl, "watchplayers", "watchconsole")) {
					setWatching(opl, false);
					return;
				}
				if (!perm(opl, "watchplayers")) return;
				opl.sendMessage(Clr.GRAY + "[CW] " + Clr.GOLD + sender.getName() + ": " + Clr.DARKAQUA + e.getMessage());
			}
		}
	}

	@EventHandler
	public void serverCmd(ServerCommandEvent e) {
		if (!isEnabled()) return;
		boolean show = true;
		String label = e.getCommand().split(" ")[0].replace("/", "");
		if (isBlacklisted(label)) return;
		for (Player opl : Bukkit.getOnlinePlayers()) {
			if (isWatching(opl)) {
				if (!perm(opl, "watchplayers", "watchconsole")) {
					setWatching(opl, false);
					return;
				}
				if (!perm(opl, "watchconsole")) return;
				opl.sendMessage(Clr.GRAY + "[CW] " + Clr.GOLD + "CONSOLE: " + Clr.DARKAQUA + e.getCommand());
			}
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	private boolean isBlacklisted(String query) {
		if (getConfig() == null) return false;
		for (String b : getConfig().getStringList("blacklist")) {
			if (MiscUtils.eq(query, b)) return true;
			if (getConfig().getBoolean("match-aliases", true) && Bukkit.getPluginCommand(b) != null
					&& MiscUtils.eq(query, Bukkit.getPluginCommand(b).getAliases())) return true;
		}
		return false;
	}
	public static boolean isWatching(OfflinePlayer pl) {
		UserStore us = CarbonKit.getPlayerData(pl.getUniqueId());
		return us.getBoolean(inst.getName() + ".watching", false);
	}
	public static void toggleWatching(OfflinePlayer pl) { setWatching(pl, !isWatching(pl)); }
	public static void setWatching(OfflinePlayer pl, boolean b) {
		UserStore us = CarbonKit.getPlayerData(pl.getUniqueId());
		us.set(inst.getName() + ".watching", b);
		us.save();
	}
}
