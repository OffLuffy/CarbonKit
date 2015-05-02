package net.teamcarbon.carbonkit.commands.SkullShop;

import net.milkbowl.vault.economy.EconomyResponse;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class HeadCommand extends ModuleCmd {

	public HeadCommand(Module module) { super(module, "head"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		if (!MiscUtils.perm(sender, "carbonkit.skullshop.skull") && !MiscUtils.perm(sender, "carbonkit.skullshop.skull.free")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		double price = CarbonKit.getDefConfig().getDouble("SkullShop.buy-price", 2000.0);
		if (MiscUtils.perm(sender, "carbonkit.skullshop.skull.free") || price < 0)
			price = 0;
		if (args.length > 0) {
			String pName = args[0];
			OfflinePlayer opl = MiscUtils.getPlayer(args[0], CarbonKit.checkOffline);
			if (opl != null) pName = opl.getName();
			if (((Player)sender).getInventory().firstEmpty() > -1) {
				if (CarbonKit.econ.has((OfflinePlayer)sender, price)) {
					if (price > 0.0) {
						EconomyResponse er = CarbonKit.econ.withdrawPlayer((OfflinePlayer) sender, price);
						if (!er.transactionSuccess()) {
							sender.sendMessage(CustomMessage.SS_TRANSACTION_FAILED.pre());
							return;
						}
					}
					ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
					skull.setDurability((short) 3);
					SkullMeta sm = (SkullMeta) skull.getItemMeta();
					sm.setOwner(pName);
					skull.setItemMeta(sm);
					((Player) sender).getInventory().addItem(skull);
					HashMap<String, String> rep = new HashMap<String, String>();
					rep.put("{SKULLOWNER}", pName);
					if (price <= 0.0 || MiscUtils.perm(sender, "carbonkit.skullshop.skull.free")) {
						sender.sendMessage(MiscUtils.massReplace(CustomMessage.SS_SKULL_GIVEN_FREE.pre(), rep));
					} else {
						rep.put("{PRICE}", price + "");
						sender.sendMessage(MiscUtils.massReplace(CustomMessage.SS_SKULL_GIVEN.pre(), rep));
					}
				} else {
					sender.sendMessage(CustomMessage.SS_NOT_ENOUGH_MONEY.pre());
				}
			} else {
				sender.sendMessage(CustomMessage.SS_INVENTORY_FULL.pre());
			}
		} else {
			double p = CarbonKit.getDefConfig().getDouble("SkullShop.price", 50000);
			CustomMessage.printHeader(sender, "SkullShop");
			if (MiscUtils.perm(sender, "carbonkit.skullshop.skull.free") || price <= 0)
				sender.sendMessage(Clr.AQUA + "/skull <player>" + Clr.DARKAQUA + " - Get a player's head");
			else if (MiscUtils.perm(sender, "carbonkit.skullshop.skull"))
				sender.sendMessage(Clr.AQUA + "/skull <player>" + Clr.DARKAQUA + " - Buy a player's head" + ((price > 0)?" for $" + price:""));
			if (MiscUtils.perm(sender, "carbonkit.skullshop.getskull")) {
				if (MiscUtils.perm(sender, "carbonkit.skullshop.skull.free") || price <= 0)
					sender.sendMessage(Clr.AQUA + "/gskull" + Clr.DARKAQUA + " - Get a saved player head");
				else
					sender.sendMessage(Clr.AQUA + "/gskull" + Clr.DARKAQUA + " - Buy a saved player head" + ((price > 0)?" for $" + price:""));
			}
			if (MiscUtils.perm(sender, "carbonkit.skullshop.getskull"))
				sender.sendMessage(Clr.GRAY + "Right click a skull to check who it is and save it.");
		}
	}

}