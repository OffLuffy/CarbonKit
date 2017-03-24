package net.teamcarbon.carbonkit.utils;

import net.teamcarbon.carbonkit.utils.CmdMask.MaskType;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


@SuppressWarnings("UnusedDeclaration")
public abstract class MaskedModuleCmd extends ModuleCmd {

	private HashMap<String, ArrayList<CmdMask>> namedMasks = new HashMap<>();

	/**
	 * Initializes a new command that is automatically registered
	 * @param module The module which this command will pertain to
	 * @param names List of command labels to register with this command executor
	 */
	public MaskedModuleCmd(Module module, String ... names) {
		super(module, names);
		addMask("default");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!getMod().isEnabled()) { sender.sendMessage(Clr.RED + "This module is disabled!"); return true; }
		for (String maskName : namedMasks.keySet()) {
			for (CmdMask cm : namedMasks.get(maskName)) {
				if (cm.matches(args)) {
					execModCmd(sender, cmd, label, args, maskName);
				}
			}
		}
		return true;
	}

	public void addMask(String title, MaskType[] ... masks) {
		namedMasks.put(title, new ArrayList<CmdMask>());
		for (MaskType[] mta : masks) {
			if (mta.length > 0) {
				CmdMask mask = new CmdMask(mta);
				namedMasks.get(title).add(mask);
			}
		}
	}

	/**
	 * This method executes if the module is enabled, otherwise sends a dsiabled message and prevents command execution
	 * @param sender The CommandSender representing whom sent the command
	 * @param cmd The Command
	 * @param label The String label of the command
	 * @param args The arguments supplied to the command
	 */
	public abstract void execModCmd(CommandSender sender, Command cmd, String label, String[] args, String maskName);
}
