package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.SkullShop.UpdateHeadCommand;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.teamcarbon.carbonkit.commands.SkullShop.DupeHeadCommand;
import net.teamcarbon.carbonkit.commands.SkullShop.HeadCommand;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.MiscUtils;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class SkullShopModule extends Module {
	public SkullShopModule() throws DuplicateModuleException { super("SkullShop", "headshop", "sshop", "hshop", "ss"); }
	public static HashMap<Player, String> getSkulls;
	public static SkullShopModule inst;
	public void initModule() {
		inst = this;
		if (getSkulls == null) getSkulls = new HashMap<Player, String>(); else getSkulls.clear();
		addCmd(new HeadCommand(this));
		addCmd(new UpdateHeadCommand(this));
		addCmd(new DupeHeadCommand(this));
		registerListeners();
	}
	public void disableModule() {
		getSkulls.clear();
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

	@EventHandler
	public void interact(PlayerInteractEvent e) {
		if (!isEnabled()) return;
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (e.getClickedBlock().getState() instanceof Skull) {
				if (MiscUtils.perm(e.getPlayer(), "carbonkit.skullshop.check")) {
					Skull sb = (Skull)e.getClickedBlock().getState();
					if (sb.hasOwner()) {
						getSkulls.put(e.getPlayer(), sb.getOwner());
						HashMap<String, String> rep = new HashMap<String, String>();
						rep.put("{SKULLOWNER}", sb.getOwner());
						if (!MiscUtils.perm(e.getPlayer(), "carbonkit.skullshop.getskull", "carbonkit.skullshop.skull.free"))
							e.getPlayer().sendMessage(MiscUtils.massReplace(CustomMessage.SS_SKULL_CHECK.pre(), rep));
						if (MiscUtils.perm(e.getPlayer(), "carbonkit.skullshop.skull.free") || getConfig().getDouble("price", 50000) <= 0) {
							e.getPlayer().sendMessage(MiscUtils.massReplace(CustomMessage.SS_GET_SKULL_FREE.pre(), rep));
						} else if (MiscUtils.perm(e.getPlayer(), "carbonkit.skullshop.getskull")) {
							rep.put("{PRICE}", getConfig().getDouble("price", 50000) + "");
							e.getPlayer().sendMessage(MiscUtils.massReplace(CustomMessage.SS_GET_SKULL.pre(), rep));
						}
					}
				}
			}
		}
	}
	@EventHandler
	public void playerQuit(PlayerQuitEvent e) {
		if (!isEnabled()) return;
		if (getSkulls.containsKey(e.getPlayer()))
			getSkulls.remove(e.getPlayer());
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	public static String getSavedSkull(Player pl) { if (hasSavedSkull(pl)) return getSkulls.get(pl); return null; }
	public static boolean hasSavedSkull(Player pl) { return getSkulls.containsKey(pl); }
}
