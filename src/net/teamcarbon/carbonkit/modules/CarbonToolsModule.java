package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.CarbonTools.*;
import net.teamcarbon.carbonkit.tasks.UpdateOnlineTimeTask;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.Misc.LocUtils;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonlib.TitleUtils.TitleHelper;
import org.bukkit.*;
import org.bukkit.World.Environment;
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
import org.bukkit.event.player.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.net.InetAddress;
import java.util.*;

@SuppressWarnings("unused")
public class CarbonToolsModule extends Module {
	public static CarbonToolsModule inst;

	public static UUID instId;

	private static List<Chunk> lockedChunks;
	public static List<UUID> freezeList = new ArrayList<UUID>();
	public static List<UUID> pendingAniInfo = new ArrayList<UUID>();
	public static HashMap<UUID, Long> tempFreezeList = new HashMap<UUID, Long>();
	public static HashMap<UUID, String> addressMap = new HashMap<UUID, String>();
	public static HashMap<UUID, Long> onlineTimeStart = new HashMap<UUID, Long>();

	public CarbonToolsModule() throws DuplicateModuleException {
		super("CarbonTools", "misc", "miscmodule", "msc", "ctools", "ctool", "tools", "tool", "ctl");
	}

	public void initModule() {
		inst = this;
		lockedChunks = new ArrayList<Chunk>();
		instId = UUID.randomUUID();
		freezeList = new ArrayList<UUID>();
		if (addressMap == null) addressMap = new HashMap<UUID, String>();
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
		addCmd(new RideCommand(this));
		addCmd(new EntCountCommand(this));
		addCmd(new GamemodeCommand(this));
		addCmd(new HelpCommand(this));
		addCmd(new OnlineTimeCommand(this));
		addCmd(new TicketCommand(this));
		addCmd(new DiceCommand(this));
		addCmd(new AnimalInfoCommand(this));
		registerListeners();
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CarbonKit.inst, new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				if (isEnabled()) {
					boolean hold = getConfig().getBoolean("hud-settings.must-have-in-hand", false);
					Material LOC_ITEM = MiscUtils.getMaterial(getConfig().getString("hud-settings.location-item", "MAP")),
							DIR_ITEM = MiscUtils.getMaterial(getConfig().getString("hud-settings.direction-item", "COMPASS")),
							CLK_ITEM = MiscUtils.getMaterial(getConfig().getString("hud-settings.clock-item", "WATCH")),
							TPS_ITEM = MiscUtils.getMaterial(getConfig().getString("hud-settings.tps-item", "REDSTONE"));
					String text, sep = " &r&l\u2022&r ", pr = "hud.", bh = "bypassholding",
							prl = "location", prd = "direction", prc = "clock", prt = "tps", pro = ".override";
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						if (hold) hold = !perm(p, pr + bh);
						Material i = p.getItemInHand().getType();
						text = "";
						boolean mOvr = perm(p, pr + prl + pro), dOvr = perm(p, pr + prd + pro),
								cOvr = perm(p, pr + prc + pro), tOvr = perm(p, pr + prt + pro);
						boolean map = mOvr || ((hold ? i == LOC_ITEM : invHas(p, LOC_ITEM)) && perm(p, pr + prl)),
								cmp = dOvr || ((hold ? i == DIR_ITEM : invHas(p, DIR_ITEM)) && perm(p, pr + prd)),
								clk = cOvr || ((hold ? i == CLK_ITEM : invHas(p, CLK_ITEM)) && perm(p, pr + prc)),
								tps = tOvr || ((hold ? i == TPS_ITEM : invHas(p, TPS_ITEM)) && perm(p, pr + prt));
						if (tps) text += (!text.isEmpty() ? sep : "") + "&cTPS: {TPS}";
						if (clk && plrInOverworld(p)) text += (!text.isEmpty() ? sep : "") + "&b{TIME}";
						if (map) {
							if (!mOvr && LOC_ITEM == Material.MAP) {
								MapView m;
								if (hold) {
									m = Bukkit.getMap(p.getItemInHand().getDurability());
									map = m != null && m.getWorld().equals(p.getWorld());
								} else {
									for (ItemStack invItem : p.getInventory().getContents())
										if (invItem != null && invItem.getType() == Material.MAP) {
											m = Bukkit.getMap(invItem.getDurability());
											map = m != null && m.getWorld().equals(p.getWorld());
											break;
										}
								}
							}
							// TODO Remove world name later?
							if (mOvr || map) text += (!text.isEmpty() ? sep : "") + "&6{WORLD} {X} {Y} {Z}";
						}
						if (cmp) text += (!text.isEmpty() ? sep : "") + "&7{COMPASS_SHORT}";
						if (!text.isEmpty()) TitleHelper.sendActionBar(p, text);
						else TitleHelper.sendActionBar(p, " ");
					}
				}
			}
		}, 20L, 5L);
	}

	public void disableModule() {
		updateAllOnlineTimes();
		freezeList.clear();
		onlineTimeStart.clear();
		unregisterListeners();
		for (Player p : Bukkit.getServer().getOnlinePlayers()) TitleHelper.sendActionBar(p, " ");
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

	@EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
	public void login(AsyncPlayerPreLoginEvent e) {
		if (!isEnabled()) return;
		if (e.getLoginResult() == Result.KICK_BANNED) e.setKickMessage(banHandler(e.getKickMessage()));
		if (e.getLoginResult() == Result.ALLOWED) storeAddress(e.getUniqueId(), e.getAddress());
	}

	@EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
	public void login(PlayerLoginEvent e) {
		if (!isEnabled()) return;
		if (e.getResult() == PlayerLoginEvent.Result.KICK_BANNED) e.setKickMessage(banHandler(e.getKickMessage()));
		if (e.getResult() == PlayerLoginEvent.Result.ALLOWED) storeAddress(e.getPlayer().getUniqueId(), e.getAddress());
	}

	@EventHandler
	public void joinEvent(PlayerJoinEvent e) {
		if (!isEnabled()) return;
		storeAddress(e.getPlayer().getUniqueId(), e.getPlayer().getAddress().getAddress());
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
		updateOnlineTime(e.getPlayer().getUniqueId(), true, true);
		onlineTimeStart.remove(e.getPlayer().getUniqueId());
		e.setQuitMessage(null);
	}

	@EventHandler(ignoreCancelled = true)
	public void entityInteract(PlayerInteractEntityEvent e) {
		if (!isEnabled()) return;
		Player pl = e.getPlayer();
		UUID pid = pl.getUniqueId();
		if (frozenCancellableHandle(e, pl)) return;
		if (e.getRightClicked().getType().equals(EntityType.PAINTING) && perm(pl, "artcycle")) {
			Painting p = (Painting) e.getRightClicked();
			boolean allow = true;
			if (MiscUtils.checkPlugin("GriefPrevention", true)) {
				me.ryanhamshire.GriefPrevention.DataStore ds = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore;
				me.ryanhamshire.GriefPrevention.Claim claim = ds.getClaimAt(p.getLocation(), false, null);
				me.ryanhamshire.GriefPrevention.PlayerData pd = ds.getPlayerData(pid);
				allow = claim == null || claim.ownerID.equals(pid) || (pd != null && pd.ignoreClaims);
				if (!allow) {
					String msg = claim.allowBuild(e.getPlayer(), Material.PAINTING);
					if (msg == null) {
						allow = true;
					} else {
						e.getPlayer().sendMessage(msg);
						return;
					}
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
		if (pendingAniInfo.contains(pid)) {
			Entity ent = e.getRightClicked();
			if (ent instanceof Damageable || ent instanceof Ageable || ent instanceof Tameable) {
				AnimalInfoCommand.showInfo(pl, ent);
				pendingAniInfo.remove(pid);
			} else {
				pl.sendMessage(Clr.RED + "This is not an animal you can inspect!");
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void moveEvent(PlayerMoveEvent e) {
		if (!LocUtils.isSameBlockLoc(e.getFrom(), e.getTo()) && frozenHandle(e.getPlayer()))
			MiscUtils.teleport(e.getPlayer(), e.getFrom());
	}

	@EventHandler(ignoreCancelled = true)
	public void vehicleMoveEvent(VehicleMoveEvent e) {
		for (Entity v = e.getVehicle(); v.getPassenger() != null; v = v.getPassenger()) {
			if (v instanceof Player && isFrozen((Player) v, false)) {
				if (!LocUtils.isSameLoc(e.getFrom(), e.getTo()) && frozenHandle((Player) v)) {
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
			if (b.getType() == Material.DIAMOND_ORE) { e.blockList().remove(b); }
		}
	}

	@EventHandler
	public void chunkUnload(ChunkUnloadEvent e) {
		if (getLockedChunks() != null && isLockedChunk(e.getChunk())) {
			e.setCancelled(true);
			while (!e.getChunk().load(true));
		}
	}

	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	public void storeAddress(UUID id, InetAddress address) {
		if (address != null) {
			String addr = address.toString().replace("/", "");
			if (addr.contains(":")) addr = addr.substring(0, addr.indexOf(":"));
			addressMap.put(id, addr);
		} else {
			if (addressMap.containsKey(id)) return;
			addressMap.put(id, "X.X.X.X");
		}
	}

	public String banHandler(String kickMsg) {
		String bms = "ban-message-settings.";
		boolean r = getConfig().getBoolean(bms + "enable-replace", false),
				p = getConfig().getBoolean(bms + "enable-prefix", false),
				s = getConfig().getBoolean(bms + "enable-suffix", false);
		if (!(r || p || s)) return kickMsg;
		CarbonKit.log.debug("Enabled ban messages: " + (r ? " Replace ": "") + (p ? " Prefix " : "") + (s ? " Suffix " : ""));
		String msg = r ? Clr.trans(getConfig().getString(bms + "replace-msg", "").replace("\\n", "\n")) : kickMsg;
		if (p) msg = Clr.trans(getConfig().getString(bms + "prefix", "").replace("\\n", "\n")) + msg;
		if (s) msg += Clr.trans(getConfig().getString(bms + "suffix", "").replace("\\n", "\n"));
		if (r || p || s) CarbonKit.log.debug("Setting kick message to: " + msg);
		return msg;
	}

	public void updateAllOnlineTimes() {
		for (UUID id : onlineTimeStart.keySet()) { updateOnlineTime(id, false, false); }
		CarbonKit.saveConfig(ConfType.DATA);
	}

	public void updateOnlineTime(UUID id, boolean save, boolean quitting) {
		FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
		String idPath = "online-time." + id.toString() + ".", midPath = getName() + "." + idPath;
		long lastOnline = getData().getLong(idPath + "last-online", -1);
		long tmonthTime = getData().getLong(idPath + "this-month", -1);
		long lmonthTime = getData().getLong(idPath + "last-month", -1);
		long oldOverall = getData().getLong(idPath + "overall-time", -1);
		long monthlyAvg = getData().getLong(idPath + "monthly-avg", -1);
		long avgSession = getData().getLong(idPath + "average-session", -1);
		long curSession = 0L;
		Player pl = Bukkit.getPlayer(id);
		boolean plOnline = pl != null && pl.isOnline();
		if (plOnline) curSession = System.currentTimeMillis() - onlineTimeStart.get(id);
		Date lst = new Date(lastOnline), cur = new Date();
		Calendar lstCal = Calendar.getInstance(), curCal = Calendar.getInstance();
		lstCal.setTime(lst);
		curCal.setTime(cur);
		boolean newMonth = lastOnline != -1 && lstCal.get(Calendar.MONTH) != curCal.get(Calendar.MONTH);
		if (newMonth) monthlyAvg = ((monthlyAvg == -1) ? (tmonthTime + curSession) : ((lmonthTime + (tmonthTime + curSession))/2));
		if (quitting) avgSession = ((avgSession == -1) ? curSession : (avgSession + curSession) / 2);
		long newMonthTime = newMonth ? curSession : (tmonthTime + curSession);
		long lastMonthTime = newMonth ? (tmonthTime + curSession) : lmonthTime;
		data.set(midPath + "overall-time", (oldOverall + curSession));
		data.set(midPath + "this-month", newMonthTime);
		data.set(midPath + "last-month", lastMonthTime);
		data.set(midPath + "last-online", (pl != null && pl.isOnline())?System.currentTimeMillis():lastOnline);
		data.set(midPath + "monthly-avg", monthlyAvg);
		data.set(midPath + "average-session", avgSession);
		onlineTimeStart.put(id, System.currentTimeMillis());
		if (save) { CarbonKit.saveConfig(ConfType.DATA); }
	}

	public static boolean hasFrozenEffects(Player p) {
		return p.hasPotionEffect(PotionEffectType.JUMP) && p.getWalkSpeed() == 0.0f && p.getFlySpeed() == 0.0f;
	}

	public static void freezePlayer(OfflinePlayer p) { freezePlayer(p, -1); }

	public static void freezePlayer(OfflinePlayer p, long duration) {
		if (p.isOnline()) {
			((Player) p).setWalkSpeed(0.0f);
			((Player) p).setFlySpeed(0.0f);
			((Player) p).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
		}
		FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
		if (duration > -1) {
			CarbonToolsModule.freezeList.add(p.getUniqueId());
			data.set("CarbonTools.temp-frozen-players." + p.getUniqueId().toString(), System.currentTimeMillis() + duration);
		} else {
			CarbonToolsModule.tempFreezeList.put(p.getUniqueId(), System.currentTimeMillis() + duration);
			MiscUtils.addToStringList(data, "CarbonTools.frozen-players", p.getUniqueId());
		}
		CarbonKit.saveConfig(ConfType.DATA);
	}

	public static void unfreezePlayer(OfflinePlayer p) {
		FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
		if (p.isOnline()) {
			if (CarbonToolsModule.freezeList.contains(p.getUniqueId())) CarbonToolsModule.freezeList.remove(p.getUniqueId());
			if (CarbonToolsModule.tempFreezeList.containsKey(p.getUniqueId())) CarbonToolsModule.tempFreezeList.remove(p.getUniqueId());
			((Player) p).setWalkSpeed(0.2f);
			((Player) p).setFlySpeed(0.1f);
			((Player) p).removePotionEffect(PotionEffectType.JUMP);
			MiscUtils.removeFromStringList(data, "CarbonTools.to-be-unfrozen", p.getUniqueId());
		} else {
			MiscUtils.addToStringList(data, "CarbonTools.to-be-unfrozen", p.getUniqueId());
		}
		MiscUtils.removeFromStringList(data, "CarbonTools.frozen-players", p.getUniqueId());
		data.set("CarbonTools.temp-frozen-players." + p.getUniqueId().toString(), null);
		CarbonKit.saveConfig(ConfType.DATA);
	}

	public static boolean isFrozen(OfflinePlayer p, boolean checkIfOffline) {
		if (p == null) { return false; }
		FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
		String id = p.getUniqueId().toString();
		String permPath = "CarbonTools.frozen-players", tempPath = "CarbonTools.temp-frozen-players." + id;
		long cur = System.currentTimeMillis() / 1000L;
		return p.isOnline() && (freezeList.contains(p.getUniqueId()) || (tempFreezeList.containsKey(p.getUniqueId())
				&& tempFreezeList.get(p.getUniqueId()) > cur)) || (checkIfOffline
				&& (data.getStringList(permPath).contains(id) || (data.contains(tempPath)
				&& data.getLong(tempPath) > cur)));
	}

	public static boolean needsUnfreezing(OfflinePlayer p) {
		FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
		String tempPath = "CarbonTools.temp-frozen-players." + p.getUniqueId().toString(), ufrzPath = "CarbonTools.to-be-unfrozen";
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
			String permPath = "CarbonTools.frozen-players", tempPath = "CarbonTools.temp-frozen-players." + p.getUniqueId().toString();
			if (data.getStringList(permPath).contains(p.getUniqueId().toString())) return -1;
			if (data.contains(tempPath)) return data.getLong(tempPath, 0);
		}
		return 0;
	}

	public static void toggleLockedChunk(Chunk c) { setLockedChunk(c, !isLockedChunk(c)); }

	public static void setLockedChunk(Chunk c, boolean locked) {
		if (locked) addLockedChunk(c);
		else removeLockedChunk(c);
	}

	public static void addLockedChunk(Chunk c) {
		if (!lockedChunks.contains(c)) lockedChunks.add(c);
		while (!c.load(true));
	}

	public static void removeLockedChunk(Chunk c) {
		if (lockedChunks.contains(c)) lockedChunks.remove(c);
		boolean unload = true;
		for (Entity ent : c.getEntities()) if (ent.getType() == EntityType.PLAYER) unload = false;
		if (unload) c.unload();
	}

	public static boolean isLockedChunk(Chunk c) { return lockedChunks.contains(c); }

	public static List<Chunk> getLockedChunks() {
		if (lockedChunks == null) return null;
		return new ArrayList<Chunk>(lockedChunks);
	}

	private static Art getNextArt(Art curArt) { return Art.values()[(curArt.ordinal() + 1) % Art.values().length]; }

	private static boolean invHas(Player p, Material mat) { return p.getInventory().contains(mat); }
	private static boolean plrInOverworld(Player p) { return p.getWorld().getEnvironment() == Environment.NORMAL; }

	private boolean frozenCancellableHandle(Cancellable e, Player p) {
		if (frozenHandle(p)) { e.setCancelled(true); return true; }
		return false;
	}

	private boolean frozenHandle(Player p) {
		if (!isEnabled()) return false;
		if (isFrozen(p, false)) {
			if (perm(p, "freeze.immune")) unfreezePlayer(p);
			if (!hasFrozenEffects(p)) freezePlayer(p);
			return true;
		}
		return false;
	}

	private void joinQuitHandle(boolean join, Player p) {
		if (join) {
			if (needsUnfreezing(p)) unfreezePlayer(p);
			if (isFrozen(p, true)) {
				if (perm(p, "freeze.immune")) unfreezePlayer(p);
				else freezePlayer(p, getUnfreezeTime(p));
			}
		} else {
			if (tempFreezeList.containsKey(p.getUniqueId())) tempFreezeList.remove(p.getUniqueId());
			if (freezeList.contains(p.getUniqueId())) freezeList.remove(p.getUniqueId());
		}
		String a = join?"join":"quit";
		Location l = p.getLocation();
		String statuses = "";
		boolean silent = perm(p, "silent"+a);
		if (silent && join) {
			p.sendMessage(Clr.DARKAQUA + "[Silent] " + Clr.AQUA + "You have joined silently.");
			if (perm(p, "siletjoin.vanish") && MiscUtils.checkPlugin("Essentials", true)) {
				com.earth2me.essentials.Essentials ep = (com.earth2me.essentials.Essentials) MiscUtils.getPlugin("Essentials", true);
				com.earth2me.essentials.User user = ep.getUser(p);
				user.setVanished(true);
				p.sendMessage(Clr.DARKAQUA + "[Silent] " + Clr.AQUA + "You have been vanished");
			}
		}
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
		String m = join ? CustomMessage.MISC_JOIN.noPre(rep) : CustomMessage.MISC_QUIT.noPre(rep);
		String me = join ? CustomMessage.MISC_JOIN_EXT.noPre(rep) : CustomMessage.MISC_QUIT_EXT.noPre(rep);
		for (Player opl : Bukkit.getOnlinePlayers()) {
			if (!silent || perm(opl, "silent" + a + ".notify")) {
				opl.sendMessage(perm(opl, a + "msg.extended") ? me : m);
			}
		}
	}
}
