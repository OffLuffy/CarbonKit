package net.teamcarbon.carbonkit.commands.CarbonTools;

import net.teamcarbon.carbonkit.modules.CarbonToolsModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import org.bukkit.entity.*;

@SuppressWarnings("UnusedDeclaration")
public class AnimalInfoCommand extends ModuleCmd {

	public AnimalInfoCommand(Module module) { super(module, "animalinfo"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		if (!MiscUtils.perm(sender, "carbonkit.misc.animalinfo")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		Player p = (Player) sender;
		CarbonToolsModule.pendingAniInfo.add(p.getUniqueId());
		sender.sendMessage(Clr.AQUA + "Right click a mob to view info about it");
	}

	// Ageable: Chicken, Cow, Horse, MushroomCow, Pig, Rabbit, Sheep, Villager, Wolf
	// Tameable: Horse, Ocelot, Wolf

	public static void showInfo(Player pl, Entity ent) {
		CustomMessage.printHeader(pl, "Animal Info");
		pl.sendMessage(Clr.AQUA + "Type: " + prep(ent.getType().name()));
		if (ent instanceof Tameable) {
			Tameable te = (Tameable) ent;
			pl.sendMessage(Clr.AQUA + "Tamed: " + te.isTamed());
			if (te.isTamed()) {
				String tamer = te.getOwner().getName(), tamerId = te.getOwner().getUniqueId().toString();
				pl.sendMessage(Clr.AQUA + "Owner: " + ((tamer != null && !tamer.isEmpty()) ? tamer : "Unknown"));
				pl.sendMessage(Clr.AQUA + "Owner ID: " + ((tamer != null && !tamerId.isEmpty()) ? tamerId : "Unknown"));
			}
		}
		if (ent instanceof Ageable) {
			Ageable ae = (Ageable) ent;
			pl.sendMessage(Clr.AQUA + "Age: " + ae.getAge());
			pl.sendMessage(Clr.AQUA + "Age Locked: " + ae.getAgeLock());
		}
		if (ent instanceof Damageable) {
			Damageable de = (Damageable) ent;
			pl.sendMessage(Clr.AQUA + "Health: " + de.getHealth() + " / " + de.getMaxHealth());
		}
		if (ent instanceof Horse) {
			Horse horse = (Horse) ent;
			pl.sendMessage(Clr.AQUA + "Horse Variant: " + prep(horse.getVariant().name()));
			pl.sendMessage(Clr.AQUA + "Horse Style: " + prep(horse.getStyle().name()));
			pl.sendMessage(Clr.AQUA + "Horse Color: " + prep(horse.getColor().name()));
			pl.sendMessage(Clr.AQUA + "Horse Speed: " + getSpeed(horse));
			pl.sendMessage(Clr.AQUA + "Horse Jump Strength: " + horse.getJumpStrength());
		}
		if (ent instanceof Rabbit) {
			Rabbit rabbit = (Rabbit) ent;
			pl.sendMessage(Clr.AQUA + "Rabbit Type: " + prep(rabbit.getRabbitType().name()));
		}
		if (ent instanceof Ocelot) {
			Ocelot ocelot = (Ocelot) ent;
			pl.sendMessage(Clr.AQUA + "Cat Type: " + prep(ocelot.getCatType().name()));
		}
		CustomMessage.printFooter(pl);
	}

	private static String prep(String s) { return MiscUtils.capFirst(s.toLowerCase()).replace("_", " "); }

	// TODO Update per version change
	private static double getSpeed(Horse horse) {
		double speed = -1;
		org.bukkit.craftbukkit.v1_8_R3.entity.CraftHorse cHorse = (org.bukkit.craftbukkit.v1_8_R3.entity.CraftHorse) horse;
		net.minecraft.server.v1_8_R3.NBTTagCompound compound = new net.minecraft.server.v1_8_R3.NBTTagCompound();
		cHorse.getHandle().b(compound);
		net.minecraft.server.v1_8_R3.NBTTagList list = (net.minecraft.server.v1_8_R3.NBTTagList) compound.get("Attributes");
		for(int i = 0; i < list.size() ; i++) {
			net.minecraft.server.v1_8_R3.NBTTagCompound base = list.get(i);
			if (base.getTypeId() == 10)
				if (base.toString().contains("generic.movementSpeed"))
					speed = base.getDouble("Base");
		}
		return speed;
	}
}
