package net.teamcarbon.carbonkit.commands.Misc;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;

@SuppressWarnings("UnusedDeclaration")
public class SlapCommand extends ModuleCmd {
	public SlapCommand(Module module) { super(module, "slap"); }
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!MiscUtils.perm(sender, "carbonkit.misc.slap")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		if (args.length == 0) {
			sender.sendMessage(Clr.RED + "/slap [player] [msg]");
		} else {
			Player pl = Bukkit.getServer().getPlayer(args[0]);
			if (sender instanceof Player) {
				if (pl == sender) {
					sender.sendMessage(Clr.RED + "You can't slap yourself!");
					return;
				}
			}
			if (pl == null) {
				sender.sendMessage(Clr.RED + "That player is not online");
			} else {
				String msg = "You've been slapped by " + sender.getName() + "!";
				if (args.length > 1) {
					msg = args[1];
					for (int i = 2; i < args.length; i++)
						msg += " " + args[i];
				}
				pl.damage(CarbonKit.getDefConfig().getDouble("Misc.slap-damage", 0.0));
				if (CarbonKit.getDefConfig().getBoolean("Misc.knockback"))
					pl.setVelocity(pl.getVelocity().setY(pl.getVelocity().getY()+.5));
				pl.sendMessage(Clr.GOLD + msg);
				for (Player player : CarbonKit.inst.getServer().getOnlinePlayers())
					if (!pl.equals(player))
						player.sendMessage(Clr.GOLD + pl.getName() + " was slapped by " + sender.getName());
			}
		}
	}
}
