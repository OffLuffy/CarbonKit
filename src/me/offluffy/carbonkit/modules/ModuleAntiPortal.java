package me.offluffy.carbonkit.modules;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.Log;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.NumUtils;

import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.inventory.ItemStack;

public class ModuleAntiPortal extends Module {
	public ModuleAntiPortal() throws DuplicateModuleException {
		super("AntiPortal", "antip", "ap");
	}

	@Override
	public void initModule() {}

	@Override
	public void disableModule() {}

	@Override
	protected boolean hasListeners() { return true; }

	@Override
	public boolean hasDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void portalCreateEvent(EntityCreatePortalEvent e) {
		Log.debug("Portal event triggered");
		if (isEnabled() && e.getPortalType().equals(PortalType.ENDER) && e.getEntityType().equals(EntityType.ENDER_DRAGON)) {
			Log.debug("Portal is end portal formed by ender dragon");
			String world = e.getBlocks().get(0).getWorld().getName();
			if (CarbonKit.config.getStringList(getName() + ".worlds").contains(world)) {
				Log.debug("Portal world not found in AntiPortal list");
				if (CarbonKit.config.getBoolean(getName() + ".generate-egg")) {
					e.setCancelled(true);
					Block b = e.getEntity().getLocation().getBlock();
					String matString = CarbonKit.config.getString(getName() + ".platform-mat", "glowstone");
					Material plat = Material.GLOWSTONE;
					if (NumUtils.isInteger(matString))
						plat = Material.getMaterial(Integer.parseInt(matString));
					else
						plat = Material.getMaterial(CarbonKit.config.getString(getName() + ".platform-mat", "glowstone"));
					if (plat == null)
						plat = Material.GLOWSTONE;
					if (!plat.isSolid())
						plat = Material.GLOWSTONE;
					while (b.getType() == Material.AIR)
						b = b.getRelative(BlockFace.DOWN);
					b = b.getRelative(BlockFace.UP).getRelative(BlockFace.UP);
					b.setType(plat);
					b = b.getRelative(BlockFace.UP);
					b.setType(Material.DRAGON_EGG);
				} else if (CarbonKit.config.getBoolean(getName() + ".drop-egg")) {
					e.setCancelled(true);
					e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), new ItemStack(Material.DRAGON_EGG, 1));
				} else
					e.setCancelled(true);
			}
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
}
