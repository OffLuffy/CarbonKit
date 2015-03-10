package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.Misc.*;
import net.teamcarbon.carbonkit.tasks.UpdateOnlineTimeTask;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.LocUtils;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class MiscModule extends Module {

	public static UUID instId;

	public static List<UUID> freezeList = new ArrayList<UUID>();
	public static HashMap<UUID, Long> tempFreezeList = new HashMap<UUID, Long>();
	public static HashMap<UUID, String> addressMap = new HashMap<UUID, String>();
	public static HashMap<UUID, Long> onlineTimeStart = new HashMap<UUID, Long>();
	public static MiscModule inst;

	public MiscModule() throws DuplicateModuleException {
		super("Misc", "miscmodule", "mm");
	}

	public void initModule() {
		inst = this;
		instId = UUID.randomUUID();
		MiscUtils.resetList(freezeList);
		if (addressMap == null) addressMap = new HashMap<UUID, String>();
		// else addressMap.clear(); // Not clearing anymore to save IPs across re-init
		if (onlineTimeStart == null) onlineTimeStart = new HashMap<UUID, Long>();
		else onlineTimeStart.clear();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (needsUnfreezing(p)) unfreezePlayer(p);
			if (isFrozen(p, true)) freezePlayer(p, getUnfreezeTime(p));
			if (p.getAddress() != null) {
				String addr = p.getAddress().toString().replace("/", "");
				addr = addr.substring(0, addr.indexOf(":"));
				addressMap.put(p.getUniqueId(), addr);
			} else { addressMap.put(p.getUniqueId(), "X.X.X.X"); }
			onlineTimeStart.put(p.getUniqueId(), System.currentTimeMillis());
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(CarbonKit.inst, new UpdateOnlineTimeTask(instId), 1L, 6000L);
		addCmd(new SlapCommand(this));
		addCmd(new FreezeCommand(this));
		addCmd(new FakeJoinCommand(this));
		addCmd(new FakeQuitCommand(this));
		addCmd(new RideCommand(this));
		addCmd(new EntCountCommand(this));
		addCmd(new GamemodeCommand(this));
		addCmd(new CustomHelpCommand(this));
		addCmd(new OnlineTimeCommand(this));
		registerListeners();
	}

	public void disableModule() {
		updateAllOnlineTimes();
		freezeList.clear();
		// addressMap.clear(); // Not clearing anymore to save IPs across re-init
		onlineTimeStart.clear();
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
	public void joinEvent(PlayerJoinEvent e) {
		if (!isEnabled()) return;
		if (e.getPlayer().getAddress() != null) {
			String addr = e.getPlayer().getAddress().toString().replace("/", "");
			if (addr.contains(":")) addr = addr.substring(0, addr.indexOf(":"));
			addressMap.put(e.getPlayer().getUniqueId(), addr);
		} else { addressMap.put(e.getPlayer().getUniqueId(), "X.X.X.X"); }
		onlineTimeStart.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
		FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
		String midPath = getName() + "." + "online-time." + e.getPlayer().getUniqueId().toString() + ".";
		long firstSeen = data.getLong(midPath + "first-seen", -1L);
		data.set(midPath + "first-seen", firstSeen == -1L ? System.currentTimeMillis() : firstSeen);
		CarbonKit.saveConfig(ConfType.DATA);
		joinQuitHandle(true, e.getPlayer());
		e.setJoinMessage(null);
	}

	@EventHandler
	public void quitEvent(PlayerQuitEvent e) {
		if (!isEnabled()) return;
		joinQuitHandle(false, e.getPlayer());
		if (addressMap.containsKey(e.getPlayer().getUniqueId()))
			addressMap.remove(e.getPlayer().getUniqueId());
		updateOnlineTime(e.getPlayer().getUniqueId(), true);
		onlineTimeStart.remove(e.getPlayer().getUniqueId());
		e.setQuitMessage(null);
	}

	@EventHandler(ignoreCancelled = true)
	public void entityInteract(PlayerInteractEntityEvent e) {
		if (!isEnabled()) return;
		if (frozenCancellableHandle(e, e.getPlayer())) return;
		if (e.getRightClicked().getType().equals(EntityType.PAINTING) && MiscUtils.perm(e.getPlayer(), "carbonkit.misc.artcycle")) {
			Painting p = (Painting) e.getRightClicked();
			boolean allow = true;
			if (MiscUtils.checkPlugin("GriefPrevention", true)) {
				me.ryanhamshire.GriefPrevention.DataStore ds = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore;
				me.ryanhamshire.GriefPrevention.Claim claim = ds.getClaimAt(p.getLocation(), false, null);
				me.ryanhamshire.GriefPrevention.PlayerData pd = ds.getPlayerData(e.getPlayer().getUniqueId());
				if (claim != null) {
					ArrayList<String> builders = new ArrayList<String>(), managers = new ArrayList<String>();
					claim.getPermissions(builders, null, null, managers);
					String pn = e.getPlayer().getName(), pid = e.getPlayer().getUniqueId().toString();
					allow = MiscUtils.eq(claim.getOwnerName(), pn) || e.getPlayer().isOp() || pd.ignoreClaims
							|| builders.contains(pid) || managers.contains(pid);
				}
			}
			if (allow) {
				boolean s = false;
				Art last = p.getArt();
				for (int c = 0; !s && c < Art.values().length; c++) {
					last = getNextArt(last);
					s = p.setArt(last);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void moveEvent(PlayerMoveEvent e) {
		if (frozenMoveHandle(e.getPlayer(), e.getFrom(), e.getTo()))
			MiscUtils.teleport(e.getPlayer(), e.getFrom());
	}

	@EventHandler(ignoreCancelled = true)
	public void vehicleMoveEvent(VehicleMoveEvent e) {
		for (Entity v = e.getVehicle(); v.getPassenger() != null; v = v.getPassenger()) {
			if (v instanceof Player && isFrozen((Player) v, false)) {
				if (frozenMoveHandle((Player) v, e.getFrom(), e.getTo())) {
					MiscUtils.teleport(v, e.getFrom());
					return;
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void teleport(PlayerTeleportEvent e) { frozenCancellableHandle(e, e.getPlayer()); }

	@EventHandler(ignoreCancelled = true)
	public void itemDrop(PlayerDropItemEvent e) { frozenCancellableHandle(e, e.getPlayer()); }

	@EventHandler(ignoreCancelled = true)
	public void breakEvent(BlockBreakEvent e) { frozenCancellableHandle(e, e.getPlayer()); }

	@EventHandler(ignoreCancelled = true)
	public void placeEvent(BlockPlaceEvent e) { frozenCancellableHandle(e, e.getPlayer()); }

	@EventHandler(ignoreCancelled = true)
	public void damageEvent(BlockDamageEvent e) { frozenCancellableHandle(e, e.getPlayer()); }

	@EventHandler(ignoreCancelled = true)
	public void hangingPlace(HangingPlaceEvent e) { frozenCancellableHandle(e, e.getPlayer()); }

	@EventHandler(ignoreCancelled = true)
	public void hangingBreak(HangingBreakByEntityEvent e) {
		Entity r = e.getRemover();
		Player p = null;
		if (r instanceof Player) p = (Player) e.getRemover();
		if (r instanceof Projectile && ((Projectile) r).getShooter() instanceof Player) p = (Player) ((Projectile) r).getShooter();
		if (e.getRemover() instanceof Player) frozenCancellableHandle(e, p);
	}

	@EventHandler(ignoreCancelled = true)
	public void explosion(EntityExplodeEvent e) {
		if (!isEnabled()) return;
		for (Block b : new ArrayList<Block>(e.blockList())) {
			if (b.getType() == Material.DIAMOND_ORE) {
				e.blockList().remove(b);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void itemRename(InventoryClickEvent e){
		// check if the event has been cancelled by another plugin
		if(!e.isCancelled()){
			HumanEntity ent = e.getWhoClicked();
			// not really necessary
			if(ent instanceof Player){
				Player player = (Player)ent;
				Inventory inv = e.getInventory();
				// see if the event is about an anvil
				if(inv instanceof AnvilInventory){
					InventoryView view = e.getView();
					int rawSlot = e.getRawSlot();
					// compare the raw slot with the inventory view to make sure we are talking about the upper inventory
					if(rawSlot == view.convertSlot(rawSlot)){
						/*
						slot 0 = left item slot
						slot 1 = right item slot
						slot 2 = result item slot
						see if the player clicked in the result item slot of the anvil inventory
						*/
						if(rawSlot == 2){
							/*
							get the current item in the result slot
							I think inv.getItem(rawSlot) would be possible too
							*/
							ItemStack item = e.getCurrentItem();
							// check if there is an item in the result slot
							if(item != null){
								ItemMeta meta = item.getItemMeta();
								// it is possible that the item does not have meta data
								if(meta != null){
									// see whether the item is being renamed
									if(meta.hasDisplayName()){
										String displayName = meta.getDisplayName();
										// do something
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public static void itemRepair(InventoryClickEvent e){
		// check whether the event has been cancelled by another plugin
		if(!e.isCancelled()){
			HumanEntity ent = e.getWhoClicked();
			// not really necessary
			if(ent instanceof Player){
				Player player = (Player)ent;
				Inventory inv = e.getInventory();
				// see if we are talking about an anvil here
				if(inv instanceof AnvilInventory){
					AnvilInventory anvil = (AnvilInventory)inv;
					InventoryView view = e.getView();
					int rawSlot = e.getRawSlot();
					// compare raw slot to the inventory view to make sure we are in the upper inventory
					if(rawSlot == view.convertSlot(rawSlot)){
						// 2 = result slot
						if(rawSlot == 2){
							// all three items in the anvil inventory
							ItemStack[] items = anvil.getContents();
							// item in the left slot
							ItemStack item1 = items[0];
							// item in the right slot
							ItemStack item2 = items[1];
							// I do not know if this is necessary
							if(item1 != null && item2 != null){
								int id1 = item1.getTypeId();
								int id2 = item2.getTypeId();
								// if the player is repairing something the ids will be the same
								if(id1 != 0 && id1 == id2){
									// item in the result slot
									ItemStack item3 = e.getCurrentItem();
									// check if there is an item in the result slot
									if(item3 != null){
										ItemMeta meta = item3.getItemMeta();
											// meta data could be null
										if(meta != null){
											// get the repairable interface to obtain the repair cost
											if(meta instanceof Repairable){
												Repairable repairable = (Repairable)meta;
												int repairCost = repairable.getRepairCost();
												// can the player afford to repair the item
												if(player.getLevel() >= repairCost){
													// success
												}else{
													// bugger
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	public void updateAllOnlineTimes() {
		for (UUID id : onlineTimeStart.keySet()) { updateOnlineTime(id, false); }
		CarbonKit.saveConfig(ConfType.DATA);
	}

	public void updateOnlineTime(UUID id, boolean save) {
		FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
		String idPath = "online-time." + id.toString() + ".", midPath = getName() + "." + idPath;
		long lastOnline = getData().getLong(idPath + "last-online", -1);
		long tmonthTime = getData().getLong(idPath + "this-month", -1);
		long lmonthTime = getData().getLong(idPath + "last-month", -1);
		long oldOverall = getData().getLong(idPath + "overall-time", -1);
		long monthlyAvg = getData().getLong(idPath + "monthly-avg", -1);
		long curSession = System.currentTimeMillis() - onlineTimeStart.get(id);
		Date lst = new Date(lastOnline), cur = new Date();
		Calendar lstCal = Calendar.getInstance(), curCal = Calendar.getInstance();
		lstCal.setTime(lst);
		curCal.setTime(cur);
		boolean newMonth = lastOnline != -1 && lstCal.get(Calendar.MONTH) != curCal.get(Calendar.MONTH);
		if (newMonth) {
			monthlyAvg = ((monthlyAvg == -1) ? (tmonthTime + curSession) : ((lmonthTime + (tmonthTime + curSession))/2));
		}
		long newMonthTime = newMonth ? curSession : (tmonthTime + curSession);
		long lastMonthTime = newMonth ? (tmonthTime + curSession) : lmonthTime;
		data.set(midPath + "overall-time", (oldOverall + curSession));
		data.set(midPath + "this-month", newMonthTime);
		data.set(midPath + "last-month", lastMonthTime);
		data.set(midPath + "last-online", System.currentTimeMillis());
		data.set(midPath + "monthly-avg", monthlyAvg);
		onlineTimeStart.put(id, System.currentTimeMillis());
		if (save) { CarbonKit.saveConfig(ConfType.DATA); }
	}

	private boolean frozenCancellableHandle(Cancellable e, Player p) {
		if (!isEnabled()) return false;
		if (isFrozen(p, false)) {
			if (MiscUtils.perm(p, "carbonkit.misc.freeze.immune")) unfreezePlayer(p);
			e.setCancelled(true);
			if (!hasFrozenEffects(p)) freezePlayer(p);
		}
		return e.isCancelled();
	}

	private boolean frozenHandle(Player p) {
		if (!isEnabled()) return false;
		if (isFrozen(p, false)) {
			if (MiscUtils.perm(p, "carbonkit.misc.freeze.immune")) unfreezePlayer(p);
			if (!hasFrozenEffects(p)) freezePlayer(p);
			return true;
		}
		return false;
	}

	private boolean frozenMoveHandle(Player p, Location from, Location to) {
		return isEnabled() && !LocUtils.isSameLoc(from, to) && frozenHandle(p);
	}

	private void joinQuitHandle(boolean join, Player p) {
		if (join) {
			if (needsUnfreezing(p)) unfreezePlayer(p);
			if (isFrozen(p, true)) {
				if (MiscUtils.perm(p, "carbonkit.misc.freeze.immune")) unfreezePlayer(p);
				else freezePlayer(p, getUnfreezeTime(p));
			}
		} else {
			if (tempFreezeList.containsKey(p.getUniqueId())) tempFreezeList.remove(p.getUniqueId());
			if (freezeList.contains(p.getUniqueId())) freezeList.remove(p.getUniqueId());
		}
		String a = join?"join":"quit";
		Location l = p.getLocation();
		String statuses = "";
		boolean silent = MiscUtils.perm(p, "carbonkit.misc.silent"+a);
		if (silent && join)
			p.sendMessage(Clr.DARKAQUA + "[Silent] " + Clr.AQUA + "You have joined silently.");
		if (silent) statuses += "[S]";
		if (isFrozen(p, !join)) statuses += "[F]";
		if (MiscUtils.checkPlugin("Essentials", true)) {
			com.earth2me.essentials.Essentials ess = (com.earth2me.essentials.Essentials)MiscUtils.getPlugin("Essentials", true);
			com.earth2me.essentials.User u = ess.getUserMap().getUser(p.getName());
			if (u != null) {
				if (u.isJailed()) statuses += "[J]";
				if (u.isMuted()) statuses += "[M]";
			}
		}
		if (statuses.length() > 0)
			statuses += " ";
		HashMap<String, String> rep = new HashMap<String, String>();
		rep.put("{STATUS}", statuses);
		rep.put("{PLAYER}", p.getName());
		rep.put("{IP}", addressMap.get(p.getUniqueId()));
		rep.put("{X}", l.getBlockX()+"");
		rep.put("{Y}", l.getBlockY()+"");
		rep.put("{Z}", l.getBlockZ()+"");
		rep.put("{WORLD}", l.getWorld().getName());
		String m = MiscUtils.massReplace(join ? CustomMessage.MISC_JOIN.noPre() : CustomMessage.MISC_QUIT.noPre(), rep);
		String me = MiscUtils.massReplace(join ? CustomMessage.MISC_JOIN_EXT.noPre() : CustomMessage.MISC_QUIT_EXT.noPre(), rep);
		for (Player opl : Bukkit.getOnlinePlayers()) {
			if (!silent || MiscUtils.perm(opl, "carbonkit.misc.silent" + a + ".notify")) {
				opl.sendMessage(MiscUtils.perm(opl, "carbonkit.misc." + a + "msg.extended") ? me : m);
			}
		}
	}

	public static boolean hasFrozenEffects(Player p) {
		return p.hasPotionEffect(PotionEffectType.JUMP) && p.getWalkSpeed() == 0.0f && p.getFlySpeed() == 0.0f;
	}

	public static void freezePlayer(OfflinePlayer p, long duration) {
		if (p.isOnline()) {
			((Player) p).setWalkSpeed(0.0f);
			((Player) p).setFlySpeed(0.0f);
			((Player) p).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
		}
		FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
		if (duration > -1) {
			MiscModule.freezeList.add(p.getUniqueId());
			data.set("Misc.temp-frozen-players." + p.getUniqueId().toString(), System.currentTimeMillis() + duration);
		} else {
			MiscModule.tempFreezeList.put(p.getUniqueId(), System.currentTimeMillis() + duration);
			MiscUtils.addToStringList(data, "Misc.frozen-players", p.getUniqueId());
		}
		CarbonKit.saveConfig(ConfType.DATA);
	}

	public static void freezePlayer(OfflinePlayer p) { freezePlayer(p, -1); }

	public static void unfreezePlayer(OfflinePlayer p) {
		FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
		if (p.isOnline()) {
			if (MiscModule.freezeList.contains(p.getUniqueId())) MiscModule.freezeList.remove(p.getUniqueId());
			if (MiscModule.tempFreezeList.containsKey(p.getUniqueId())) MiscModule.tempFreezeList.remove(p.getUniqueId());
			((Player) p).setWalkSpeed(0.2f);
			((Player) p).setFlySpeed(0.1f);
			((Player) p).removePotionEffect(PotionEffectType.JUMP);
			MiscUtils.removeFromStringList(data, "Misc.to-be-unfrozen", p.getUniqueId());
		} else {
			MiscUtils.addToStringList(data, "Misc.to-be-unfrozen", p.getUniqueId());
		}
		MiscUtils.removeFromStringList(data, "Misc.frozen-players", p.getUniqueId());
		data.set("Misc.temp-frozen-players." + p.getUniqueId().toString(), null);
		CarbonKit.saveConfig(ConfType.DATA);
	}

	public static boolean isFrozen(OfflinePlayer p, boolean checkIfOffline) {
		if (p == null) { return false; }
		FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
		String id = p.getUniqueId().toString();
		String permPath = "Misc.frozen-players", tempPath = "Misc.temp-frozen-players." + id;
		long cur = System.currentTimeMillis() / 1000L;
		return p.isOnline() && (freezeList.contains(p.getUniqueId()) || (tempFreezeList.containsKey(p.getUniqueId())
				&& tempFreezeList.get(p.getUniqueId()) > cur)) || (checkIfOffline
				&& (data.getStringList(permPath).contains(id) || (data.contains(tempPath)
				&& data.getLong(tempPath) > cur)));
	}

	public static boolean needsUnfreezing(OfflinePlayer p) {
		FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
		String tempPath = "Misc.temp-frozen-players." + p.getUniqueId().toString(), ufrzPath = "Misc.to-be-unfrozen";
		return ((data.contains(tempPath) && data.getLong(tempPath) <= System.currentTimeMillis() / 1000L)
				|| (data.getStringList(ufrzPath).contains(p.getUniqueId().toString())));
	}

	public static long getUnfreezeTime(OfflinePlayer p) {
		if (!isFrozen(p, true)) return 0;
		if (p.isOnline()){
			if (freezeList.contains(p.getUniqueId())) return -1;
			if (tempFreezeList.containsKey(p.getUniqueId())) return tempFreezeList.get(p.getUniqueId());
		} else {
			FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
			String permPath = "Misc.frozen-players", tempPath = "Misc.temp-frozen-players." + p.getUniqueId().toString();
			if (data.getStringList(permPath).contains(p.getUniqueId().toString())) return -1;
			if (data.contains(tempPath)) return data.getLong(tempPath, 0);
		}
		return 0;
	}

	private static Art getNextArt(Art curArt) { return Art.values()[(curArt.ordinal() + 1) % Art.values().length]; }
}
