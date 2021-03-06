package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.commands.CarbonCore.CarbonKitCommand;
import net.teamcarbon.carbonkit.commands.CarbonCore.CarbonReloadCommand;
import net.teamcarbon.carbonkit.commands.CarbonCore.CarbonToggleCommand;
import net.teamcarbon.carbonkit.events.coreEvents.FinishModuleLoadingEvent;
import net.teamcarbon.carbonkit.events.coreEvents.PlayerFirstJoinEvent;
import net.teamcarbon.carbonkit.events.coreEvents.PlayerRejoinEvent;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.UserStore;
import net.teamcarbon.carbonkit.utils.LagMeter;
import net.teamcarbon.carbonkit.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import static net.teamcarbon.carbonkit.utils.Messages.Clr.*;

@SuppressWarnings("UnusedDeclaration")
public class CarbonCoreModule extends Module {

	public CarbonCoreModule() throws DuplicateModuleException { super(CarbonKit.inst, "CarbonKit", "ckit", "core", "ck"); }
	public static CarbonCoreModule inst;
	public void initModule() {
		inst = this;
		addCmd(new CarbonKitCommand(this));
		addCmd(new CarbonReloadCommand(this));
		addCmd(new CarbonToggleCommand(this));
		if (!LagMeter.initialized())
			Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CarbonKit.inst, new LagMeter(), 20L, 1L);
		registerListeners();
	}
	public void disableModule() {
		for (Module m : Module.getAllModules())
			if (!(m instanceof CarbonCoreModule)) m.disableModule();
		unregisterListeners();
	}
	public void reloadModule() {
		CarbonKit.reloadAllConfigs();
		for (Module m : Module.getAllModules())
			if (m.isEnabled() && !(m instanceof CarbonCoreModule)) m.reloadModule();
	}
	protected boolean needsListeners() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler
	public void finishedLoading(FinishModuleLoadingEvent e) {
		String enabled = "enabled: ", disabled = "disabled: ";
		enabled += MiscUtils.implode(", ",0, e.getEnabledModules());
		disabled += MiscUtils.implode(", ",0, e.getDisabledModules());
		CarbonKit.log.info("CarbonKit finished loading modules.");
		CarbonKit.log.log(LIME + enabled);
		CarbonKit.log.log(RED + disabled);
	}

	@EventHandler
	public void playerLogin(PlayerLoginEvent e) {
		Player pl = e.getPlayer();
		UserStore us = CarbonKit.getPlayerData(pl.getUniqueId());
		if (!us.getLastUsername().equals(pl.getName())) {
			us.addPreviousNames(pl.getName());
			us.setLastUsername(pl.getName());
		}
		CarbonKit.pm.callEvent(pl.hasPlayedBefore() ? new PlayerRejoinEvent(pl) : new PlayerFirstJoinEvent(pl));
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent e) {
		final UUID id = e.getPlayer().getUniqueId();
		Bukkit.getScheduler().runTaskLater(CarbonKit.inst, new Runnable() {
			public void run() {
				CarbonKit.uncachePlayerData(id);
			}
		}, 1L);
	}

	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
}
