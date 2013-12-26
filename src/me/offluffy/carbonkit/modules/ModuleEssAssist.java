package me.offluffy.carbonkit.modules;

import java.util.ArrayList;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.cmds.CommandToggleInteract;
import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Module;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class ModuleEssAssist extends Module {
	private List<String> antiInteractMap = new ArrayList<String>();
	public ModuleEssAssist() throws DuplicateModuleException {
		super("EssentialsAssist", "essassist", "eassist", "ea");
		requires.add("Essentials");
	}

	@Override
	public void initModule() {
		addCmd(new CommandToggleInteract(this));
	}

	@Override
	public void disableModule() {}

	@Override
	protected boolean hasListeners() { return true; }

	@Override
	public boolean hasDependencies() {
		for (String r : requires)
			if (!CarbonKit.pm.isPluginEnabled(r))
				return false;
		return true;
	}
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/
	
	@EventHandler
	public void targetEvent(EntityTargetEvent e) {
		if (isEnabled()) {
			if (e.getTarget() instanceof Player) {
				Player target = (Player)e.getTarget();
				if (isAntiInteractEnabled(target.getName()) || Lib.perm(target, "carbonkit.toggleinteract.force")) {
					if (isEssVanished(target.getName())) {
						e.setCancelled(true);
					} else if(!Lib.perm(target, "carbonkit.toggleinteract.force")) {
						removeAntiInteract(target.getName());
						target.sendMessage(Messages.Clr.HEAD + "Vanish-Interact enabled (no longer vanished)");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void pickupEvent(PlayerPickupItemEvent e) {
		if (isEnabled()) {
			Player target = e.getPlayer();
			if (isAntiInteractEnabled(target.getName()) || Lib.perm(target, "carbonkit.toggleinteract.force")) {
				if (isEssVanished(target.getName())) {
					e.setCancelled(true);
				} else if(!Lib.perm(target, "carbonkit.toggleinteract.force")) {
					removeAntiInteract(target.getName());
					target.sendMessage(Messages.Clr.HEAD + "Vanish-Interact enabled (no longer vanished)");
				}
			}
		}
	}

	@EventHandler
	public void interactEvent(PlayerInteractEvent e) {
		if (isEnabled()) {
			Player target = e.getPlayer();
			if (e.getAction().equals(Action.PHYSICAL)) {
				if (isAntiInteractEnabled(target.getName()) || Lib.perm(target, "carbonkit.toggleinteract.force")) {
					if (isEssVanished(target.getName())) {
						e.setCancelled(true);
					} else if(!Lib.perm(target, "carbonkit.toggleinteract.force")) {
						removeAntiInteract(target.getName());
						target.sendMessage(Messages.Clr.HEAD + "Vanish-Interact enabled (no longer vanished)");
					}
				}
			}
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
	
	public boolean isEssVanished(String player) {
		if (CarbonKit.pm.isPluginEnabled("Essentials")) {
			Essentials ess = (Essentials)CarbonKit.pm.getPlugin("Essentials");
			User u = ess.getUserMap().getUser(player);
			if (u.isVanished())
				return true;
		}
		return false;
	}
	
	public boolean isAntiInteractEnabled(String player) {
		if (antiInteractMap.contains(player))
			return true;
		return false;
	}
	
	public void addAntiInteract(String player) {
		if (!antiInteractMap.contains(player))
			antiInteractMap.add(player);
	}
	
	public void removeAntiInteract(String player) {
		if (antiInteractMap.contains(player))
			antiInteractMap.remove(player);
	}
}
