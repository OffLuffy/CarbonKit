package net.teamcarbon.carbonkit.utils;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;

@SuppressWarnings("UnusedDeclaration")
public abstract class CBModuleCmd extends ModuleCmd {
	/**
	 * Initializes a new command that is automatically registered
	 * @param module The module which this command will pertain to
	 * @param names List of command labels to register with this command executor
	 */
	public CBModuleCmd(Module module, String... names) {
		super(module, names);
	}
	/**
	 * Prints a warning to players with permission to receive invalid command block messages
	 * @param msg The error message
	 * @param l The location of the errenous command block
	 */
	protected void cmdNotify(String msg, Location l) {
		for (OfflinePlayer p : CarbonKit.inst.getServer().getOfflinePlayers()) {
			if (p.isOnline() && MiscUtils.perm((Player) p, "cbtools.invalid-notify")) {
				((Player)p).sendMessage(Clr.GOLD + "[CBTOOLS] " + Clr.DARKRED + "Error with CmdBlock @ "
					+ Clr.RED + "x:" + l.getBlockX() + ", y:" + l.getBlockY() + ", z:" + l.getBlockZ());
				if (msg != null)
					((Player)p).sendMessage(Clr.GOLD + "[CBTOOLS] " + Clr.RED + msg);
			}
		}
	}
}
