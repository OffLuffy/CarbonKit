package me.offluffy.carbonkit.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class ModuleCmd implements CommandExecutor {
	public static List<ModuleCmd> commands = new ArrayList<ModuleCmd>();
	public HashMap<String, List<String>> subPerms = new HashMap<String, List<String>>();
	private List<String>labels;
	protected Module mod;
	
	/**
	 * Initializes a new command that is automatically registered
	 * @param name The command label
	 * @param perms HashMap of subcommands and permission nodes without the 'carbonkit.' portion
	 */
	public ModuleCmd(Module module, String ... names) {
		labels = new ArrayList<String>();
		for (String name : names) {
			CarbonKit.inst.getCommand(name).setExecutor(this);
			labels.add(name);
		}
		this.subPerms = populatePerms();
		this.mod = module;
		commands.add(this);
	}
	
	/**
	 * @return Returns the command's label
	 */
	public List<String> getLabels() {
		return labels;
	}
	
	/**
	 * Sets the commands map of sub permissions
	 * @param perms The HashMap of String and List<String> that dictates permissions
	 */
	public void setPerms(HashMap<String, List<String>> perms) {
		this.subPerms = perms;
	}
	
	/**
	 * @return Returns the HashMap of subcommands (String), and List<String> of permission nodes (excluding 'carbonkit.' portion)
	 */
	public HashMap<String, List<String>> getPerms() {
		return subPerms;
	}
	
	protected abstract HashMap<String, List<String>> populatePerms();
	
	/**
	 * Checks if a sender can use a sub command by checking the ModuleCmd's in-built subPerms list
	 * @param sender The sender to check
	 * @param requireAll True to require all listed permissions or false for any single permission
	 * @param subCommand The sub command to check
	 * @return Returns true if the CommandSender has all the necessary permissions to execute the sub command
	 */
	public boolean canExec(CommandSender sender, boolean requireAll, String subCommand) {
		if (!subPerms.containsKey(subCommand))
			return false;
		else {
			List<String> perms = subPerms.get(subCommand);
			for (String p : perms) {
				if (requireAll) {
					if (!CarbonKit.perms.has(sender, p))
						return false;
				} else {
					if (CarbonKit.perms.has(sender, p))
						return true;
				}
			}
			return requireAll;
		}
	}
	
	// XXX Check if this is right
	/**
	 * Check if the sender has permission to execute any or all sub commands
	 * @param sender The sender to check
	 * @param requireAllPerms Whether or not to require all or any listed perms for each sub command
	 * @param requireAllCmds Whether or not to require all or any listed sub commands
	 * @param subCommands The list of sub commands to check
	 * @return Returns true if the sender can execute the sub command(s) based on permissions
	 */
	public boolean canExec(CommandSender sender, boolean requireAllPerms, boolean requireAllCmds, String ... subCommands) {
		for (String subCommand : subCommands) {
			if (subPerms.containsKey(subCommand)) {
				List<String> perms = subPerms.get(subCommand);
				for (String p : perms) {
					if (requireAllPerms) {
						if (!CarbonKit.perms.has(sender, p))
							if (requireAllCmds)
								return false;
					} else {
						if (CarbonKit.perms.has(sender, p))
							if (!requireAllCmds)
								return true;
					}
				}
			} else if (requireAllCmds)
				return false;
		}
		return requireAllCmds;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof ModuleCmd)) return false;
		ModuleCmd mc = (ModuleCmd)obj;
		
		return new EqualsBuilder()
			.append(labels, mc.getLabels())
			.append(subPerms, mc.getPerms())
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(labels)
			.toHashCode();
	}
	
	protected void cmdNotify(String msg, Location l) {
		for (Player p : CarbonKit.inst.getServer().getOnlinePlayers()) {
			if (CarbonKit.perms.has(p, "cbtools.invalid-notify")) {
				p.sendMessage(Messages.Clr.PRE + "[CBTOOLS] " + Messages.Clr.ERR + "Error with CmdBlock @ "
					+ Messages.Clr.HEAD + "x:" + l.getBlockX() + ", y:" + l.getBlockY() + ", z:" + l.getBlockZ());
				if (msg != null)
					p.sendMessage(Messages.Clr.PRE + "[CBTOOLS] " + Messages.Clr.ERR + msg);
			}
		}
	}
	
	protected List<String> toList(String ... perms) {
		List<String> list = new ArrayList<String>();
		for (String p : perms)
			if (!list.contains(p.toLowerCase()))
				list.add(p.toLowerCase());
		return list;
	}
}
