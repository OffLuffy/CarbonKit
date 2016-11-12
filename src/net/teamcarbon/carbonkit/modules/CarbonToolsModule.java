package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.CarbonTools.*;
import net.teamcarbon.carbonkit.tasks.UpdateOnlineTimeTask;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.UserStore;
import net.teamcarbon.carbonlib.Misc.LocUtils;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonlib.Misc.MiscUtils.TrustLevel;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
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

import java.net.InetAddress;
import java.util.*;

@SuppressWarnings("unused")
public class CarbonToolsModule extends Module {
	public static CarbonToolsModule inst;

	public static UUID instId;

	public static List<Player> pendingAniInfo = new ArrayList<>();
	public static HashMap<UUID, String> addressMap = new HashMap<>();

	public CarbonToolsModule() throws DuplicateModuleException {
		super("CarbonTools", "misc", "miscmodule", "msc", "ctools", "ctool", "tools", "tool", "ctl");
	}

	public void initModule() {
		inst = this;
		instId = UUID.randomUUID();
		if (addressMap == null) addressMap = new HashMap<>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (needsUnfreezing(p)) unfreezePlayer(p);
			if (isFrozen(p, true)) freezePlayer(p, getUnfreezeTime(p));
			if (p.getAddress() != null) {
				String addr = p.getAddress().toString().replace("/", "");
				addr = addr.substring(0, addr.indexOf(":"));
				addressMap.put(p.getUniqueId(), addr);
			} else { addressMap.put(p.getUniqueId(), "X.X.X.X"); }
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(CarbonKit.inst, new UpdateOnlineTimeTask(instId), 1L, 6000L);
		addCmd(new SlapCommand(this));
		addCmd(new FakeJoinCommand(this));
		addCmd(new FakeQuitCommand(this));
		addCmd(new RideCommand(this));
		addCmd(new EntCountCommand(this));
		addCmd(new GamemodeCommand(this));
		addCmd(new HelpCommand(this));
		addCmd(new OnlineTimeCommand(this));
		addCmd(new TicketCommand(this));
		addCmd(new DiceCommand(this));
		addCmd(new AnimalInfoCommand(this));
		addCmd(new CalcCommand(this));
		registerListeners();
		/*Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CarbonKit.inst, new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				if (isEnabled()) {
					boolean hold = getConfig().getBoolean("hud-settings.must-have-in-hand", false);
					Material LOC_ITEM = MiscUtils.getMaterial(getConfig().getString("hud-settings.location-item", "MAP")),
							DIR_ITEM = MiscUtils.getMaterial(getConfig().getString("hud-settings.direction-item", "COMPASS")),
							CLK_ITEM = MiscUtils.getMaterial(getConfig().getString("hud-settings.clock-item", "WATCH")),
							TPS_ITEM = MiscUtils.getMaterial(getConfig().getString("hud-settings.tps-item", "REDSTONE"));
					String text, sep = " &r&l\u2022&r ", pr = "hud.",
							prl = "location", prd = "direction", prc = "clock", prt = "tps", pro = ".override";
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						if (hold) hold = !perm(p, pr + "bypassholding");
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
							if (mOvr || map) text += (!text.isEmpty() ? sep : "") + "&6{X} {Y} {Z}";
						}
						if (cmp) text += (!text.isEmpty() ? sep : "") + "&7{COMPASS_SHORT}";
						if (!text.isEmpty()) TitleHelper.sendActionBar(p, text);
						else TitleHelper.sendActionBar(p, " ");
					}
				}
			}
		}, 20L, 5L);*/
	}

	public void disableModule() {
		updateAllOnlineTimes();
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
		joinQuitHandle(true, e.getPlayer());
		e.setJoinMessage(null);
	}

	@EventHandler
	public void quitEvent(PlayerQuitEvent e) {
		if (pendingAniInfo.contains(e.getPlayer())) pendingAniInfo.remove(e.getPlayer());
		if (!isEnabled()) return;
		joinQuitHandle(false, e.getPlayer());
		if (addressMap.containsKey(e.getPlayer().getUniqueId()))
			addressMap.remove(e.getPlayer().getUniqueId());

		updateOnlineTime(e.getPlayer().getUniqueId(), true);

		e.setQuitMessage(null);
	}

	@EventHandler(ignoreCancelled = true)
	public void entityInteract(PlayerInteractEntityEvent e) {
		if (!isEnabled()) return;
		if (isFrozen(e.getPlayer(), false)) { e.setCancelled(true); return; }
		Player pl = e.getPlayer();
		UUID pid = pl.getUniqueId();

		// Art cycle
		if (e.getRightClicked().getType().equals(EntityType.PAINTING) && perm(pl, "artcycle")) {
			Painting p = (Painting) e.getRightClicked();
			boolean allow = true;
			if (!MiscUtils.accessCheck(e.getRightClicked().getLocation(), pl, TrustLevel.BUILD)) allow = false;
			if (allow) {
				boolean s = false;
				Art last = p.getArt();
				for (int c = 0; !s && c < Art.values().length; c++) {
					last = getNextArt(last);
					s = p.setArt(last);
				}
			}
		}

		// Animal Info
		if (pendingAniInfo.contains(pl)) {
			Entity ent = e.getRightClicked();
			if (ent instanceof Damageable || ent instanceof Ageable || ent instanceof Tameable) {
				showInfo(pl, ent);
				pendingAniInfo.remove(pl);
			} else {
				pl.sendMessage(Clr.RED + "This is not an animal you can inspect!");
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void moveEvent(PlayerMoveEvent e) {
		if (!isEnabled()) return;
		if (isFrozen(e.getPlayer(), false)) {
			// TODO Allow looking but not moving? Place on ground to prevent kicking for flight?
			UserStore us = CarbonKit.getPlayerData(e.getPlayer().getUniqueId());
			Location loc = LocUtils.fromStr(us.getString(inst.getName() + ".freeze-location", ""));
			if (!LocUtils.isSameLoc(e.getTo(), loc)) {
				e.getPlayer().teleport(loc);
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void itemDrop(PlayerDropItemEvent e) {
		if (!isEnabled()) return;
		if (isFrozen(e.getPlayer(), false)) e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void breakEvent(BlockBreakEvent e) {
		if (!isEnabled()) return;
		if (isFrozen(e.getPlayer(), false)) e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void placeEvent(BlockPlaceEvent e) {
		if (!isEnabled()) return;
		if (isFrozen(e.getPlayer(), false)) e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void damageEvent(BlockDamageEvent e) {
		if (!isEnabled()) return;
		if (isFrozen(e.getPlayer(), false)) e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void hangingPlace(HangingPlaceEvent e) {
		if (!isEnabled()) return;
		if (isFrozen(e.getPlayer(), false)) e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void hangingBreak(HangingBreakByEntityEvent e) {
		Entity r = e.getRemover();
		Player p;
		if (e.getRemover() instanceof Player) {
			p = (Player) e.getRemover();
		} else if (e.getRemover() instanceof Projectile && ((Projectile) e.getRemover()).getShooter() instanceof Player) {
			p = (Player) ((Projectile) e.getRemover()).getShooter();
		} else return;
		if (isFrozen(p, false)) e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void explosion(EntityExplodeEvent e) {
		if (!isEnabled()) return;
		for (Block b : new ArrayList<>(e.blockList())) {
			if (b.getType() == Material.DIAMOND_ORE) { e.blockList().remove(b); }
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

	public static void updateAllOnlineTimes() {
		for (Player pl : Bukkit.getOnlinePlayers()) { updateOnlineTime(pl.getUniqueId(), false); }
	}

	public static void updateOnlineTime(UUID id, boolean quitting) {
		Player pl = Bukkit.getPlayer(id);
		UserStore us = CarbonKit.getPlayerData(id);

		long lastOnline = pl.getLastPlayed();// us.getLong(inst.getName() + ".online-time.last-online",		-1L);
		if (pl.isOnline()) lastOnline = System.currentTimeMillis();
		if (lastOnline == 0L) lastOnline = -1L;
		long oldOverall = us.getLong(inst.getName() + ".online-time.overall-time",		-1L);
		long tmonthTime = us.getLong(inst.getName() + ".online-time.this-month",		-1L);
		long monthlyCnt = us.getLong(inst.getName() + ".online-time.month-count",		-1L);
		long monthlyAvg = us.getLong(inst.getName() + ".online-time.monthly-avg",		-1L);
		long sessionCnt = us.getLong(inst.getName() + ".online-time.session-count",		-1L);
		long avgSession = us.getLong(inst.getName() + ".online-time.average-session",	-1L);

		Date lst = new Date(lastOnline), cur = new Date();
		Calendar lstCal = Calendar.getInstance(), curCal = Calendar.getInstance();
		lstCal.setTime(lst);
		curCal.setTime(cur);

		boolean plOnline = pl != null && pl.isOnline();
		long curSession = 0L;
		if (plOnline) curSession = System.currentTimeMillis() - us.getLong(inst.getName() + ".online-time.last-online", System.currentTimeMillis());
		boolean newMonth = lastOnline != -1L && lstCal.get(Calendar.MONTH) != curCal.get(Calendar.MONTH);
		tmonthTime = newMonth ? curSession : tmonthTime + curSession;

		if (newMonth) {
			monthlyAvg = monthlyAvg == -1L ? tmonthTime : (monthlyAvg * monthlyCnt + tmonthTime) / (monthlyCnt+1);
			monthlyCnt++;
		}
		if (quitting) {
			avgSession = avgSession == -1L ? curSession : (avgSession * sessionCnt + curSession) / (sessionCnt+1);
			sessionCnt++;
		}

		us.set(inst.getName() + ".online-time.overall-time", oldOverall + curSession);
		us.set(inst.getName() + ".online-time.this-month", tmonthTime);
		us.set(inst.getName() + ".online-time.month-count", monthlyCnt);
		us.set(inst.getName() + ".online-time.monthly-avg", monthlyAvg);
		us.set(inst.getName() + ".online-time.session-count", sessionCnt);
		us.set(inst.getName() + ".online-time.average-session", avgSession);
		us.save();
	}

	public static void freezePlayer(OfflinePlayer p) { freezePlayer(p, -1); }

	public static void freezePlayer(OfflinePlayer p, long duration) {
		UserStore us = CarbonKit.getPlayerData(p.getUniqueId());
		us.set(inst.getName() + ".frozen", true);
		us.set(inst.getName() + ".freeze-expire", duration == -1L ? -1L : System.currentTimeMillis() + duration);
		us.set(inst.getName() + ".freeze-location", LocUtils.toStr(p.getPlayer().getLocation(), false));
		us.save();
	}

	public static void unfreezePlayer(OfflinePlayer p) {
		UserStore us = CarbonKit.getPlayerData(p.getUniqueId());
		us.set(inst.getName() + ".frozen", false);
		us.set(inst.getName() + ".freeze-expire", -1L);
		us.set(inst.getName() + ".freeze-location", "");
		us.save();
	}

	public static boolean isFrozen(OfflinePlayer p, boolean checkIfOffline) {
		if (p == null || (!checkIfOffline && !p.isOnline())) { return false; }
		UserStore us = CarbonKit.getPlayerData(p.getUniqueId());
		if (inst.perm(p, "freeze.immune")) return false;
		boolean frozen = us.getBoolean(inst.getName() + ".frozen", false);
		long freezeExpire = us.getLong(inst.getName() + ".freeze-expire", -1L);
		return frozen && (freezeExpire == -1L || freezeExpire < System.currentTimeMillis());
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
			//if (freezeList.contains(p.getUniqueId())) return -1;
			//if (tempFreezeList.containsKey(p.getUniqueId())) return tempFreezeList.get(p.getUniqueId());
		} else {
			FileConfiguration data = CarbonKit.getConfig(ConfType.DATA);
			String permPath = "CarbonTools.frozen-players", tempPath = "CarbonTools.temp-frozen-players." + p.getUniqueId().toString();
			if (data.getStringList(permPath).contains(p.getUniqueId().toString())) return -1;
			if (data.contains(tempPath)) return data.getLong(tempPath, 0);
		}
		return 0;
	}

	// Ageable: Chicken, Cow, Horse, MushroomCow, Pig, Rabbit, Sheep, Villager, Wolf
	// Tameable: Horse, Ocelot, Wolf

	public static void showInfo(Player pl, Entity ent) {
		CustomMessage.printHeader(pl, "Animal Info");
		pl.sendMessage(Clr.AQUA + "Type: " + prep(ent.getType().name()));
		if (ent instanceof Tameable) {
			Tameable te = (Tameable) ent;
			pl.sendMessage(Clr.AQUA + "Tamed: " + te.isTamed());
			if (te.isTamed()) {
				String tamer = te.getOwner().getName(), tamerId = te.getOwner().getUniqueId().toString();
				pl.sendMessage(Clr.AQUA + "Owner: " + ((tamer != null && !tamer.isEmpty()) ? tamer : "Unknown"));
				pl.sendMessage(Clr.AQUA + "Owner ID: " + ((tamer != null && !tamerId.isEmpty()) ? tamerId : "Unknown"));
			}
		}
		if (ent instanceof Ageable) {
			Ageable ae = (Ageable) ent;
			pl.sendMessage(Clr.AQUA + "Age: " + ae.getAge());
			pl.sendMessage(Clr.AQUA + "Age Locked: " + ae.getAgeLock());
		}
		if (ent instanceof Damageable) {
			Damageable de = (Damageable) ent;
			pl.sendMessage(Clr.AQUA + "Health: " + de.getHealth() + " / " + de.getMaxHealth());
		}
		if (ent instanceof Horse) {
			Horse horse = (Horse) ent;
			pl.sendMessage(Clr.AQUA + "Horse Variant: " + prep(horse.getVariant().name()));
			pl.sendMessage(Clr.AQUA + "Horse Style: " + prep(horse.getStyle().name()));
			pl.sendMessage(Clr.AQUA + "Horse Color: " + prep(horse.getColor().name()));
			pl.sendMessage(Clr.AQUA + "Horse Speed: " + getSpeed(horse));
			pl.sendMessage(Clr.AQUA + "Horse Jump Strength: " + horse.getJumpStrength());
		}
		if (ent instanceof Rabbit) {
			Rabbit rabbit = (Rabbit) ent;
			pl.sendMessage(Clr.AQUA + "Rabbit Type: " + prep(rabbit.getRabbitType().name()));
		}
		if (ent instanceof Ocelot) {
			Ocelot ocelot = (Ocelot) ent;
			pl.sendMessage(Clr.AQUA + "Cat Type: " + prep(ocelot.getCatType().name()));
		}
		CustomMessage.printFooter(pl);
	}

	private static String prep(String s) { return MiscUtils.capFirst(s, true).replace("_", " "); }

	// TODO Update per version change
	private static double getSpeed(Horse horse) {
		double speed = -1;
		org.bukkit.craftbukkit.v1_10_R1.entity.CraftHorse cHorse = (org.bukkit.craftbukkit.v1_10_R1.entity.CraftHorse) horse;
		net.minecraft.server.v1_10_R1.NBTTagCompound compound = new net.minecraft.server.v1_10_R1.NBTTagCompound();
		cHorse.getHandle().b(compound);
		net.minecraft.server.v1_10_R1.NBTTagList list = (net.minecraft.server.v1_10_R1.NBTTagList) compound.get("Attributes");
		for(int i = 0; i < list.size() ; i++) {
			net.minecraft.server.v1_10_R1.NBTTagCompound base = list.get(i);
			if (base.getTypeId() == 10)
				if (base.toString().contains("generic.movementSpeed"))
					speed = base.getDouble("Base");
		}
		return speed;
	}

	private static Art getNextArt(Art curArt) { return Art.values()[(curArt.ordinal() + 1) % Art.values().length]; }

	private static boolean invHas(Player p, Material mat) { return p.getInventory().contains(mat); }
	private static boolean plrInOverworld(Player p) { return p.getWorld().getEnvironment() == Environment.NORMAL; }

	private void joinQuitHandle(boolean join, Player p) {
		if (join) {
			if (needsUnfreezing(p)) unfreezePlayer(p);
			if (isFrozen(p, true)) {
				if (perm(p, "freeze.immune")) unfreezePlayer(p);
				else freezePlayer(p, getUnfreezeTime(p));
			}
		} else {
			//if (tempFreezeList.containsKey(p.getUniqueId())) tempFreezeList.remove(p.getUniqueId());
			//if (freezeList.contains(p.getUniqueId())) freezeList.remove(p.getUniqueId());
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
		HashMap<String, String> rep = new HashMap<>();
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
