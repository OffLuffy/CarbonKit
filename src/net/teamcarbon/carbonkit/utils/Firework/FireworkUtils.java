package net.teamcarbon.carbonkit.utils.Firework;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonlib.Misc.CarbonException;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonlib.Misc.NumUtils;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * firework() method is modified content written by codename_b
 * @author Luffy, codename_b
 */
@SuppressWarnings("UnusedDeclaration")
public final class FireworkUtils {

	/**
	 * The minimum amount of each color that will be used in random color generation for randomly generated effects.
	 * This limits how dark a firework color can be when randomly generated
	 */
	private static final int RANDBRIGHT = 64;

	/*
	EXAMPLE FIREWORK STRUCTURE:
      effect-0:
        colors:
          - 0x00000000
        fades:
          - 0x00000000
        shape: 'ball' (ball, ball_large, burst, star, creeper)
        trail: false
        flicker: false
      effect-1:
        colors:
          - 0x00000000
        fades:
          - 0x00000000
        shape: 'ball'
        trail: false
        flicker: false
	 */

	/**
	 * Loads a list of FireworkEffects from a FileConfiguration
	 * @param fc The FileConfiguration to load from
	 * @param path The path to load from
	 * @return Returns a List&lt;FireworkEffect&gt; object loaded
	 */
	public static List<FireworkEffect> loadEffects(FileConfiguration fc, String path) {
		return loadEffects(fc.getConfigurationSection(path));
	}

	/**
	 * Loads a list of FireworkEffects from a FileConfiguration
	 * @param cs The ConfigurationSection to load from
	 * @return Returns a List&lt;FireworkEffect&gt; object loaded
	 */
	public static List<FireworkEffect> loadEffects(ConfigurationSection cs) {
		List<FireworkEffect> effects = new ArrayList<FireworkEffect>();
		for (String key : cs.getKeys(false)) {
			String en = key + ".";
			Builder feb = FireworkEffect.builder();

			feb.with(Type.valueOf(cs.getString(en + "type", "ball").toUpperCase()));
			if (cs.getBoolean(en + "trail", false))
				feb.withTrail();
			if (cs.getBoolean(en + "flicker", false))
				feb.withFlicker();

			if (cs.contains(en + "colors") && cs.getStringList(en + "colors").size() > 0) {
				List<Color> colors = new ArrayList<Color>();
				for (String color : cs.getStringList(en + "colors"))
					colors.add(colorFromString(color));
				feb.withColor(colors);
			}

			if (cs.contains(en + "fades") && cs.getStringList(en + "fades").size() > 0) {
				List<Color> fades = new ArrayList<Color>();
				for (String fade : cs.getStringList(en + "fades"))
					fades.add(colorFromString(fade));
				feb.withColor(fades);
			}
			effects.add(feb.build());
		}
		return effects;
	}

	/**
	 * Generates a random list of effects
	 * @return Returns a List&lt;FireworkEffect&gt; of randomly generated effects
	 */
	public static List<FireworkEffect> generateRandom() {
		List<FireworkEffect> effects = new ArrayList<FireworkEffect>();
		Random r = new Random(System.currentTimeMillis());
		for (int i = 0; i < 2+r.nextInt(4); i++) {
			Type type = Type.values()[r.nextInt(4)];
			boolean flicker = r.nextBoolean(), trail = r.nextBoolean();
			List<Color> colors = new ArrayList<Color>();
			List<Color> fades = new ArrayList<Color>();
			for (int j = 0; j < 3+r.nextInt(5); j++)
				colors.add(Color.fromRGB(r.nextInt(255-RANDBRIGHT)+RANDBRIGHT, r.nextInt(255-RANDBRIGHT)+RANDBRIGHT, r.nextInt(255-RANDBRIGHT)+RANDBRIGHT));
			for (int j = 0; j < 3+r.nextInt(5); j++)
				fades.add(Color.fromRGB(r.nextInt(255-RANDBRIGHT)+RANDBRIGHT, r.nextInt(255-RANDBRIGHT)+RANDBRIGHT, r.nextInt(255-RANDBRIGHT)+RANDBRIGHT));
			Builder feb = FireworkEffect.builder();
			feb.with(type);
			if (flicker) feb.withFlicker();
			if (trail) feb.withTrail();
			feb.withColor(colors);
			feb.withFade(fades);
			effects.add(feb.build());
		}
		return effects;
	}

	/**
	 * Parse single or multiple firework effects from command arguments
	 * @param args The entire argument string consisting of firework properties to be parsed
	 * @return Returns a List&lt;FireworkEffect&gt; indicating what effects were parsed from the arguments
	 */
	public static List<FireworkEffect> fromArgs(String args) {
		List<FireworkEffect> effects = new ArrayList<FireworkEffect>();
		if (FireworkPreset.getPreset(args) != null)
			return FireworkPreset.getPreset(args).getEffects();
		if (MiscUtils.eq(args, "random", "rand"))
			return FireworkUtils.generateRandom();
		if (args.contains("{") && args.contains("}")) {
			Pattern pat = Pattern.compile("\\{.*\\}");
			Matcher mat = pat.matcher(args);
			while (mat.find())
				effects.add(parseEffect(mat.group().replace("{","").replace("}","").split(" ")));
		} else
			effects.add(parseEffect(args.split(" ")));
		return effects;
	}

	/**
	 * Play a firework at the location with the given FireworkEffect
	 * @param loc The location to create the firework effect
	 * @param fe The effects to reproduce
	 */
	public static void playFirework(Location loc, List<FireworkEffect> fe) { firework(loc, true, 0, fe); }

	/**
	 * Play a firework at the location with the given FireworkEffect
	 * @param loc The location to create the firework effect
	 * @param fe The effects to reproduce
	 */
	public static void playFirework(Location loc, FireworkEffect ... fe) {
		List<FireworkEffect> lfe = new ArrayList<FireworkEffect>();
		Collections.addAll(lfe, fe);
		firework(loc, true, 0, lfe);
	}

	/**
	 * Play a firework at the location with the given FireworkEffect and power
	 * @param loc The location to create the firework effect
	 * @param fe The effect to reproduce
	 * @param power The power to apply to the firework (must be between 1 and 4, inclusive)
	 */
	public static void launchFirework(Location loc, int power, List<FireworkEffect> fe) { firework(loc, false, power, fe); }

	/**
	 * Saves the given effects in the location specified
	 * @param fc The The FileConfiguration to save to
	 * @param path The path to save to
	 * @param effects The effects to save
	 */
	public static void saveFireworkToPath(FileConfiguration fc, String path, List<FireworkEffect> effects, UUID owner) {
		for (int i = 0; i < effects.size(); i++) {
			String en = path + ".effect-" + i + ".";
			FireworkEffect e = effects.get(i);

			List<String> colors = new ArrayList<String>();
			for (Color c : e.getColors())
				colors.add("0x" + Integer.toHexString(c.asRGB()));
			fc.set(en + "colors", colors);

			List<String> fades = new ArrayList<String>();
			for (Color c : e.getFadeColors())
				fades.add("0x" + Integer.toHexString(c.asRGB()));
			fc.set(en + "fades", fades);

			fc.set(en + "shape", e.getType().name().toLowerCase());
			fc.set(en + "trail", e.hasTrail());
			fc.set(en + "flicker", e.hasFlicker());

			if (owner != null)
				fc.set(en + "owner", owner.toString());
		}
	}

	/**
	 * Attempts to translate textual firework parameters to a FireworkEffect
	 * @param args The parameters to parse
	 * @return The FireworkEffect the parameters build
	 */
	public static FireworkEffect parseEffect(String[] args) {
		boolean setType = false, setColor = false;
		Builder feb = FireworkEffect.builder();
		for (String s : args) {
			s = s.toLowerCase();
			if (s.startsWith("s:")) {
				String type = s.replace("s:","");
				Type t = Type.BALL;
				try {
					t = Type.valueOf(type.toUpperCase());
				} catch (Exception e) {
					if (MiscUtils.eq(type, "large", "largeball", "balllarge", "firecharge")) t = Type.BALL_LARGE;
					if (MiscUtils.eq(type, "star")) t = Type.STAR;
					if (MiscUtils.eq(type, "face", "mob")) t = Type.CREEPER;
					// TODO Add aliases for other firework shapes?
				}
				if (t != null) t = Type.BALL;
				feb.with(t);
				setType = true;
			} else if (s.startsWith("c:")) {
				List<Color> colors = new ArrayList<Color>();
				String[] colorStrings = s.replace("c:", "").split(",");
				for (String cs : colorStrings)
					colors.add(colorFromString(cs));
				feb.withColor(colors);
				setColor = true;
			} else if (s.startsWith("f:")) {
				List<Color> fades = new ArrayList<Color>();
				String[] fadeStrings = s.replace("f:", "").split(",");
				for (String cs : fadeStrings)
					fades.add(colorFromString(cs));
				feb.withFade(fades);
			} else if (s.startsWith("-")) {
				if (s.contains("t"))
					feb.withTrail();
				if (s.contains("f"))
					feb.withFlicker();
			}
		}
		if (!setType)
			feb.with(Type.BALL);
		if (!setColor)
			feb.withColor(Color.WHITE);
		return feb.build();
	}

	/**
	 * Attempts to parse a Bukkit Color object from a string
	 * @param c The hex code or color name
	 * @return The Color object, or white if not a parseable color
	 */
	public static Color colorFromString(String c) {
		Color color;
		if (colorFromHex(c) != null) return colorFromHex(c);
		for (DyeColor dc : DyeColor.values())
			if (MiscUtils.eq(dc.name(), c))
				return dc.getFireworkColor();
		c = c.replace("_", "").replace("-", "");
		if (MiscUtils.eq(c, "rosered", "poppy", "rose", "rosebush", "redtulip")) {
			color = DyeColor.RED.getFireworkColor();
		} else if (MiscUtils.eq(c, "gold", "orangetulip")) {
			color = DyeColor.ORANGE.getFireworkColor();
		} else if (MiscUtils.eq(c, "dandelionyellow", "dandelion", "sunflower")) {
			color = DyeColor.YELLOW.getFireworkColor();
		} else if (MiscUtils.eq(c, "cactusgreen", "cactus")) {
			color = DyeColor.GREEN.getFireworkColor();
		} else if (MiscUtils.eq(c, "lapislazuli", "lapis", "navy")) {
			color = DyeColor.BLUE.getFireworkColor();
		} else if (MiscUtils.eq(c, "aqua", "brightblue", "blueorchid", "orchid")) {
			color = DyeColor.LIGHT_BLUE.getFireworkColor();
		} else if (MiscUtils.eq(c, "fuschia", "lilac", "allium")) {
			color = DyeColor.MAGENTA.getFireworkColor();
		} else if (MiscUtils.eq(c, "lightred", "brightred", "peony", "pinktulip")) {
			color = DyeColor.PINK.getFireworkColor();
		} else if (MiscUtils.eq(c, "bonemeal", "bone")) {
			color = DyeColor.WHITE.getFireworkColor();
		} else if (MiscUtils.eq(c, "lightgray", "lightgrey", "brightgray", "brightgrey", "azurebluet", "azure", "bluet", "oxeyedaisy", "oxeye", "daisy", "whitetulip")) {
			color = DyeColor.SILVER.getFireworkColor();
		} else if (MiscUtils.eq(c, "ink", "inksac", "squid")) {
			color = DyeColor.BLACK.getFireworkColor();
		} else if (MiscUtils.eq(c, "maroon", "cocoa", "cocoaplant")) {
			color = DyeColor.BROWN.getFireworkColor();
		} else if (MiscUtils.eq(c, "teal", "bluegreen", "greenblue")) {
			color = DyeColor.CYAN.getFireworkColor();
		} else if (MiscUtils.eq(c, "violet", "redblue", "bluered")) {
			color = DyeColor.PURPLE.getFireworkColor();
		} else if (MiscUtils.eq(c, "grey", "darkgray", "darkgrey")) {
			color = DyeColor.GRAY.getFireworkColor();
		} else if (MiscUtils.eq(c, "lightgreen", "brightgreen")) {
			color = DyeColor.LIME.getFireworkColor();
		} else {
			color = DyeColor.WHITE.getFireworkColor();
		}
		return color;
	}


	/**
	 * Attempts to parse a List of Bukkit Color objects from a Strings
	 * @param colors The hex codes or color names
	 * @return Returns a List of Color objects, or null if failed to parse any
	 */
	public static List<Color> colorsFromString(String ... colors) {
		List<Color> cl = new ArrayList<Color>();
		for (String c : colors) {
			if (colorFromString(c) != null)
				cl.add(colorFromString(c));
		}
		return cl.isEmpty()?null:cl;
	}

	/**
	 * Parse a Bukkit's Color object from a Hex string
	 * @param c The hex string
	 * @return Returns a Bukkit's Color object if successful, null otherwise
	 */
	public static Color colorFromHex(String c) {
		Color color;
		c = c.replace("#", "").replace("0x", "");
		int r,g,b;
		if (c.length() == 6) {
			r = Integer.parseInt(c.substring(0, 2), 16);
			g = Integer.parseInt(c.substring(2, 4), 16);
			b = Integer.parseInt(c.substring(4, 6), 16);
			color = Color.fromRGB(r,g,b);
		} else if (c.length() == 3) {
			r = Integer.parseInt(c.substring(0, 1), 16);
			g = Integer.parseInt(c.substring(1, 2), 16);
			b = Integer.parseInt(c.substring(2, 3), 16);
			color = Color.fromRGB(r,g,b);
		} else if (c.length() == 1) {
			r = g = b = Integer.parseInt(c.substring(0, 1), 16);
			color = Color.fromRGB(r,g,b);
		} else {
			return null;
		}
		return color;
	}

	/**
	 * Parse all hex Strings into Bukkit's Color objects
	 * @param colors The hex strings
	 * @return Returns a List of Bukkit's Color objects if successful, null otherwise
	 */
	public static List<Color> colorsFromHex(String ... colors) {
		List<Color> cl = new ArrayList<Color>();
		for (String c : colors) {
			if (colorFromHex(c) != null)
				cl.add(colorFromHex(c));
		}
		return cl.isEmpty()?null:cl;
	}

	private static void firework(Location l, boolean fxOnly, int power, List<FireworkEffect> f) {
		if (f == null || f.size() <= 0) return;
		Firework fw = l.getWorld().spawn(l, Firework.class);
		FireworkMeta fm = fw.getFireworkMeta();
		fm.addEffects(f);
		if (fxOnly) {
			fw.setFireworkMeta(fm);
			try {
				Class<?> entityFireworkClass = getNMSClass("EntityFireworks");
				Class<?> craftFireworkClass = getOBCClass("entity.CraftFirework");
				Object firework = craftFireworkClass.cast(fw);
				Method handle = firework.getClass().getMethod("getHandle");
				Object entityFirework = handle.invoke(firework);
				Field expectedLifespan = entityFireworkClass.getDeclaredField("expectedLifespan");
				Field ticksFlown = entityFireworkClass.getDeclaredField("ticksFlown");
				ticksFlown.setAccessible(true);
				ticksFlown.setInt(entityFirework, expectedLifespan.getInt(entityFirework) - 1);
				ticksFlown.setAccessible(false);
			} catch (Exception e) {
				(new CarbonException(CarbonKit.inst, e)).printStackTrace();
			}
		} else {
			fm.setPower(NumUtils.normalizeInt(power, 1, 6));
			fw.setFireworkMeta(fm);
		}
	}

	public static void firework(Location l, boolean fxOnly, int power, FireworkEffect ... fx) {
		List<FireworkEffect> lfx = new ArrayList<FireworkEffect>();
		Collections.addAll(lfx, fx);
		firework(l, fxOnly, power, lfx);
	}

	private static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
		String name = "net.minecraft.server." + version + nmsClassString;
		return Class.forName(name);
	}

	private static Class<?> getOBCClass(String nmsClassString) throws ClassNotFoundException {
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
		String name = "org.bukkit.craftbukkit." + version + nmsClassString;
		return Class.forName(name);
	}

	private static Method getMethod(Class<?> cl, String method) {
		for(Method m : cl.getMethods())
			if(m.getName().equals(method)) { return m; }
		return null;
	}

}
