package net.teamcarbon.carbonkit.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonlib.Misc.CarbonException;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@SuppressWarnings("UnusedDeclaration")
public abstract class ModuleCmd implements CommandExecutor {
	public static List<ModuleCmd> commands = new ArrayList<>();
	private List<String>labels;
	protected Module mod;
	/**
	 * Initializes a new command that is automatically registered
	 * @param module The module which this command will pertain to
	 * @param names List of command labels to register with this command executor
	 */
	public ModuleCmd(Module module, String ... names) {
		labels = new ArrayList<>();
		Collections.addAll(labels, names);
		this.mod = module;
		commands.add(this);
		for (String n : names) {
			if (Bukkit.getPluginCommand(n) == null) {
				(new CarbonException(CarbonKit.inst(), "Failed to fetch command during ModuleCmd init for command: " + n)).printStackTrace();
				return;
			}
			Bukkit.getPluginCommand(n).setExecutor(this);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!getMod().isEnabled()) { sender.sendMessage(Clr.RED + "This module is disabled!"); return true; }
		execModCmd(sender, cmd, label, args);
		return true;
	}

	/**
	 * This method executes if the module is enabled, otherwise sends a dsiabled message and prevents command execution
	 * @param sender The CommandSender representing whom sent the command
	 * @param cmd The Command
	 * @param label The String label of the command
	 * @param args The arguments supplied to the command
	 */
	public abstract void execModCmd(CommandSender sender, Command cmd, String label, String[] args);

	/**
	 * @return Returns the command's labels
	 */
	public List<String> getLabels() { return labels; }
	/**
	 * @return Returns the module that registered this command
	 */
	public Module getMod() { return mod; }
	/**
	 * Converts a list of Strings to an Array&gt;String&lt; of Strings, intended for permission nodes
	 * @param perms The array of permission nodes as Strings
	 * @return Returns a List&lt;String&gt; of String permission nodes
	 */
	protected List<String> toList(String ... perms) {
		List<String> list = new ArrayList<>();
		for (String p : perms)
			if (!list.contains(p.toLowerCase()))
				list.add(p.toLowerCase());
		return list;
	}

	/**
	 * Converts the provided arguments into a String of space-delimited arguments
	 * @param args The arguments to stringify
	 * @return Returns a string of the arguments in a space-delimited format
	 */
	protected String stringifyArgs(String[] args) { return stringifyArgs(args, 0, args.length-1); }
	/**
	 * Converts the provided arguments into a String of space-delimited arguments after the 'from' index, inclusive.
	 * @param args The arguments to stringify
	 * @return Returns a string of the arguments in a space-delimited format
	 */
	protected String stringifyArgs(String[] args, int from) { return stringifyArgs(args, from, args.length-1); }
	/**
	 * Converts the provided arguments into a String of space-delimited arguments between the 'from' and 'to' indices, inclusive.
	 * @param args The arguments to stringify
	 * @return Returns a string of the arguments in a space-delimited format
	 */
	protected String stringifyArgs(String[] args, int from, int to) {
		if (args.length-1 < from)
			return null;
		if (to > args.length-1)
			to = args.length-1;
		String sa = args[from];
		for (int i = from+1; i <= to; i++)
			sa += " " + args[i];
		return sa;
	}
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof ModuleCmd)) return false;
		ModuleCmd mc = (ModuleCmd)obj;
		return new EqualsBuilder().append(labels, mc.getLabels()).isEquals();
	}
	public int hashCode() { return new HashCodeBuilder().append(labels).toHashCode(); }
}
