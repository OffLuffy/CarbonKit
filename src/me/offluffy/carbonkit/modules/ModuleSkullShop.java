package me.offluffy.carbonkit.modules;

import java.util.HashMap;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.cmds.CommandGetSkull;
import me.offluffy.carbonkit.cmds.CommandSkull;
import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Module;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

public class ModuleSkullShop extends Module {
	private static HashMap<String, String> skullQueue = new HashMap<String, String>();
	public ModuleSkullShop() throws DuplicateModuleException {
		super("SkullShop", "skulls", "skull", "heads", "head", "headshop");
	}

	@Override
	public void initModule() {
		addCmd(new CommandSkull(this));
		addCmd(new CommandGetSkull(this));
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
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (isEnabled()) {
			if (Lib.perm(e.getPlayer(), "carbonkit.skull")) {
				if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (e.getClickedBlock().getState() instanceof Skull) {
						Player player = e.getPlayer();
						Skull skull = (Skull)e.getClickedBlock().getState();
						if (skull.hasOwner()) {
							String owner = skull.getOwner();
							double prc = CarbonKit.config.getDouble(getName() + ".price", 50000d);
							String actn = " spawn it.";
							if (prc > 0 && !Lib.perm(player, "carbonkit.skull.free"))
								actn = " buy it for " + ( (prc%1.0==0) ? ""+((int)prc) : String.format("%.2f", prc) );
							player.sendMessage(Messages.Clr.NORM + "This is " + Messages.Clr.HEAD + owner + "'s" + Messages.Clr.NORM + " head. Type " + Messages.Clr.HEAD + "/getskull" + Messages.Clr.NORM + " to" + actn);
							ModuleSkullShop.setQueuedSkull(player.getName(), owner);
						}
					}
				}
			}
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
	
	public static String getQueuedSkull(String buyer) {
		if (!skullQueue.containsKey(buyer))
			return null;
		else
			return skullQueue.get(buyer);
	}
	public static void setQueuedSkull(String buyer, String skull) {
		skullQueue.put(buyer, skull);
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack createSkull(String owner) {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM,1,(short)SkullType.PLAYER.ordinal());
		MaterialData data = skull.getData();
		data.setData((byte)3);
		SkullMeta meta = (SkullMeta)skull.getItemMeta();
		meta.setOwner(owner);
		skull.setItemMeta(meta);
		return skull;
	}
}
