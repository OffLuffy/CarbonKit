package net.teamcarbon.carbonkit.commands.CarbonSkulls;

import net.milkbowl.vault.economy.EconomyResponse;
import net.teamcarbon.carbonkit.modules.CarbonSkullsModule;
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
import net.teamcarbon.carbonlib.Misc.MiscUtils;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class DupeHeadCommand extends ModuleCmd {

	public DupeHeadCommand(Module module) { super(module, "dupehead"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		if (!mod.perm(sender, "getskull")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		double price = CarbonKit.getDefConfig().getDouble("CarbonSkulls.price", 5000.0);
		if (mod.perm(sender, "skull.free") || price < 0)
			price = 0;
		if (CarbonSkullsModule.hasSavedSkull((Player) sender)) {
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
					sm.setOwner(CarbonSkullsModule.getSavedSkull((Player) sender));
					skull.setItemMeta(sm);
					((Player) sender).getInventory().addItem(skull);
					HashMap<String, String> rep = new HashMap<String, String>();
					rep.put("{SKULLOWNER}", CarbonSkullsModule.getSavedSkull((Player) sender));
					if (price <= 0.0 || mod.perm(sender, "skull.free")) {
						sender.sendMessage(CustomMessage.SS_SKULL_GIVEN_FREE.pre(rep));
					} else {
						rep.put("{PRICE}", price + "");
						sender.sendMessage(CustomMessage.SS_SKULL_GIVEN.pre(rep));
					}
				} else {
					sender.sendMessage(CustomMessage.SS_NOT_ENOUGH_MONEY.pre());
				}
			} else {
				sender.sendMessage(CustomMessage.SS_INVENTORY_FULL.pre());
			}
		} else {
			sender.sendMessage(CustomMessage.SS_NO_SKULL_SAVED.pre());
		}
	}

}
