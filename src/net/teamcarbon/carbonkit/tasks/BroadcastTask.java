package net.teamcarbon.carbonkit.tasks;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.modules.CarbonNewsModule;
import net.teamcarbon.carbonlib.Misc.CarbonException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@SuppressWarnings("unused")
public class BroadcastTask extends BukkitRunnable {

	private static List<BroadcastTask> tasks = new ArrayList<BroadcastTask>();

	private List<String> normalMsgList, randomMsgList;
	private String setName, pre, pst;
	private int position = 0;
	private boolean enabled, random, perm, console, players, consoleClr;

	public BroadcastTask(String set) {
		this.setName = set;
		loadSet(true);
		tasks.add(this);
	}

	@Override
	public void run() {
		if (enabled) {
			if (isEmpty()) {
				setEnabled(false);
				stopBroadcasts();
			} else {
				if (position >= randomMsgList.size() || position < 0)
					position = 0;
				if (position == 0 && random)
					Collections.shuffle(randomMsgList, new Random(System.nanoTime()));
				String msg = randomMsgList.get(position);
				msg = pre + msg + pst;
				Collection<? extends Player> plArray = Bukkit.getOnlinePlayers();
				int i = 0;
				CarbonNewsModule.broadcastFormatted(msg, plArray, isSentToPlayers(), isSentToConole(), isConsoleColorized(), perm, getPerm());
				position++;
				if (position >= randomMsgList.size())
					position = 0;
			}
		} else {
			stopBroadcasts();
		}
	}

	public void startBroadcasts() {
		try {
			runTaskTimer(CarbonKit.inst, getDelay() * 20L, getDelay() * 20L);
			CarbonKit.log.debug("Started broadcast task for set: " + getSetName());
			enabled = true;
		} catch (Exception e) {
			CarbonKit.log.debug("Broadcast task already started for set: " + getSetName());
		}
	}
	public void stopBroadcasts() {
		try { cancel(); } catch (Exception e) {}
		enabled = false;
		CarbonKit.log.debug("Stopped broadcast task for set: " + getSetName());
	}
	public void restartBroadcasts() {
		try {
			stopBroadcasts();
			removeTask(this);
			BroadcastTask t = new BroadcastTask(getSetName());
			t.startBroadcasts();
			enabled = true;
			CarbonKit.log.debug("Restarted broadcast task for set: " + getSetName());
		} catch (Exception e) {
			(new CarbonException(CarbonKit.inst, e)).printStackTrace();
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		CarbonKit.getConfig(ConfType.NEWS).set(path("setEnabled"), enabled);
		CarbonKit.saveConfig(ConfType.NEWS);
		if (enabled) startBroadcasts(); else stopBroadcasts();
		CarbonKit.log.debug("Set broadcast task " + (enabled ? "enabled" : "disabled") + " for set: " + getSetName());
	}
	public void setRandom(boolean random) {
		if (random != this.random) {
			this.random = random;
			position = 0;
			randomMsgList.clear();
			randomMsgList = new ArrayList<String>(normalMsgList);
			if (random) Collections.shuffle(randomMsgList);
			CarbonKit.getConfig(ConfType.NEWS).set(path("randomOrder"), random);
			CarbonKit.saveConfig(ConfType.NEWS);
			restartTask();
		}
		CarbonKit.log.debug("Set broadcast task random order " + (random ? "enabled" : "disabled") + " for set: " + getSetName());
	}
	public void setDelay(long delay) {
		restartTask();
		CarbonKit.getConfig(ConfType.NEWS).set(path("delaySeconds"), delay);
		CarbonKit.saveConfig(ConfType.NEWS);
		CarbonKit.log.debug("Set broadcast task delay to " + delay + " seconds for set: " + getSetName());
	}
	public void setRequirePerms(boolean requirePerms) {
		this.perm = requirePerms;
		CarbonKit.getConfig(ConfType.NEWS).set(path("requirePermission"), perm);
		CarbonKit.saveConfig(ConfType.NEWS);
		CarbonKit.log.debug("Set broadcast task require perm " + (perm ? "enabled" : "disabled") + " for set: " + getSetName());
	}
	public void setPrefix(String prefix) {
		pre = prefix;
		CarbonKit.getConfig(ConfType.NEWS).set(path("prefix"), prefix);
		CarbonKit.saveConfig(ConfType.NEWS);
		CarbonKit.log.debug("Set broadcast task prefix to " + prefix + " for set: " + getSetName());
	}
	public void setPostfix(String postfix) {
		pst = postfix;
		CarbonKit.getConfig(ConfType.NEWS).set(path("postfix"), postfix);
		CarbonKit.saveConfig(ConfType.NEWS);
		CarbonKit.log.debug("Set broadcast task postfix to " + postfix + " for set: " + getSetName());
	}
	public void setSendToConsole(boolean stc) {
		console = stc;
		CarbonKit.getConfig(ConfType.NEWS).set(path("sendToConsole"), stc);
		CarbonKit.saveConfig(ConfType.NEWS);
		CarbonKit.log.debug("Set broadcast task send console " + (console ? "enabled" : "disabled") + " for set: " + getSetName());
	}
	public void setColorConsole(boolean cc) {
		consoleClr = cc;
		CarbonKit.getConfig(ConfType.NEWS).set(path("colorConsoleMessages"), cc);
		CarbonKit.saveConfig(ConfType.NEWS);
		CarbonKit.log.debug("Set broadcast task color console " + (consoleClr ? "enabled" : "disabled") + " for set: " + getSetName());
	}
	public void setSendToPlayer(boolean stp) {
		players = stp;
		CarbonKit.getConfig(ConfType.NEWS).set(path("sendToPlayers"), stp);
		CarbonKit.saveConfig(ConfType.NEWS);
		CarbonKit.log.debug("Set broadcast task send player " + (players ? "enabled" : "disabled") + " for set: " + getSetName());
	}
	public void setMessages(List<String> messages) {
		normalMsgList.clear();
		normalMsgList = new ArrayList<String>(messages);
		position = 0;
		if (random) {
			randomMsgList.clear();
			randomMsgList = new ArrayList<String>(normalMsgList);
			Collections.shuffle(randomMsgList);
		}
		CarbonKit.getConfig(ConfType.NEWS).set(path("messages"), normalMsgList);
		CarbonKit.saveConfig(ConfType.NEWS);
	}
	public void addMessage(String msg) {
		List<String> om = getMessages();
		om.add(msg);
		setMessages(om);
	}
	public void removeMessage(int msgId) {
		List<String> om = getMessages();
		om.remove(msgId);
		setMessages(om);
	}
	public void updateMessage(int msgId, String msg) {
		List<String> om = getMessages();
		om.set(msgId, msg);
		setMessages(om);
	}

	public String getSetName() {
		return setName;
	}
	public String getPerm() { return "carbonkit.news.receive." + setName; }
	public List<String> getMessages() {
		if (normalMsgList == null)
			return new ArrayList<String>();
		return new ArrayList<String>(normalMsgList);
	}
	public String getMessage(int msgId) {
		if (msgId > size())
			return null;
		return (normalMsgList.get(msgId));
	}
	public boolean isEnabled() { return enabled; }
	public boolean isRandom() { return random; }
	public boolean isConsoleColorized() { return consoleClr; }
	public boolean requirePerms() { return perm; }
	public boolean isSentToConole() { return console; }
	public boolean isSentToPlayers() { return players; }
	public boolean isEmpty() { return normalMsgList.isEmpty(); }
	public int size() { return normalMsgList.size(); }
	public long getDelay() {
		ConfigurationSection setConf = CarbonKit.getConfig(ConfType.NEWS).getConfigurationSection(path());
		long defDelay = CarbonKit.getConfig(ConfType.NEWS).getLong("setDefault.delaySeconds", 60L);
		return setConf.getLong("delaySeconds", defDelay);
	}

	public String getPrefix() { return pre; }
	public String getPostfix() { return pst; }
	private String path() { return "MessageSets." + setName; }
	private String path(String postPath) { return "MessageSets." + setName + "." + postPath; }

	private void loadSet(boolean allowCreate) {
		if (CarbonKit.getConfig(ConfType.NEWS).contains(path())) {
			ConfigurationSection setConf = CarbonKit.getConfig(ConfType.NEWS).getConfigurationSection(path());
			ConfigurationSection setDefaults = CarbonKit.getConfig(ConfType.NEWS).getConfigurationSection("setDefaults");
			enabled = setConf.getBoolean("setEnabled", false);
			normalMsgList = new ArrayList<String>(setConf.getStringList("messages"));
			random = setConf.getBoolean(("randomOrder"), setDefaults.getBoolean("randomOrder", false));
			perm = setConf.getBoolean(("requirePermission"), setDefaults.getBoolean("requirePermission", false));
			console = setConf.getBoolean(("sendToConsole"), setDefaults.getBoolean("sendToConsole", true));
			players = setConf.getBoolean(("sendToPlayers"), setDefaults.getBoolean("sendToPlayers", true));
			consoleClr = setConf.getBoolean(("colorConsoleMessages"), setDefaults.getBoolean("colorConsoleMessages", true));
			pre = setConf.getString(("prefix"), setDefaults.getString("prefix", "{TTP:News!|TXT:&b&l[!]} &f"));
			pst = setConf.getString(("postfix"), setDefaults.getString("postfix", ""));
			if (normalMsgList == null)
				normalMsgList = new ArrayList<String>();
			if (size() < 1) {
				enabled = false;
				CarbonKit.log.warn("Message list for set " + setName + " is empty, disabling it");
			}
			randomMsgList = new ArrayList<String>(normalMsgList);
		} else if (allowCreate) {
			CarbonKit.log.warn("Failed to find settings for message set '" + setName + "', creating it...");
			CarbonKit.getConfig(ConfType.NEWS).set("MessageSets." + setName, CarbonKit.getConfig(ConfType.NEWS).getConfigurationSection("setDefaults"));
			CarbonKit.saveConfig(ConfType.NEWS);
			CarbonKit.log.info("Created message set '" + setName + "' with default values (setName disabled by default, no messages)");
			loadSet(false); // Reload it now that default values has been added to the set, shouldn't cause stack overflow...
		}
	}

	public static void disableAllTasks() { for (BroadcastTask task : tasks) task.stopBroadcasts(); }

	public static void startAllTasks() { for (BroadcastTask task : tasks) task.startBroadcasts(); }

	public static BroadcastTask getTask(String setName) {
		for (BroadcastTask bt : tasks)
			if (bt.getSetName().equalsIgnoreCase(setName))
				return bt;
		return null;
	}

	public static boolean isTask(String setName) { return getTask(setName) != null; }

	public static List<BroadcastTask> getTasks() { return new ArrayList<BroadcastTask>(tasks); }

	public static void removeTask(BroadcastTask task) {
		task.stopBroadcasts();
		if (tasks.contains(task)) tasks.remove(task);
	}

	public static void removeAllTasks() { for (BroadcastTask task : getTasks()) { removeTask(task); } }

	public static void removeTask(String name) { if (getTask(name) != null) removeTask(getTask(name)); }

	public static int taskListSize() { return tasks.size(); }

	private void restartTask() {
		if (isEnabled()) restartBroadcasts();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof BroadcastTask)) return false;
		BroadcastTask bt = (BroadcastTask)obj;
		return new EqualsBuilder()
				.append(bt.getSetName(), getSetName())
				.isEquals();
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getSetName())
				.toHashCode();
	}
}
