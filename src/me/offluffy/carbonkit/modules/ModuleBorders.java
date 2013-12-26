package me.offluffy.carbonkit.modules;

import me.offluffy.carbonkit.cmds.CommandBorder;
import me.offluffy.carbonkit.utils.Border;
import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.PlayerData;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class ModuleBorders extends Module {
	public ModuleBorders() throws DuplicateModuleException {
		super("CarbonBorders", "carbonborder", "border", "borders", "bordercraft", "bordercrafter");
	}

	@Override
	public void initModule() {
		Border.loadBorders();
		addCmd(new CommandBorder(this));
	}

	@Override
	public void disableModule() {}

	@Override
	protected boolean hasListeners() { return true; }
	@Override
	public boolean hasDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/
	
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void moveEvent(PlayerMoveEvent e) {
		if (isEnabled()) {
			Player pl = e.getPlayer();
			if (PlayerData.getCoords(e.getPlayer().getName()) == null)
				PlayerData.setCoords(pl.getName(), (int)pl.getLocation().getX(), (int)pl.getLocation().getZ());
			int fX = PlayerData.getCoords(pl.getName())[0];
			int fZ = PlayerData.getCoords(pl.getName())[1];
			int tX = (int)pl.getLocation().getX();
			int tZ = (int)pl.getLocation().getZ();
			if ((fX != tX) || (fZ != tZ)) {
				PlayerData.setCoords(pl.getName(), (int)pl.getLocation().getX(), (int)pl.getLocation().getZ());
				Border.check(pl);
			}
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
}
