package me.offluffy.carbonkit.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Border {
	
	public enum BorderShape {
		SQUARE("square","s","cube","cuboid","rectangle","rect"), CIRCLE("circle","c","round","rnd","sphere","s","radial","cylinder","cyl");
		private String[] aliases;
		BorderShape(String ... alias) {
			aliases = alias;
		}
		public static BorderShape getShape(String s) {
			for (BorderShape bs : values())
				for (String al : bs.aliases)
					if (s.equalsIgnoreCase(al))
						return bs;
			return null;
		}
	}
	
	public enum QueryType {
		NAME, GROUP;
	}

	public static HashMap<Border, Boolean> borderMap;
	private World world;
	private int x, z, r;
	private BorderShape shape;
	private String group, name, message;
	private boolean enabled;
	
	public Border(World world, int xPos, int zPos, int radius, BorderShape shape, String group, String name) {
		x = xPos;
		z = zPos;
		r = radius;
		this.shape = shape;
		this.world = world;
		this.group = group;
		this.name = name;
		this.message = "Sorry {PLAYER}, this is the edge of border: {BORDER}";
		this.enabled = true;
	}
	
	public World getWorld() {
		return world;
	}
	public void setWorld(World world) {
		this.world = world;
	}
	public int[] getCenter() {
		return new int[]{x,z};
	}
	public int getX() {
		return x;
	}
	public void setX(int xPos) {
		x = xPos;
	}
	public int getZ() {
		return z;
	}
	public void setZ(int zPos) {
		z = zPos;
	}
	public int getRadius() {
		return r;
	}
	public void setRadius(int radius) {
		r = radius;
	}
	public BorderShape getShape() {
		return shape;
	}
	public void setShape(BorderShape shape) {
		this.shape = shape;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String msg) {
		this.message = msg;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enable) {
		this.enabled = enable;
	}
	public void toggle() {
		enabled = !enabled;
	}
	
	public boolean isInside(double px, double pz) {
		if (shape == BorderShape.CIRCLE){
			double dist = Math.sqrt(Math.pow(x - px, 2.0) + Math.pow(z - pz, 2.0));
			return (dist < r);
		} else if (shape == BorderShape.SQUARE) {
			return !(px > x+r || px < x-r || pz > z+r || pz < z-r);
		}
		return false;
	}
	
	public boolean isInside(Player p) {
		return isInside(p.getLocation().getX(), p.getLocation().getZ());
	}
	
	public void reposition(Player player) {
		if (shape.equals(BorderShape.CIRCLE)){
			Location loc = player.getLocation();
			double theta = Math.atan2((loc.getX()-x), (loc.getZ()-z));
			double px = x + (r-1) * Math.sin(theta);
			double pz = z + (r-1) * Math.cos(theta);
			Location tpLoc = loc.clone();
			tpLoc.setX(px);
			tpLoc.setZ(pz);
			player.sendMessage(procMsg(player));
			teleport(player, tpLoc);
		} else if (shape.equals(BorderShape.SQUARE)) {
			Location loc = player.getLocation();
			double pz = loc.getZ();
			double px = loc.getX();
			if (loc.getZ() < getZ()-getRadius())
				pz = getZ() - getRadius() + 1;
			if (loc.getZ() > getZ()+getRadius())
				pz = getZ() + getRadius() - 1;
			if (loc.getX() < getX()-getRadius())
				px = getX() - getRadius() + 1;
			if (loc.getX() > getX()+getRadius())
				px = getX() + getRadius() - 1;
			Location tpLoc = loc.clone();
			tpLoc.setX(px);
			tpLoc.setZ(pz);
			player.sendMessage(procMsg(player));
			teleport(player, tpLoc);
		}
	}
	
    private void teleport(Player player, Location location) {
        if (player.isDead())
        	return;
        
        loadChunks(location, 3);
        loadChunks(player.getLocation(), 3);
        Entity vehicle = null;
        List<Entity> passengers = new ArrayList<Entity>();

        Entity ent = player;
        while (ent.getVehicle() != null)
        	ent = ent.getVehicle();
        vehicle = ent;
        
        while (ent.getPassenger() != null) {
        	passengers.add(ent.getPassenger());
        	ent = ent.getPassenger();
        };
        
        player.teleport(location);
        Entity lastEnt = vehicle;
        for (Entity e : passengers) {
        	lastEnt.setPassenger(e);
        	lastEnt = e;
        }
        if (vehicle != null)
        	vehicle.setPassenger(player);
    }
    
    private String procMsg(Player player) {
    	String newMsg = message;
    	newMsg = newMsg.replace("{BORDER}", name);
    	newMsg = newMsg.replace("{GROUP}", ((group==null)?"N/A":group));
    	newMsg = newMsg.replace("{WORLD}", world.getName());
    	newMsg = newMsg.replace("{PLAYER}", player.getName());
    	newMsg = newMsg.replace("{SHAPE}", shape.toString());
    	newMsg = ChatColor.translateAlternateColorCodes('&', newMsg);
    	return newMsg;
    }
	
	// =============================[ STATIC ]=============================
    
    private static void loadChunks(Location loc, int rad) {
    	int xm = (int)(loc.getX()/16.0), zm = (int)(loc.getZ()/16.0);
    	for (int cx = xm - rad; cx <= xm + rad; cx++) {
    		for (int cz = zm - rad; cz <= zm + rad; cz++) {
    			loc.getWorld().getChunkAt(cx,cz).load();
    		}
    	}
    }
	
	public static Border getBorder(String name) {
		for (Border bd : borderMap.keySet())
			if (bd.getName().equalsIgnoreCase(name))
				return bd;
		return null;
	}
	
	public static void deleteBorder(Border border) {
		if (borderMap.containsKey(border))
			borderMap.remove(border);
		CarbonKit.borders.set(border.getName(), null);
		Border.saveBorders();
	}

	public static void check(Player pl) {
		if (Lib.perm(pl, "carbonborder.bypass"))
			return;
		World world = pl.getWorld();
		if (Lib.perm(pl, "carbonborder.bypass." + world.getName().toLowerCase()))
			return;
		Border bd = getRelevantBorder(pl, world);
		Border gb = getGlobalBorder(world);
		if (gb == null && bd == null)
			return;
		if (!bd.isInside(pl)) {
			bd.reposition(pl);
			return;
		}
		if (!gb.isInside(pl)) {
			gb.reposition(pl);
			return;
		}
	}
	
	public static Border getRelevantBorder(Player player, World world) {
		String group = CarbonKit.perms.getPrimaryGroup(player);
		for (Border bd : borderMap.keySet())
			if (bd.getGroup() != null && bd.getGroup().equalsIgnoreCase(group) && bd.getWorld().equals(world))
				return bd;
		return getGlobalBorder(world);
	}
	
	public static Border getGlobalBorder(World world) {
		for (Border bd : borderMap.keySet())
			if (bd.getGroup() == null && bd.getWorld().equals(world))
				return bd;
		return null;
	}
	
	public static ArrayList<Border> getBorders(QueryType queryType, String query, World worldFilter) {
		ArrayList<Border> bdList = new ArrayList<Border>();
		boolean found = false;
		if (queryType == null && query == null) {
			for (Border bd : borderMap.keySet())
				bdList.add(bd);
		} else {
			if (Border.validName(query)) {
				for (Border bd : borderMap.keySet())
					if (bd.getName().equalsIgnoreCase(query) && (worldFilter == null || bd.getWorld().equals(worldFilter))) {
						found = true;
						bdList.add(bd);
					}
			}
			if (!found) {
				for (Border bd : borderMap.keySet()) {
					if (bd.getGroup() != null && bd.getGroup().equalsIgnoreCase(query) && (worldFilter == null || bd.getWorld().equals(worldFilter))) {
						found = true;
						bdList.add(bd);
					}
				}
			}
			if (queryType == null && query == null && worldFilter != null) {
				for (Border bd : borderMap.keySet())
					if (bd.getWorld().equals(worldFilter)) {
						found = true;
						bdList.add(bd);
					}
			}
		}
		return bdList;
	}
	
	public static boolean hasBorder(World world) {
		for (Border bd : borderMap.keySet())
			if (bd.getWorld().equals(world))
				return true;
		return false;
	}
	
	public static void loadBorders() {
		if (borderMap == null)
			borderMap = new HashMap<Border, Boolean>();
		borderMap.clear();
		FileConfiguration bds = CarbonKit.borders;
		for (String key : bds.getKeys(false)) {
			String wn = bds.getString(key + ".world", "world");
			if (Bukkit.getWorld(wn) != null) {
				World world = Bukkit.getWorld(wn);
				int x = bds.getInt(key + ".x", 0);
				int z = bds.getInt(key + ".z", 0);
				int r = bds.getInt(key + ".radius", 10);
				String group = bds.getString(key + ".group", null);
				String msg = bds.getString(key + ".message", null);
				BorderShape shape = BorderShape.getShape(bds.getString(key + ".shape", "SQUARE"));
				if (shape == null) {
					Log.warn("Invalid border shape specified for border: " + key);
					Log.warn("This border will load with a square border");
					shape = BorderShape.SQUARE;
				}
				if (group != null) {
					boolean groupFound = false;
					for (String s : CarbonKit.perms.getGroups())
						if (group.equalsIgnoreCase(s))
							groupFound = true;
					if (!groupFound) {
						Log.warn("Couldn't find group: " + group + " for border: " + key);
						Log.warn("This border will be loaded, although it may not apply to anyone");
					}
				}
				Border bd = new Border(world, x, z, r, shape, group, key);
				bd.setMessage(msg);
				if (!Border.borderMap.containsKey(bd))
					Border.borderMap.put(bd, true);
			} else {
				Log.warn("Border found for a world that can't be found! This border won't be loaded.");
				Log.warn("Border: " + key + " - World: " + wn);
			}
		}
		Border.saveBorders();
	}
	
	public static void saveBorders() {
		for (Border bd : borderMap.keySet()) {
			CarbonKit.borders.set(bd.getName() + ".world", bd.getWorld().getName());
			CarbonKit.borders.set(bd.getName() + ".x", bd.getX());
			CarbonKit.borders.set(bd.getName() + ".z", bd.getZ());
			CarbonKit.borders.set(bd.getName() + ".radius", bd.getRadius());
			CarbonKit.borders.set(bd.getName() + ".shape", bd.getShape().toString());
			CarbonKit.borders.set(bd.getName() + ".group", bd.getGroup());
			CarbonKit.borders.set(bd.getName() + ".message", bd.getMessage());
		}
		Lib.saveFile(CarbonKit.borders, "borders.yml");
	}
	
	public static boolean validName(String name) {
		return name.matches("^[a-zA-Z0-9_]+$");
	}
	
	// =============================[ OVERRIDES ]=============================
	
	@Override
	public Border clone() {
		return new Border(world, x, z, r, shape, group, name);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,31).
				append(x).
				append(z).
				append(r).
				append(world).
				append(shape).
				append(group).
				append(name).
				toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Border)) return false;
		
		Border bd = (Border)obj;
		return new EqualsBuilder().
				append(x, bd.getX()).
				append(z, bd.getZ()).
				append(r, bd.getRadius()).
				append(world, bd.getWorld()).
				append(shape, bd.getShape()).
				append(group, bd.getGroup()).
				append(name, bd.getName()).
				isEquals();
	}
}
