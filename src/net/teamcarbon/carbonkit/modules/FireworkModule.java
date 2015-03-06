package net.teamcarbon.carbonkit.modules;

import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.Firework.FireworkCommand;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.FireworkPreset;
import net.teamcarbon.carbonkit.utils.FireworkUtils;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.MiscUtils;

import java.util.*;

@SuppressWarnings({"UnusedDeclaration", "deprecation"})
public class FireworkModule extends Module {
	public FireworkModule() throws DuplicateModuleException { super("Fireworks", "firework", "fw"); }
	private static List<OfflinePlayer> enabledArrowPlayers, enabledSnowballPlayers;
	private static List<Projectile> launchedProj;
	private static HashMap<OfflinePlayer, List<FireworkEffect>> arrowEffects, snowballEffects;
	public static FireworkModule inst;
	public void initModule() {
		inst = this;
		addCmd(new FireworkCommand(this));
		if (enabledArrowPlayers == null)
			enabledArrowPlayers = new ArrayList<OfflinePlayer>();
		else
			enabledArrowPlayers.clear();
		if (enabledSnowballPlayers == null)
			enabledSnowballPlayers = new ArrayList<OfflinePlayer>();
		else
			enabledSnowballPlayers.clear();
		if (launchedProj == null)
			launchedProj = new ArrayList<Projectile>();
		else
			launchedProj.clear();
		if (arrowEffects == null)
			arrowEffects = new HashMap<OfflinePlayer, List<FireworkEffect>>();
		else
			arrowEffects.clear();
		if (snowballEffects == null)
			snowballEffects = new HashMap<OfflinePlayer, List<FireworkEffect>>();
		else
			snowballEffects.clear();
		for (String s : getData().getStringList("enabled-arrows"))
			if (Bukkit.getOfflinePlayer(UUID.fromString(s)).isOnline())
				enabledArrowPlayers.add(Bukkit.getOfflinePlayer(UUID.fromString(s)));
		for (String s : getData().getStringList("enabled-snowballs"))
			if (Bukkit.getOfflinePlayer(UUID.fromString(s)).isOnline())
				enabledSnowballPlayers.add(Bukkit.getOfflinePlayer(UUID.fromString(s)));
		for (OfflinePlayer p : Bukkit.getOnlinePlayers()) {
			if (getData().contains("arrow-effects")) {
				if (getData().getConfigurationSection("arrow-effects").getKeys(false).contains(p.getUniqueId().toString())) {
					ConfigurationSection cs = getData().getConfigurationSection("arrow-effects." + p.getUniqueId());
					if (cs.contains("preset")) {
						if (MiscUtils.eq(cs.getString("preset"), "random"))
							arrowEffects.put(p, FireworkUtils.generateRandom());
						else
							arrowEffects.put(p, FireworkPreset.getPreset(cs.getString("preset")).getEffects());
					} else
						arrowEffects.put(p, FireworkUtils.loadEffects(cs));
				}
			}
			if (getData().contains("snowball-effects")) {
				if (getData().getConfigurationSection("snowball-effects").getKeys(false).contains(p.getUniqueId().toString())) {
					ConfigurationSection cs = getData().getConfigurationSection("snowball-effects." + p.getUniqueId());
					if (cs.contains("preset")) {
						if (MiscUtils.eq(cs.getString("preset"), "random"))
							snowballEffects.put(p, FireworkUtils.generateRandom());
						else
							snowballEffects.put(p, FireworkPreset.getPreset(cs.getString("preset")).getEffects());
					} else
						snowballEffects.put(p, FireworkUtils.loadEffects(cs));
				}
			}
		}
	}
	public void disableModule() {
		enabledArrowPlayers.clear();
		enabledSnowballPlayers.clear();
		launchedProj.clear();
		arrowEffects.clear();
		snowballEffects.clear();
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
	public void projLaunch(ProjectileLaunchEvent e) {
		if (!isEnabled()) return;
		if (e.getEntity().getShooter() instanceof Player) {
			OfflinePlayer shooter = (OfflinePlayer)e.getEntity().getShooter();
			if (e.getEntity() instanceof Arrow && enabledArrowPlayers.contains(shooter) && MiscUtils.perm((Player)shooter, "carbonkit.firework.use.arrows"))
				launchedProj.add(e.getEntity());
			else if (e.getEntity() instanceof Snowball && enabledSnowballPlayers.contains(shooter) && MiscUtils.perm((Player)shooter, "carbonkit.firework.use.snowballs"))
				launchedProj.add(e.getEntity());
		}
	}
	@EventHandler
	public void projHit(ProjectileHitEvent e) {
		if (!isEnabled()) return;
		if (e.getEntity() instanceof Arrow) {
			if (launchedProj.contains(e.getEntity()) && e.getEntity().getShooter() instanceof Player) {
				FireworkUtils.playFirework(e.getEntity().getLocation(), arrowEffects.get(e.getEntity().getShooter()));
				Location l = e.getEntity().getLocation();
				launchedProj.remove(e.getEntity());
			}
		} else if (e.getEntity() instanceof Snowball) {
			if (launchedProj.contains(e.getEntity()) && e.getEntity().getShooter() instanceof Player) {
				FireworkUtils.playFirework(e.getEntity().getLocation(), snowballEffects.get(e.getEntity().getShooter()));
				Location l = e.getEntity().getLocation();
				launchedProj.remove(e.getEntity());
			}
		}
	}
	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		if (!isEnabled()) return;
		if (getData().getConfigurationSection("arrow-effects").getKeys(false).contains(e.getPlayer().getUniqueId().toString())) {
			ConfigurationSection cs = getData().getConfigurationSection("arrow-effects." + e.getPlayer().getUniqueId());
			if (cs.contains("preset")) {
				if (MiscUtils.eq(cs.getString("preset"), "random"))
					arrowEffects.put(e.getPlayer(), FireworkUtils.generateRandom());
				else
					arrowEffects.put(e.getPlayer(), FireworkPreset.getPreset(cs.getString("preset")).getEffects());
			} else
				arrowEffects.put(e.getPlayer(), FireworkUtils.loadEffects(cs));
		}
		if (getData().getConfigurationSection("snowball-effects").getKeys(false).contains(e.getPlayer().getUniqueId().toString())) {
			ConfigurationSection cs = getData().getConfigurationSection("snowball-effects." + e.getPlayer().getUniqueId());
			if (cs.contains("preset")) {
				if (MiscUtils.eq(cs.getString("preset"), "random"))
					snowballEffects.put(e.getPlayer(), FireworkUtils.generateRandom());
				else
					snowballEffects.put(e.getPlayer(), FireworkPreset.getPreset(cs.getString("preset")).getEffects());
			} else
				snowballEffects.put(e.getPlayer(), FireworkUtils.loadEffects(cs));
		}
	}
	@EventHandler
	public void playerQuit(PlayerQuitEvent e) {
		if (!isEnabled()) return;
		if (arrowEffects.containsKey(e.getPlayer()))
			arrowEffects.remove(e.getPlayer());
		if (snowballEffects.containsKey(e.getPlayer()))
			snowballEffects.remove(e.getPlayer());
	}
	@EventHandler
	public void damageEvent(EntityDamageByEntityEvent e) {
		if (!isEnabled()) return;
		if (e.getDamager().getType().equals(EntityType.ARROW) || e.getDamager().getType().equals(EntityType.SNOWBALL)) {
			if (((Projectile)e.getDamager()).getShooter() instanceof Player) {
				String proj = e.getDamager().getType().equals(EntityType.ARROW)?"arrow":"snowball";
				String trgt = e.getEntity().getType().equals(EntityType.PLAYER)?"player":"mob";
				String path = String.format("extra-%s-%s-damage", proj, trgt);
				if (getConfig().getDouble(path, 0.0) > 0.0) {
					// TODO Additional permission for these?
					double ed = getConfig().getDouble(path);
					e.setDamage(e.getDamage() + ed);
					CarbonKit.log.debug("Added " + ed + " damage to damage event triggered by " + ((Player) ((Projectile) e.getDamager()).getShooter()).getName());
				}
			}
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	public static boolean hasArrowsEnabled(OfflinePlayer pl) { return enabledArrowPlayers.contains(pl); }
	public static boolean hasSnowballsEnabled(OfflinePlayer pl) { return enabledSnowballPlayers.contains(pl); }
	public static void toggleArrowsEnabled(OfflinePlayer pl) { setArrowsEnabled(pl, !hasArrowsEnabled(pl)); }
	public static void toggleSnowballsEnabled(OfflinePlayer pl) { setSnowballEnabled(pl, !hasSnowballsEnabled(pl)); }
	public static void setArrowsEnabled(OfflinePlayer pl, boolean b) {
		List<String> list = inst.getData().getStringList("enabled-arrows");
		if (b) {
			if (!hasArrowsEnabled(pl))
				enabledArrowPlayers.add(pl);
			if (!list.contains(pl.getUniqueId().toString()))
				list.add(pl.getUniqueId().toString());
		} else {
			if (hasArrowsEnabled(pl))
				enabledArrowPlayers.remove(pl);
			if (list.contains(pl.getUniqueId().toString()))
				list.remove(pl.getUniqueId().toString());
		}
		CarbonKit.getConfig(ConfType.DATA).set("Fireworks.enabled-arrows", list);
		CarbonKit.saveConfig(ConfType.DATA);
	}
	public static void setSnowballEnabled(OfflinePlayer pl, boolean b) {
		List<String> list = inst.getData().getStringList("enabled-snowballs");
		if (b) {
			if (!hasSnowballsEnabled(pl))
				enabledSnowballPlayers.add(pl);
			if (!list.contains(pl.getUniqueId().toString()))
				list.add(pl.getUniqueId().toString());
		} else {
			if (hasSnowballsEnabled(pl))
				enabledSnowballPlayers.remove(pl);
			if (list.contains(pl.getUniqueId().toString()))
				list.remove(pl.getUniqueId().toString());
		}
		CarbonKit.getConfig(ConfType.DATA).set("Fireworks.enabled-snowballs", list);
		CarbonKit.saveConfig(ConfType.DATA);
	}
	public static List<FireworkEffect> getArrowEffects(OfflinePlayer pl) {
		if (arrowEffects.containsKey(pl))
			return arrowEffects.get(pl);
		return null;
	}
	public static List<FireworkEffect> getSnowballEffects(OfflinePlayer pl) {
		if (snowballEffects.containsKey(pl))
			return snowballEffects.get(pl);
		return null;
	}
	public static void setArrowEffects(OfflinePlayer pl, List<FireworkEffect> fx) { arrowEffects.put(pl, fx); }
	public static void setSnowballEffects(OfflinePlayer pl, List<FireworkEffect> fx) { snowballEffects.put(pl, fx); }
}
