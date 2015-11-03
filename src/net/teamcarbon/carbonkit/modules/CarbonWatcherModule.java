package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.commands.CarbonWatcher.WatcherCommand;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
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
import net.teamcarbon.carbonlib.Misc.Messages.Clr;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("UnusedDeclaration")
public class CarbonWatcherModule extends Module {
	public static CarbonWatcherModule inst;
	public CarbonWatcherModule() throws DuplicateModuleException { super("CarbonWatcher", "commandwatcher", "cwatcher", "ckw", "cw"); }
	private static List<UUID> watchers = new ArrayList<UUID>();
	public void initModule() {
		inst = this;
		if (!watchers.isEmpty()) watchers.clear();
		for (Player p : Bukkit.getOnlinePlayers())
			if (getData().getStringList("watchers").contains(p.getUniqueId().toString()))
				setWatching(p, true);
		addCmd(new WatcherCommand(this));
		registerListeners();
	}
	public void disableModule() {
		if (!watchers.isEmpty()) watchers.clear();
		unregisterListeners();
	}
	public void reloadModule() {
		disableModule();
		CarbonKit.reloadDefConfig();
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
			if (watchers.contains(opl.getUniqueId()) && !sender.equals(opl)) {
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
		for (UUID id : watchers) {
			Player cwp = Bukkit.getPlayer(id);
			if (cwp == null || !cwp.isOnline()) {
				watchers.remove(id);
				return;
			}
			if (!perm(cwp, "watchplayers", "watchconsole")) {
				setWatching(cwp, false);
				return;
			}
			if (!perm(cwp, "watchconsole")) return;
			cwp.sendMessage(Clr.GRAY + "[CW] " + Clr.GOLD + "CONSOLE: " + Clr.DARKAQUA + e.getCommand());
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
	public static boolean isWatching(OfflinePlayer pl) { return watchers.contains(pl.getUniqueId()); }
	public static void toggleWatching(OfflinePlayer pl) { setWatching(pl, !isWatching(pl)); }
	public static void setWatching(OfflinePlayer pl, boolean b) {
		List<String> list = inst.getData().getStringList("watchers");
		if (b) {
			if (!watchers.contains(pl.getUniqueId()))
				watchers.add(pl.getUniqueId());
			if (!list.contains(pl.getUniqueId().toString()))
				list.add(pl.getUniqueId().toString());
		} else {
			if (watchers.contains(pl.getUniqueId()))
				watchers.remove(pl.getUniqueId());
			if (list.contains(pl.getUniqueId().toString()))
				list.remove(pl.getUniqueId().toString());
		}
		CarbonKit.getConfig(ConfType.DATA).set("CarbonWatcher.watchers", list);
		CarbonKit.saveConfig(ConfType.DATA);
	}
}
