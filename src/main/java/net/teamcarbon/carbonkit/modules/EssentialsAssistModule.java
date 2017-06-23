package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.UserStore;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.MiscUtils;

@SuppressWarnings({"UnusedDeclaration", "SuspiciousMethodCalls"})
public class EssentialsAssistModule extends Module {
	public static EssentialsAssistModule inst;
	public EssentialsAssistModule() throws DuplicateModuleException {
		super(CarbonKit.inst, "EssentialsAssist", "essassist", "eassist", "ea");
		addRequires("Essentials");
	}
	private final static String VT = "vanish-toggles.prevent-";
	public void initModule() {
		inst = this;
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
	public void pressure(PlayerInteractEvent e) {
		if (!isEnabled()) return;
		UserStore us = CarbonKit.getPlayerData(e.getPlayer().getUniqueId());
		if (e.getAction().equals(Action.PHYSICAL)) {
			if (getConfig().getBoolean(VT + "pressure", true)) {
				checkAutoInteract(e.getPlayer());
				if (us.getBoolean(inst.getName() + ".interact", true))
					e.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void mobTarget(EntityTargetLivingEntityEvent e) {
		if (!isEnabled()) return;
		if (e.getTarget() != null && e.getTarget().getType().equals(EntityType.PLAYER)) {
			UserStore us = CarbonKit.getPlayerData(e.getTarget().getUniqueId());
			if (getConfig().getBoolean(VT + "mob-targeting", true)) {
				checkAutoInteract((Player) e.getTarget());
				if (us.getBoolean(inst.getName() + ".interact", true))
					e.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void itemPickup(PlayerPickupItemEvent e) {
		if (!isEnabled()) return;
		UserStore us = CarbonKit.getPlayerData(e.getPlayer().getUniqueId());
		if (getConfig().getBoolean(VT + "item-pickup", true)) {
			checkAutoInteract(e.getPlayer());
			if (us.getBoolean(inst.getName() + ".interact", true))
				e.setCancelled(true);
		}
	}
	@EventHandler
	public void damage(EntityDamageEvent e) {
		if (!isEnabled()) return;
		if (e.getEntity().getType().equals(EntityType.PLAYER)) {
			UserStore us = CarbonKit.getPlayerData(e.getEntity().getUniqueId());
			if (getConfig().getBoolean(VT + "damage", true)) {
				checkAutoInteract((Player)e.getEntity());
				if (us.getBoolean(inst.getName() + ".interact", true))
					e.setCancelled(true);
			}
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	public static boolean canInteract(Player pl) {
		UserStore us = CarbonKit.getPlayerData(pl.getUniqueId());
		return us.getBoolean(inst.getName() + ".interact", true);
	}
	public static void toggleInteract(Player pl) { setInteract(pl, !canInteract(pl)); }
	public static void setInteract(Player pl, boolean b) {
		UserStore us = CarbonKit.getPlayerData(pl.getUniqueId());
		us.set(inst.getName() + ".interact", b);
		us.save();
	}
	public static boolean isEssVanished(Player pl) {
		if (MiscUtils.checkPlugin("Essentials", true)) {
			com.earth2me.essentials.Essentials ess = (com.earth2me.essentials.Essentials)MiscUtils.getPlugin("Essentials", true);
			if (ess != null) {
				com.earth2me.essentials.User eUser = ess.getUser(pl);
				return eUser.isVanished();
			}
		}
		return false;
	}
	private void checkAutoInteract(Player pl) {
		if (perm(pl, "auto-anti-interact")) {
			if (!isEssVanished(pl) && !canInteract(pl)) {
				setInteract(pl, false);
				if (getConfig().getBoolean("message-on-auto-anti-interact", false))
					pl.sendMessage(CustomMessage.EA_ANTIINTERACT_AUTO_DISABLE.pre());
			} else if (isEssVanished(pl) && canInteract(pl)) {
				setInteract(pl, true);
				if (getConfig().getBoolean("message-on-auto-anti-interact", false))
					pl.sendMessage(CustomMessage.EA_ANTIINTERACT_AUTO_ENABLE.pre());
			}
		}
	}
}
