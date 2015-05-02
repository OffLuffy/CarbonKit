package net.teamcarbon.carbonkit.utils.Firework;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import org.bukkit.FireworkEffect;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonlib.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("UnusedDeclaration")
public class FireworkPreset {
	private static List<FireworkPreset> presets = new ArrayList<FireworkPreset>();
	private String presetName;
	private List<FireworkEffect> effects;
	private UUID ownerID;

	public FireworkPreset(String name, List<FireworkEffect> fx, UUID owner) {
		presetName = name;
		effects = fx;
		ownerID = owner;
		presets.add(this);
		FireworkUtils.saveFireworkToPath(CarbonKit.getDefConfig(), "Fireworks.presets." + presetName, effects, ownerID);
		CarbonKit.inst.saveConfig();
	}
	public FireworkPreset(String name, List<FireworkEffect> fx, Player owner) {
		presetName = name;
		effects = fx;
		ownerID = owner.getUniqueId();
		presets.add(this);
		FireworkUtils.saveFireworkToPath(CarbonKit.getDefConfig(), "Fireworks.presets." + presetName, effects, ownerID);
		CarbonKit.inst.saveConfig();
	}
	public FireworkPreset(String name, List<FireworkEffect> fx, OfflinePlayer owner) {
		presetName = name;
		effects = fx;
		ownerID = owner.getUniqueId();
		presets.add(this);
		FireworkUtils.saveFireworkToPath(CarbonKit.getDefConfig(), "Fireworks.presets." + presetName, effects, ownerID);
		CarbonKit.inst.saveConfig();
	}

	public String getName() { return presetName; }
	public List<FireworkEffect> getEffects() { return effects; }
	public UUID getOwnerID() { return ownerID; }
	public static List<FireworkPreset> getPresets() { return new ArrayList<FireworkPreset>(presets); }

	public void setName(String newName) {
		String oldName = presetName;
		presetName = newName;
		CarbonKit.getDefConfig().set("Fireworks.presets." + presetName, CarbonKit.getDefConfig().getConfigurationSection("Fireworks.presets." + oldName));
		CarbonKit.getDefConfig().set("Fireworks.presets." + oldName, null);
		CarbonKit.inst.saveConfig();
	}
	public void setEffects(List<FireworkEffect> fx) {
		effects = fx;
		FireworkUtils.saveFireworkToPath(CarbonKit.getDefConfig(), "Fireworks.presets." + presetName, effects, ownerID);
		CarbonKit.inst.saveConfig();
	}
	public void addEffects(List<FireworkEffect> fx) {
		effects.addAll(fx);
		FireworkUtils.saveFireworkToPath(CarbonKit.getDefConfig(), "Fireworks.presets." + presetName, effects, ownerID);
		CarbonKit.inst.saveConfig();
	}
	public void setOwnerID(UUID id) {
		ownerID = id;
		CarbonKit.getDefConfig().set("Fireworks.presets." + presetName + ".owner", ownerID.toString());
	}
	public void setOwner(Player player) {
		ownerID = player.getUniqueId();
		CarbonKit.getDefConfig().set("Fireworks.presets." + presetName + ".owner", ownerID.toString());
	}
	public void setOwner(OfflinePlayer player) {
		ownerID = player.getUniqueId();
		CarbonKit.getDefConfig().set("Fireworks.presets." + presetName + ".owner", ownerID.toString());
	}

	public static FireworkPreset getPreset(String name) {
		for (FireworkPreset fp : presets)
			if (MiscUtils.eq(name, fp.getName()))
				return fp;
		return null;
	}

	public static String getPresetName(String name) {
		return getPreset(name) == null ? null : getPreset(name).getName();
	}

	/**
	 * Sets the list of firework effects for the given player UUID
	 * @param effects The FireworkEffects to set
	 */
	public static void setPlayerEffects(UUID id, List<FireworkEffect> effects) {
		FileConfiguration fc = CarbonKit.getConfig(ConfType.DATA);
		CarbonKit.getConfig(ConfType.DATA).set("Fireworks.effects." + id, null);
		FireworkUtils.saveFireworkToPath(fc, "Fireworks.effects." + id, effects, null);
		CarbonKit.saveConfig(ConfType.DATA);
	}

	/**
	 * Sets the player's effect to a preset if the preset exists
	 * @param id The UUID of the player
	 * @param preset The name of the preset
	 */
	public void setPlayerPreset(UUID id, String preset) {
		ConfigurationSection cs = CarbonKit.getDefConfig().getConfigurationSection("Fireworks.presets");
		for (String s : cs.getKeys(false)) {
			if (MiscUtils.eq(s, preset)) {
				CarbonKit.getConfig(ConfType.DATA).set("Fireworks.effects." + id, null);
				CarbonKit.getConfig(ConfType.DATA).set("Fireworks.effects." + id + ".preset", s);
				CarbonKit.saveConfig(ConfType.DATA);
			}
		}
		CarbonKit.inst.saveConfig();
	}

	/**
	 * Sets the List<FireworkEffect> as the effects for the given preset. The preset will be updated or created accordingly
	 * @param preset The name of the preset to update or create
	 * @param effects The List of FireworkEffects to bind to this preset name
	 */
	public static void setPreset(String preset, List<FireworkEffect> effects, UUID owner) {
		FireworkPreset fp = null;
		for (FireworkPreset pre : presets)
			if (MiscUtils.eq(preset, pre.getName()))
				fp = pre;
		if (fp != null) {
			fp.setEffects(effects);
			if (owner == null)
				owner = fp.getOwnerID();
			FireworkUtils.saveFireworkToPath(CarbonKit.getDefConfig(), "Fireworks.preset." + fp.getName(), effects, owner);
			CarbonKit.inst.saveConfig();
		}
	}

	/**
	 * Remove the specified preset from the config
	 * @param preset The preset to remove
	 * @return Returns true if the preset was removed successfully, false otherwise (if it can't be found)
	 */
	public static boolean deletePreset(String preset) {
		FireworkPreset fp = null;
		for (FireworkPreset pre : presets)
			if (MiscUtils.eq(preset, pre.getName()))
				fp = pre;
		if (fp != null) {
			presets.remove(fp);
			if (CarbonKit.getDefConfig().contains("Fireworks.presets." + fp.getName())) {
				CarbonKit.getDefConfig().set("Fireworks.presets." + fp.getName(), null);
				CarbonKit.inst.saveConfig();
			}
			return true;
		}
		return false;
	}

	/**
	 * Clears cached presets and reloads them from config
	 */
	public static void reloadPresets() {
		presets.clear();
		for (String s : CarbonKit.getDefConfig().getConfigurationSection("Fireworks.presets").getKeys(false)) {
			List<FireworkEffect> effects = FireworkUtils.loadEffects(CarbonKit.getDefConfig(), "Fireworks.presets." + s);
			UUID oid = null;
			if (CarbonKit.getDefConfig().contains("Fireworks.presets." + s + ".owner"))
				oid = UUID.fromString(CarbonKit.getDefConfig().getString("Fireworks.presets." + s + ".owner"));
			new FireworkPreset(s, effects, oid);
		}
	}
}
