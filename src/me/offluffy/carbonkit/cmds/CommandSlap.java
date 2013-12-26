package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleMisc;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSlap extends ModuleCmd {
	private ModuleMisc mMod = (ModuleMisc)mod;
	public CommandSlap(Module module) {
		super(module, "slap");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!canExec(sender, false, "")) {
			Messages.send(sender, Message.NO_PERM);
			return true;
		} else {
			if (args.length == 0) {
				sender.sendMessage(Messages.Clr.ERR + "/slap [player] [msg]");
				return true;
			} else {
				Player pl = CarbonKit.inst.getServer().getPlayer(args[0]);
				if (sender instanceof Player) {
					if (pl == (Player)sender) {
						sender.sendMessage(Messages.Clr.ERR + "You can't slap yourself!");
						return true;
					}
				}
				if (pl == null) {
					sender.sendMessage(Messages.Clr.ERR + "That player is not online");
					return true;
				} else {
					String msg = "You've been slapped by " + sender.getName() + "!";
					if (args.length > 1) {
						msg = args[1];
						for (int i = 2; i < args.length; i++)
							msg += " " + args[i];
					}
					pl.damage(CarbonKit.config.getDouble(mMod.getName() + ".slap-damage", 0.0));
					if (CarbonKit.config.getBoolean(mMod.getName() + ".knockback"))
						pl.setVelocity(pl.getVelocity().setY(pl.getVelocity().getY()+.5));
					pl.sendMessage(ChatColor.GOLD + msg);
					for (Player player : CarbonKit.inst.getServer().getOnlinePlayers())
						if (!pl.equals(player))
							player.sendMessage(ChatColor.GOLD + pl.getName() + " was slapped by " + sender.getName());
					return true;
				}
			}
		}
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.slap"));
		return perms;
	}

}
