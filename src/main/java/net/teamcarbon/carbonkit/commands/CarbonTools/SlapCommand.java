package net.teamcarbon.carbonkit.commands.CarbonTools;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonkit.utils.Messages.Clr;

@SuppressWarnings("UnusedDeclaration")
public class SlapCommand extends ModuleCmd {
	public SlapCommand(Module module) { super(module, "slap"); }
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!mod.perm(sender, "slap")) {
			sender.sendMessage(mod.getCoreMsg("no-perm", false));
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
					StringBuilder sb = new StringBuilder(args[1]);
					for (int i = 2; i < args.length; i++) {
						sb.append(" ");
						sb.append(args[i]);
					}
					msg = sb.toString();
				}
				pl.damage(CarbonKit.inst.getConfig().getDouble("CarbonTools.slap-damage", 0.0));
				if (CarbonKit.inst.getConfig().getBoolean("CarbonTools.knockback"))
					pl.setVelocity(pl.getVelocity().setY(pl.getVelocity().getY()+.5));
				pl.sendMessage(Clr.GOLD + msg);
				sender.sendMessage(Clr.GOLD + "Slapped " + pl.getName());
				/*for (Player player : CarbonKit.inst.getServer().getOnlinePlayers())
					if (!pl.equals(player))
						player.sendMessage(Clr.GOLD + pl.getName() + " was slapped by " + sender.getName());*/
			}
		}
	}
}
