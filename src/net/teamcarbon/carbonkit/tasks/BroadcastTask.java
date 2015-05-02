package net.teamcarbon.carbonkit.tasks;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.modules.CarbonNewsModule;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unused")
public class BroadcastTask extends BukkitRunnable {

	private List<String> normalMsgList, randomMsgList;
	private String setName, pre, pst;
	private int position = 0;
	private boolean enabled, random, perm, console, players, consoleClr;
	private ConfigurationSection setConf;

	public BroadcastTask(String set) {
		this.setName = set;
		setConf = CarbonKit.getConfig(ConfType.NEWS).getConfigurationSection(path());
		loadSet(true);
	}

	@Override
	public void run() {
		if (enabled) {
			if (isEmpty()) {
				setEnabled(false);
			} else {
				if (position >= randomMsgList.size() || position < 0)
					position = 0;
				if (position == 0 && random)
					Collections.shuffle(randomMsgList, new Random(System.nanoTime()));
				String msg = randomMsgList.get(position);
				msg = pre + msg + pst;
				Player[] plArray = new Player[Bukkit.getOnlinePlayers().size()];
				int i = 0;
				for (Player pl : Bukkit.getOnlinePlayers()) { plArray[i] = pl; i++; }
				CarbonNewsModule.broadcastFormatted(msg, plArray, perm, getPerm());
				position++;
				if (position >= randomMsgList.size())
					position = 0;
			}
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		CarbonKit.getConfig(ConfType.NEWS).set(path("setEnabled"), enabled);
		CarbonKit.saveConfig(ConfType.NEWS);
		if (!Bukkit.getScheduler().isQueued(getTaskId()))
			runTaskTimer(CarbonKit.inst, getDelay(), getDelay());
	}
	public void setRandom(boolean random) {
		if (random != this.random) {
			this.random = random;
			position = 0;
			randomMsgList.clear();
			randomMsgList = new ArrayList<String>(normalMsgList);
			if (random)
				Collections.shuffle(randomMsgList);
			CarbonKit.getConfig(ConfType.NEWS).set(path("randomOrder"), random);
			CarbonKit.saveConfig(ConfType.NEWS);
		}
	}
	public void setDelay(long delay) {
		if (enabled) {
			if (Bukkit.getScheduler().isCurrentlyRunning(getTaskId()))
				cancel();
			runTaskTimer(CarbonKit.inst, delay * 20L, delay * 20L);
		}
		CarbonKit.getConfig(ConfType.NEWS).set(path("delaySeconds"), delay);
		CarbonKit.saveConfig(ConfType.NEWS);
	}
	public void setRequirePerms(boolean requirePerms) {
		this.perm = requirePerms;
		CarbonKit.getConfig(ConfType.NEWS).set(path("requirePermission"), perm);
		CarbonKit.saveConfig(ConfType.NEWS);
	}
	public void setPrefix(String prefix) {
		pre = prefix;
		CarbonKit.getConfig(ConfType.NEWS).set(path("prefix"), prefix);
		CarbonKit.saveConfig(ConfType.NEWS);
	}
	public void setPostfix(String postfix) {
		pst = postfix;
		CarbonKit.getConfig(ConfType.NEWS).set(path("postfix"), postfix);
		CarbonKit.saveConfig(ConfType.NEWS);
	}
	public void setSendToConsole(boolean stc) {
		console = stc;
		CarbonKit.getConfig(ConfType.NEWS).set(path("sendToConsole"), stc);
		CarbonKit.saveConfig(ConfType.NEWS);
	}
	public void setColorConsole(boolean cc) {
		consoleClr = cc;
		CarbonKit.getConfig(ConfType.NEWS).set(path("colorConsoleMessages"), cc);
		CarbonKit.saveConfig(ConfType.NEWS);
	}
	public void setSendToPlayer(boolean stp) {
		players = stp;
		CarbonKit.getConfig(ConfType.NEWS).set(path("sendToPlayers"), stp);
		CarbonKit.saveConfig(ConfType.NEWS);
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
		return CarbonKit.getConfig(ConfType.NEWS).getLong(path("delaySeconds"), CarbonKit.getConfig(ConfType.NEWS).getLong("setDefaults.delaySeconds", 60L));
	}
	public String getPrefix() { return pre; }
	public String getPostfix() { return pst; }
	private String path() { return "MessageSets." + setName; }
	private String path(String postPath) { return "MessageSets." + setName + "." + postPath; }

	private void loadSet(boolean allowCreate) {
		if (CarbonKit.getConfig(ConfType.NEWS).contains(path())) {
			enabled = setConf.getBoolean(path("setEnabled"), false);
			normalMsgList = new ArrayList<String>(setConf.getStringList("messages"));
			random = setConf.getBoolean(("randomOrder"), CarbonNewsModule.setDefaults.getBoolean("randomOrder", false));
			perm = setConf.getBoolean(("requirePermission"), CarbonNewsModule.setDefaults.getBoolean("requirePermission", false));
			console = setConf.getBoolean(("sendToConsole"), CarbonNewsModule.setDefaults.getBoolean("sendToConsole", true));
			players = setConf.getBoolean(("sendToPlayers"), CarbonNewsModule.setDefaults.getBoolean("sendToPlayers", true));
			consoleClr = setConf.getBoolean(("colorConsoleMessages"), CarbonNewsModule.setDefaults.getBoolean("colorConsoleMessages", true));
			pre = setConf.getString(("prefix"), CarbonNewsModule.setDefaults.getString("prefix", "{TTP:News!|TXT:&b&l[!]} &f"));
			pst = setConf.getString(("postfix"), CarbonNewsModule.setDefaults.getString("postfix", ""));
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
