package net.teamcarbon.carbonkit.modules.disabled;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.CarbonEssentials.FreezeCommand;
import net.teamcarbon.carbonkit.commands.CarbonTools.FakeJoinCommand;
import net.teamcarbon.carbonkit.commands.CarbonTools.FakeQuitCommand;
import net.teamcarbon.carbonkit.events.essentialsEvents.EssentialsTeleportEvent;
import net.teamcarbon.carbonkit.utils.CarbonPlayer;
import net.teamcarbon.carbonkit.utils.CarbonEssentials.TeleportHelper;
import net.teamcarbon.carbonkit.utils.CarbonEssentials.TeleportHelper.TeleportCheckResult;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import java.util.HashMap;

// TO-DO List:
// - Add Warps
// - Add Homes
// - Add TP Functions
// - - Requests
// - - Overrides
// - - Cooldowns
// - - Jump/Thru

@SuppressWarnings("UnusedDeclaration")
public class CarbonEssentialsModule extends Module {
	public CarbonEssentialsModule() throws DuplicateModuleException { super("CarbonEssentials", "essentials", "ess"); }

	private static HashMap<String, Location> warps = new HashMap<>();
	private final static String VT = "vanish-toggles.prevent-";
	public static CarbonEssentialsModule inst;

	public void initModule() {
		inst = this;
		addCmd(new FreezeCommand(this));
		addCmd(new FakeJoinCommand(this));
		addCmd(new FakeQuitCommand(this));
		registerListeners();
	}
	public void disableModule() {
		unregisterListeners();
	}
	public void reloadModule() {
		disableModule();
		CarbonKit.reloadDefConfig();
		CarbonKit.reloadConfig(ConfType.DATA);
		initModule();
	}
	protected boolean needsListeners() { return true; }
	public boolean hasAllDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler(ignoreCancelled = true)
	public void playerLogin(PlayerLoginEvent e) {
		if (!isEnabled()) return;
		if (e.getResult() == Result.ALLOWED) { new CarbonPlayer(e.getPlayer()); }
	}

	@EventHandler(ignoreCancelled = true)
	public void playerJoin(PlayerJoinEvent e) {
		if (!isEnabled()) return;
		CarbonPlayer cp = CarbonPlayer.getCarbonPlayer(e.getPlayer(), false);
		boolean cpSee = perm(e.getPlayer(), "seevanished");
		for (Player pl : Bukkit.getOnlinePlayers()) {
			CarbonPlayer ocp = CarbonPlayer.getCarbonPlayer(pl, false);
			boolean ocpSee = perm(pl, "seevanished");
			if (cp != null && cp.isVanished() && !ocpSee) pl.hidePlayer(e.getPlayer());
			if (ocp != null && ocp.isVanished() && !cpSee) e.getPlayer().hidePlayer(pl);
		}
	}

	@EventHandler
	public void pressure(PlayerInteractEvent e) {
		if (!isEnabled()) return;
		if (e.getAction().equals(Action.PHYSICAL)) {
			if (getConfig().getBoolean(VT + "pressure", true)) {
				CarbonPlayer cp = CarbonPlayer.getCarbonPlayer(e.getPlayer(), false);
				if (cp != null) {
					checkAutoInteract(cp);
					if (cp.isVanished()) e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void mobTarget(EntityTargetLivingEntityEvent e) {
		if (!isEnabled()) return;
		if (e.getTarget() != null && e.getTarget().getType().equals(EntityType.PLAYER)) {
			if (getConfig().getBoolean(VT + "mob-targeting", true)) {
				CarbonPlayer cp = CarbonPlayer.getCarbonPlayer(((Player) e.getTarget()), false);
				if (cp != null) {
					checkAutoInteract(cp);
					if (cp.isVanished()) e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void itemPickup(PlayerPickupItemEvent e) {
		if (!isEnabled()) return;
		if (getConfig().getBoolean(VT + "item-pickup", true)) {
			CarbonPlayer cp = CarbonPlayer.getCarbonPlayer(e.getPlayer(), false);
			if (cp != null) {
				checkAutoInteract(cp);
				if (cp.isVanished()) e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void damage(EntityDamageEvent e) {
		if (!isEnabled()) return;
		if (e.getEntity().getType().equals(EntityType.PLAYER)) {
			CarbonPlayer cp = CarbonPlayer.getCarbonPlayer(((Player) e.getEntity()), false);
			if (cp != null) {
				if (cp.hasGodEnabled()) {
					e.setCancelled(true);
				} else if (getConfig().getBoolean(VT + "damage", true)) {
					checkAutoInteract(cp);
					if (cp.isVanished()) e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent e) {
		if (!isEnabled()) return;
		// TODO Unload CarbonPlayer data
	}

	@EventHandler(ignoreCancelled = true)
	public void essTeleport(EssentialsTeleportEvent e) {
		TeleportCheckResult tcr = TeleportHelper.checkTeleport(e.getCarbonPlayer(), e.getDestination());
		e.setCancelled(tcr != TeleportCheckResult.SAFE);
		switch(tcr) {
			case FIRE:
				e.getPlayer().sendMessage(Clr.RED + "There is lava or fire too close to the teleport!");
				break;
			case EXPLOSIVE:
				e.getPlayer().sendMessage(Clr.RED + "There are explosives too close to the teleport!");
				break;
			case FLOOR:
				e.getPlayer().sendMessage(Clr.RED + "A floor couldn't be found under the teleport");
				break;
			case ROOM:
				e.getPlayer().sendMessage(Clr.RED + "There's not enough space at the destination");
				break;
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	/**
	 * CarbonEssentials-specific teleport, handles calling EssentialsTeleportEvent
	 * @param pl The Player to teleport
	 * @param loc The Location to send the Player
	 */
	public void teleport(Player pl, Location loc) {
		EssentialsTeleportEvent ete = new EssentialsTeleportEvent(pl, pl.getLocation(), loc);
		Bukkit.getPluginManager().callEvent(ete);
		if (!ete.isCancelled()) { pl.teleport(ete.getDestination()); }
	}

	/**
	 * Checks if the Player should have anti-interact enabled and sets it accordingly
	 * @param cp The CarbonPlayer object that corresponds to the Player to check
	 */
	private void checkAutoInteract(CarbonPlayer cp) {
		if (!cp.getPlayer().isOnline()) return;
		Player pl = (Player) cp.getPlayer();
		if (perm(pl, "auto-anti-interact")) {
			if (cp != null) {
				if (!cp.isVanished() && cp.isInteracting()) {
					cp.setInteract(false);
					if (getConfig().getBoolean("message-on-auto-anti-interact", false))
						((Player) cp.getPlayer()).sendMessage(CustomMessage.EA_ANTIINTERACT_AUTO_DISABLE.pre());
				} else if (cp.isVanished() && !cp.isInteracting()) {
					cp.setInteract(true);
					if (getConfig().getBoolean("message-on-auto-anti-interact", false))
						((Player) cp.getPlayer()).sendMessage(CustomMessage.EA_ANTIINTERACT_AUTO_ENABLE.pre());
				}
			}
		}
	}
}
