package net.teamcarbon.carbonkit.modules;

import com.google.gson.Gson;
import net.minecraft.server.v1_11_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_11_R1.PacketPlayOutChat;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.CarbonNews.CarbonNewsCommand;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.tasks.BroadcastTask;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

@SuppressWarnings("UnusedDeclaration")
public class CarbonNewsModule extends Module {
	public static CarbonNewsModule inst;
	public static final Gson gson = new Gson();

	public static final String NAME = "CarbonNews";

	public CarbonNewsModule() throws DuplicateModuleException {
		super(CarbonKit.inst, NAME, "cnews", "news", "cn");
		reqVer = "1_11_R1";
	}
	public void initModule() {
		inst = this;

		ConfigurationSection setDefaults = CarbonKit.getConfig(ConfType.NEWS).getConfigurationSection("setDefaults");
		setDefaults.set("setEnabled", false);
		setDefaults.set("messages", new ArrayList<String>());

		Set<String> keys = CarbonKit.getConfig(ConfType.NEWS).getConfigurationSection("MessageSets").getKeys(false);
		for (String k : keys) {
			BroadcastTask bt = new BroadcastTask(k);
			if (bt.isEnabled()) bt.startBroadcasts();
		}
		addCmd(new CarbonNewsCommand(this));
		registerListeners();
	}
	public void disableModule() {
		unregisterListeners();
		BroadcastTask.disableAllTasks();
		BroadcastTask.removeAllTasks();
	}
	public void reloadModule() {
		disableModule();
		initModule();
	}
	protected boolean needsListeners() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler
	public void loginEvent(final PlayerJoinEvent e) {
		if (!isEnabled()) return;
		boolean np = !e.getPlayer().hasPlayedBefore();
		ConfigurationSection cs = CarbonKit.getConfig(ConfType.NEWS).getConfigurationSection("welcomeMessage." + (np ? "newPlayer" : "returnPlayer"));
		if (cs.getBoolean("enabled", false)) {
			final boolean rp = cs.getBoolean("requirePermission", false);
			final List<String> msgs = cs.getStringList("messageLines");
			Bukkit.getScheduler().scheduleSyncDelayedTask(CarbonKit.inst, new Runnable() {
				public void run() {
					for (String msg : msgs) { CarbonNewsModule.sendFormatted(e.getPlayer(), msg, rp, "welcome"); }
				}
			}, cs.getLong("delaySeconds", 2L) * 20L);
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	public static void broadcastFormatted(String msg, boolean needsPerm, String ... perms) {
		for (Player pl : Bukkit.getOnlinePlayers()) { sendFormatted(pl, msg, needsPerm, perms); }
	}

	public static void sendFormatted(CommandSender cs, String msg, boolean needsPerm, String ... perms) {
		PacketPlayOutChat cp = null;
		try {
			cp = new PacketPlayOutChat(ChatSerializer.a(msg));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!needsPerm || inst.perm(cs, perms)) {
			if (cp != null) {
				((CraftPlayer) cs).getHandle().playerConnection.sendPacket(cp);
			} else { cs.sendMessage(msg); }
		}
	}

	public static void deleteSet(String setName) {
		if (CarbonKit.getConfig(ConfType.NEWS).contains("MessageSets." + setName))
			CarbonKit.getConfig(ConfType.NEWS).set("MessageSets." + setName, null);
		CarbonKit.saveConfig(ConfType.NEWS);
		BroadcastTask.removeTask(setName);
	}

	/**
	 * Converts the array of JSON formatted text objects to a JSON formatted text array
	 * @param jsonObjects A String JSON text format object
	 * @return Returns a JSON array with all JSON objects as array entries
	 */
	public static String toFormatArray(String ... jsonObjects) {
		if (jsonObjects.length < 1) return "{\"text\":\"\"}";
		String array = "[{\"text\":\"\",\"extra\":" + jsonObjects[0] + "}";
		for (int i = 1; i < jsonObjects.length; i++) {
			String jo = jsonObjects[i];
			if (jo != null && !jo.isEmpty()) {
				array += ",{\"text\":\"\",\"extra\":" + jo + "}";
			}
		}
		array += "]";
		return array;
	}
}
