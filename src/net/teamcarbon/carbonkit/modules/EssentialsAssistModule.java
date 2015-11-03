package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.Misc.MiscUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration", "SuspiciousMethodCalls"})
public class EssentialsAssistModule extends Module { // TODO Implement essentials user purging into EssAssist?
	public static EssentialsAssistModule inst;
	public EssentialsAssistModule() throws DuplicateModuleException {
		super("EssentialsAssist", "essassist", "eassist", "ea");
		addRequires("Essentials");
	}
	private static List<OfflinePlayer> noInteract = new ArrayList<OfflinePlayer>();
	private final static String VT = "vanish-toggles.prevent-";
	public void initModule() {
		inst = this;
		if (!noInteract.isEmpty()) noInteract.clear();
		else
			noInteract.clear();
		if (Bukkit.getOnlinePlayers().size() > 0) {
			String path = "no-interact";
			for (Player p : Bukkit.getOnlinePlayers()) {
				String id = p.getUniqueId().toString();
				if (getData().getStringList(path).contains(id))
					noInteract.add(p);
			}
		}
		registerListeners();
	}
	public void disableModule() {
		if (!noInteract.isEmpty()) noInteract.clear();
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
	public void pressure(PlayerInteractEvent e) {
		if (!isEnabled()) return;
		if (e.getAction().equals(Action.PHYSICAL)) {
			if (getConfig().getBoolean(VT + "pressure", true)) {
				checkAutoInteract(e.getPlayer());
				if (noInteract.contains(e.getPlayer()))
					e.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void mobTarget(EntityTargetLivingEntityEvent e) {
		if (!isEnabled()) return;
		if (e.getTarget() != null && e.getTarget().getType().equals(EntityType.PLAYER)) {
			if (getConfig().getBoolean(VT + "mob-targeting", true)) {
				checkAutoInteract((Player) e.getTarget());
				if (noInteract.contains(e.getTarget()))
					e.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void itemPickup(PlayerPickupItemEvent e) {
		if (!isEnabled()) return;
		if (getConfig().getBoolean(VT + "item-pickup", true)) {
			checkAutoInteract(e.getPlayer());
			if (noInteract.contains(e.getPlayer()))
				e.setCancelled(true);
		}
	}
	@EventHandler
	public void damage(EntityDamageEvent e) {
		if (!isEnabled()) return;
		if (e.getEntity().getType().equals(EntityType.PLAYER)) {
			if (getConfig().getBoolean(VT + "damage", true)) {
				checkAutoInteract((Player)e.getEntity());
				if (noInteract.contains(e.getEntity()))
					e.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		if (!isEnabled()) return;
		String path = "no-interact", id = e.getPlayer().getUniqueId().toString();
		if (getData().getStringList(path).contains(id))
			noInteract.add(e.getPlayer());
	}
	@EventHandler
	public void playerQuit(PlayerQuitEvent e) {
		if (!isEnabled()) return;
		String path = "no-interact", id = e.getPlayer().getUniqueId().toString();
		List<String> list = getData().getStringList(path);
		if (noInteract.contains(e.getPlayer())) {
			noInteract.remove(e.getPlayer());
			if (!list.contains(id)) list.add(id);
		} else if (list.contains(id)) list.remove(id);
		CarbonKit.getConfig(ConfType.DATA).set(path, list);
		CarbonKit.saveConfig(ConfType.DATA);
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	public static boolean canInteract(Player pl) { return noInteract.contains(pl); }
	public static void toggleInteract(Player pl) {
		if (noInteract.contains(pl))
			setInteract(pl, false);
		else
			setInteract(pl, true);
	}
	public static void setInteract(Player pl, boolean b) {
		String path = "no-interact", id = pl.getUniqueId().toString();
		List<String> list = inst.getData().getStringList(path);
		if (b) {
			if (!noInteract.contains(pl)) {
				noInteract.add(pl);
				if (!list.contains(id)) list.add(id);
			}
		} else {
			if (noInteract.contains(pl)) {
				noInteract.remove(pl);
				if (list.contains(id)) list.remove(id);
			}
		}
		CarbonKit.getConfig(ConfType.DATA).set(inst.getName() + "." + path, list);
		CarbonKit.saveConfig(ConfType.DATA);
	}
	public static boolean isEssVanished(Player pl) {
		if (MiscUtils.checkPlugin("Essentials", true)) {
			com.earth2me.essentials.Essentials ess = (com.earth2me.essentials.Essentials)MiscUtils.getPlugin("Essentials", true);
			com.earth2me.essentials.User eUser = ess.getUser(pl);
			return eUser.isVanished();
		}
		return false;
	}
	private void checkAutoInteract(Player pl) {
		if (perm(pl, "auto-anti-interact")) {
			if (!isEssVanished(pl) && noInteract.contains(pl)) {
				setInteract(pl, false);
				if (getConfig().getBoolean("message-on-auto-anti-interact", false))
					pl.sendMessage(CustomMessage.EA_ANTIINTERACT_AUTO_DISABLE.pre());
			} else if (isEssVanished(pl) && !noInteract.contains(pl)) {
				setInteract(pl, true);
				if (getConfig().getBoolean("message-on-auto-anti-interact", false))
					pl.sendMessage(CustomMessage.EA_ANTIINTERACT_AUTO_ENABLE.pre());
			}
		}
	}
}
