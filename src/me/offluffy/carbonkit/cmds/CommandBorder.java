package me.offluffy.carbonkit.cmds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.utils.Border;
import me.offluffy.carbonkit.utils.Border.BorderShape;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;
import me.offluffy.carbonkit.utils.NumUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBorder extends ModuleCmd {
	private HashMap<String, ArrayList<Border>> confirm = new HashMap<String, ArrayList<Border>>();
	public CommandBorder(Module module) {
		super(module, "carbonborder");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		//boolean broadcast = CarbonKit.config.getBoolean("broadcast-changes");
		
		if (args.length == 0) {
			if(!canExec(sender, false, "help")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			sender.sendMessage(Messages.Clr.TITLE + "=====[ CarbonBorder ]=====");
			sender.sendMessage(Messages.Clr.NOTE + "Note: <required> [optional] '|' delimits choices");
			if(canExec(sender, false, "set"))
				sender.sendMessage(Messages.Clr.NORM + "/" + label + " set <radius> <name> [group]");
			if(canExec(sender, false, "update"))
				sender.sendMessage(Messages.Clr.NORM + "/" + label + " update <name> <center|name|group|radius|shape|message> [value]");
			if(canExec(sender, false, "remove"))
				sender.sendMessage(Messages.Clr.NORM + "/" + label + " remove <name|world> [world]");
			if(canExec(sender, false, "toggle"))
				sender.sendMessage(Messages.Clr.NORM + "/" + label + " toggle <name>");
			if(canExec(sender, false, "check"))
				sender.sendMessage(Messages.Clr.NORM + "/" + label + " check [world|group|name]");
			if(canExec(sender, false, "reload"))
				sender.sendMessage(Messages.Clr.NORM + "/" + label + " reload");
			return true;
		}

		// ======================================================================================
		// =========================[        RELOAD       ]======================================
		// ======================================================================================
		if (Lib.eq(args[0],"reload","r","rl")) { // /bd reload
			if (!canExec(sender, false, "reload")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			CarbonKit.borders = Lib.reloadFile("borders.yml");
			Border.loadBorders();
			sender.sendMessage(Messages.Clr.HEAD + "CarbonBorder Reloaded");
			return true;
		}
		// ======================================================================================
		// =========================[         SET         ]======================================
		// ======================================================================================
		if (Lib.eq(args[0],"set","create")) { // /bd set <name> <radius> [group]
			if (!(sender instanceof Player)) {
				Messages.send(sender, Message.NOT_ONLINE);
				return true;
			}
			if(!canExec(sender, false, "set")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			if (args.length < 3 || args.length > 4) {
				sender.sendMessage(Messages.Clr.ERR + "usage: /" + label + " " + args[0] + " <radius> <name> [group]");
				return true;
			}
			if (!Border.validName(args[2])) {
				sender.sendMessage(Messages.Clr.ERR + "Name can only have letters, numbers, and underscores.");
				return true;
			}
			if (Border.getBorder(args[2]) != null) {
				sender.sendMessage(Messages.Clr.ERR + "A border with that name exists already!"
					+ " Use " + Messages.Clr.NORM + "/" + label + " update" + Messages.Clr.ERR + " to change it");
				return true;
			}
			if (!NumUtils.isInteger(args[1]) || Integer.parseInt(args[1]) < 2) {
				sender.sendMessage(Messages.Clr.ERR + "Radius must be a whole number larger than 1");
				return true;
			}
			if (args.length == 4) {
				if (!Lib.groupExists(args[3])) {
					sender.sendMessage(Messages.Clr.ERR + "Couldn't find the group specified: " + args[3]);
					String groups = CarbonKit.perms.getGroups()[0];
					for (int i = 1; i < CarbonKit.perms.getGroups().length; i++)
						groups += ", " + CarbonKit.perms.getGroups()[i];
					sender.sendMessage(Messages.Clr.NOTE + "Available groups: " + groups);
					return true;
				}
				for (Border bd : Border.borderMap.keySet()) {
					if (bd.getGroup() != null && bd.getGroup().equalsIgnoreCase(args[3]) && bd.getWorld().equals(((Player)sender).getWorld())) {
						sender.sendMessage(Messages.Clr.ERR + "There's already a border for that group in this world: " + bd.getName()
							+ ". Use " + Messages.Clr.NORM + "/" + label + " update" + Messages.Clr.ERR + " to change it");
						return true;
					}
				}
			} else {
				for (Border bd : Border.borderMap.keySet()) {
					if (bd.getGroup() == null & bd.getWorld().equals(((Player)sender).getWorld())) {
						sender.sendMessage(Messages.Clr.ERR + "There's already a global border in this world: " + bd.getName()
								+ ". Use " + Messages.Clr.NORM + "/" + label + " update" + Messages.Clr.ERR + " to change it");
							return true;
					}
				}
			}
			
			int radius = Integer.parseInt(args[1]);
			Location loc = ((Player)sender).getLocation();
			String name = args[2];
			String group = (args.length == 4)?args[3]:null;
			
			Border bd = new Border(loc.getWorld(), loc.getBlockX(), loc.getBlockZ(), radius, BorderShape.SQUARE, group, name);
			if (Border.borderMap.containsKey(bd)) {
				sender.sendMessage(Messages.Clr.ERR + "Border: " + args[2] + " already exists!"
					+ " Use " + Messages.Clr.NORM + "/" + label + " update" + Messages.Clr.ERR + " to change it");
				return true;
			} else {
				Border.borderMap.put(bd, true);
				Border.saveBorders();
				sender.sendMessage(Messages.Clr.NORM + "Created border!");
				return true;
			}
		}
		// ======================================================================================
		// =========================[        UPDATE       ]======================================
		// ======================================================================================
		if (Lib.eq(args[0],"update","u","change")) { // /bd update <name> <center|name|group|radius|shape|message> [value]
			if (!(sender instanceof Player)) {
				Messages.send(sender, Message.NOT_ONLINE);
				return true;
			}
			if(!canExec(sender, false, "update")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			if (args.length < 3 || args.length > 4) {
				sender.sendMessage(Messages.Clr.ERR + "usage: /" + label + " " + args[0] + " <name> <center|name|group|radius|shape> [value]");
				return true;
			}
			if (!Border.validName(args[1])) {
				sender.sendMessage(Messages.Clr.ERR + "Name can only have letters, numbers, and underscores.");
				return true;
			}
			if (Border.getBorder(args[1]) == null) {
				sender.sendMessage(Messages.Clr.ERR + "Couldn't find a border with that name!");
				String borders = CarbonKit.perms.getGroups()[0];
				for (int i = 1; i < Border.borderMap.size(); i++)
					borders += ", " + CarbonKit.perms.getGroups()[i];
				sender.sendMessage(Messages.Clr.NOTE + "Available borders: " + borders);
				return true;
			}
			if (!Lib.eq(args[2], "center", "name", "group", "radius", "shape")) {
				sender.sendMessage(Messages.Clr.ERR + "Unknown field. Available fields: center, name, group, radius, shape");
				return true;
			}
			if (Lib.eq(args[2], "name", "group", "radius", "shape") && args.length != 4) {
				sender.sendMessage(Messages.Clr.ERR + "The " + args[2] + " field requires a value!");
				sender.sendMessage(Messages.Clr.NOTE + "Ex: /" + label + " " + args[2] + " value");
				return true;
			}
			Border bd = Border.getBorder(args[1]);
			switch (args[2]) {
				case "name":
					if (Border.getBorder(args[3]) != null) {
						sender.sendMessage(Messages.Clr.ERR + "A border with that name exists already!"
							+ " Use " + Messages.Clr.NORM + "/" + label + " update" + Messages.Clr.ERR + " to change it");
						return true;
					}
					CarbonKit.borders.set(bd.getName(), null);
					bd.setName(args[3]);
					Border.saveBorders();
					sender.sendMessage(Messages.Clr.NORM + "Border updated!");
					return true;
				case "group":
					if (!Lib.groupExists(args[3])) {
						sender.sendMessage(Messages.Clr.ERR + "Couldn't find the group specified: " + args[3]);
						String groups = CarbonKit.perms.getGroups()[0];
						for (int i = 1; i < CarbonKit.perms.getGroups().length; i++)
							groups += ", " + CarbonKit.perms.getGroups()[i];
						sender.sendMessage(Messages.Clr.NOTE + "Available groups: " + groups);
						return true;
					}
					for (Border border : Border.borderMap.keySet()) {
						if (border.getGroup() != null && (border.getGroup().equalsIgnoreCase(args[3]) && border.getWorld().equals(bd.getWorld()))) {
							sender.sendMessage(Messages.Clr.ERR + "This would conflict with a border in that world: " + bd.getName()
								+ ". Use " + Messages.Clr.NORM + "/" + label + " update" + Messages.Clr.ERR + " to change it");
							return true;
						}
					}
					bd.setGroup(args[3]);
					Border.saveBorders();
					sender.sendMessage(Messages.Clr.NORM + "Border updated!");
					return true;
				case "radius":
					if (!NumUtils.isInteger(args[3]) || Integer.parseInt(args[3]) < 2) {
						sender.sendMessage(Messages.Clr.ERR + "Radius must be a whole number larger than 1");
						return true;
					}
					bd.setRadius(Integer.parseInt(args[3]));
					Border.saveBorders();
					sender.sendMessage(Messages.Clr.NORM + "Border updated!");
					return true;
				case "shape":
					if (BorderShape.getShape(args[3]) == null) {
						sender.sendMessage(Messages.Clr.ERR + "Shape not recognized! Try 'square' or 'circle'");
						return true;
					}
					bd.setShape(BorderShape.getShape(args[3]));
					sender.sendMessage(Messages.Clr.NORM + "Border updated!");
					return true;
				case "center":
					if (!((Player)sender).getWorld().equals(bd.getWorld())) {
						sender.sendMessage(Messages.Clr.ERR + "You must be in the world of the border!");
						return true;
					}
					bd.setX(((Player)sender).getLocation().getBlockX());
					bd.setZ(((Player)sender).getLocation().getBlockZ());
					Border.saveBorders();
					sender.sendMessage(Messages.Clr.NORM + "Border updated!");
					return true;
			}
			
		}
		// ======================================================================================
		// =========================[        REMOVE       ]======================================
		// ======================================================================================
		if (Lib.eq(args[0],"remove","delete","rem","del","confirm")) { // /bd remove <name|world|group> [world] 
			if (!(sender instanceof Player)) {
				Messages.send(sender, Message.NOT_ONLINE);
				return true;
			}
			if(!canExec(sender, false, "remove")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
				if (!confirm.containsKey(sender.getName())) {
					sender.sendMessage(Messages.Clr.ERR + "You have nothing to confirm!");
					return true;
				}
				for (Border bd : confirm.get(sender.getName()))
					Border.deleteBorder(bd);
				sender.sendMessage(Messages.Clr.NORM + "Deleted " + confirm.get(sender.getName()).size() + " borders!");
				confirm.remove(sender.getName());
				return true;
			}
			if (args.length < 2 || args.length > 3) {
				sender.sendMessage(Messages.Clr.ERR + "usage: /" + label + " " + args[0] + " <name|world|group> [world]");
				sender.sendMessage(Messages.Clr.NOTE + "This will require a confirmation command");
				return true;
			}
			boolean found = false;
			if (Border.validName(args[1])) {
				if (Border.getBorder(args[1]) != null) {
					found = true;
					ArrayList<Border> bdList = new ArrayList<Border>();
					bdList.add(Border.getBorder(args[1]));
					confirm.remove(sender.getName());
					confirm.put(sender.getName(), bdList);
					sender.sendMessage(Messages.Clr.WARN + "1 border marked for deletion (Chosen by name): " + Border.getBorder(args[1]).getName());
					sender.sendMessage(Messages.Clr.NORM + "Type " + Messages.Clr.WARN + "/" + label + " confirm" + Messages.Clr.NORM + " to delete this claim");
					return true;
				}
			}
			if (!found) {
				ArrayList<Border> bdList = new ArrayList<Border>();
				String group = args[1];
				World world = (args.length==3)?Bukkit.getWorld(args[2]):null;
				if (args.length == 3 && world == null) {
					sender.sendMessage(Messages.Clr.ERR + "Couldn't find the world: " + args[2]);
					return true;
				}
				for (Border bd : Border.borderMap.keySet())
					if (bd.getGroup() != null && bd.getGroup().equalsIgnoreCase(group) && (world == null || bd.getWorld().equals(world)))
						bdList.add(bd);
				if (bdList.size() > 0) {
					found = true;
					confirm.remove(sender.getName());
					confirm.put(sender.getName(), bdList);
					if (world != null) {
						sender.sendMessage(Messages.Clr.WARN + "" + bdList.size() + " border" + ((bdList.size() > 1)?"s":"")
							+ " marked for deletion (All borders of " + group + " group in world: " + world.getName() + ")");
						sender.sendMessage(Messages.Clr.NORM + "Type " + Messages.Clr.WARN + "/" + label + " confirm" + Messages.Clr.NORM + " to delete this claim");
						return true;
					} else {
						sender.sendMessage(Messages.Clr.WARN + "" + bdList.size() + " border" + ((bdList.size() > 1)?"s":"")
								+ " marked for deletion (All borders of " + group + " group in all worlds)");
						sender.sendMessage(Messages.Clr.NORM + "Type " + Messages.Clr.WARN + "/" + label + " confirm" + Messages.Clr.NORM + " to delete this claim");
							return true;
					}
				}
			}
			if (!found) {
				if (Bukkit.getWorld(args[1]) != null) {
					World world = Bukkit.getWorld(args[1]);
					ArrayList<Border> bdList = new ArrayList<Border>();
					for (Border bd : Border.borderMap.keySet())
						if (bd.getWorld().equals(world))
							bdList.add(bd);
					if (bdList.size() > 0) {
						found = true;
						confirm.remove(sender.getName());
						confirm.put(sender.getName(), bdList);
						sender.sendMessage(Messages.Clr.WARN + "" + bdList.size() + " border" + ((bdList.size() > 1)?"s":"")
								+ " marked for deletion (All borders in world: " + world.getName() + ")");
							sender.sendMessage(Messages.Clr.NORM + "Type " + Messages.Clr.WARN + "/" + label + " confirm" + Messages.Clr.NORM + " to delete this claim");
							return true;
					}
				}
			}
			if (!found) {
				sender.sendMessage(Messages.Clr.NORM + "No borders were found to delete.");
				return true;
			}
		}
		
		// ======================================================================================
		// =========================[        CHECK        ]======================================
		// ======================================================================================
		if (Lib.eq(args[0], "check","c","status","s","list","l")) { // /bd check [world|group|name]
			if(!canExec(sender, false, "check")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			if (args.length < 1 || args.length > 2) {
				sender.sendMessage(Messages.Clr.ERR + "usage: /" + label + " " + args[0] + " [world|group|name]");
				return true;
			}
			ArrayList<Border> bdList = new ArrayList<Border>();
			if (args.length == 1) {
				sender.sendMessage(Messages.Clr.HEAD + "Found " + Border.borderMap.keySet().size() + " borders:");
				listBorders(sender, Border.borderMap.keySet());
				return true;
			}
			boolean found = false;
			if (Border.validName(args[1])) {
				if (Border.getBorder(args[1]) != null) {
					found = true;
					bdList.add(Border.getBorder(args[1]));
					sender.sendMessage(Messages.Clr.HEAD + "Found 1 border, based on name: " + args[1]);
					listBorders(sender, bdList);
					return true;
				}
			}
			if (!found) {
				String group = args[1];
				for (Border bd : Border.borderMap.keySet())
					if (bd.getGroup() != null && bd.getGroup().equalsIgnoreCase(group))
						bdList.add(bd);
				if (bdList.size() > 0) {
					found = true;
					sender.sendMessage(Messages.Clr.HEAD + "Found " + bdList.size() + " border" + ((bdList.size()>1)?"s":"") + ", based on group: " + args[1]);
					listBorders(sender, bdList);
					return true;
				}
			}
			if (!found) {
				if (Bukkit.getWorld(args[1]) != null) {
					World world = Bukkit.getWorld(args[1]);
					for (Border bd : Border.borderMap.keySet())
						if (bd.getWorld().equals(world))
							bdList.add(bd);
				}
				if (bdList.size() > 0) {
					found = true;
					sender.sendMessage(Messages.Clr.HEAD + "Found " + bdList.size() + " border" + ((bdList.size()>1)?"s":"") + ", based on world: " + args[1]);
					listBorders(sender, bdList);
					return true;
				}
			}
			if (!found) {
				sender.sendMessage(Messages.Clr.NORM + "No borders were found.");
				return true;
			}
		}
		
		// ======================================================================================
		// =========================[        TOGGLE       ]======================================
		// ======================================================================================
		if (Lib.eq(args[0], "toggle")) { // /bd toggle [world|group|name]
			//XXX TOGGLE BORDERS
		}
		if(!canExec(sender, false, "help")) {
			Messages.send(sender, Message.NO_PERM);
			return true;
		}
		sender.sendMessage(Messages.Clr.TITLE + "=====[ CarbonBorder ]=====");
		sender.sendMessage(Messages.Clr.NOTE + "Note: <required> [optional] '|' delimits choices");
		if(canExec(sender, false, "set"))
			sender.sendMessage(Messages.Clr.NORM + "/" + label + " set <radius> <name> [group]");
		if(canExec(sender, false, "update"))
			sender.sendMessage(Messages.Clr.NORM + "/" + label + " update <name> <center|name|group|radius|shape|message> [value]");
		if(canExec(sender, false, "remove"))
			sender.sendMessage(Messages.Clr.NORM + "/" + label + " remove <name|world> [world]");
		if(canExec(sender, false, "toggle"))
			sender.sendMessage(Messages.Clr.NORM + "/" + label + " toggle <name>");
		if(canExec(sender, false, "check"))
			sender.sendMessage(Messages.Clr.NORM + "/" + label + " check [world|group|name]");
		if(canExec(sender, false, "reload"))
			sender.sendMessage(Messages.Clr.NORM + "/" + label + " reload");
		return true;
	}
	
	public void listBorders(CommandSender sender, Set<Border> bdList) {
		for (Border bd : bdList) {
			sender.sendMessage(Messages.Clr.NORM + "----------------------------");
			sender.sendMessage(Messages.Clr.HEAD + "Border: " + Messages.Clr.NOTE + bd.getName());
			sender.sendMessage(Messages.Clr.HEAD + "Center: " + Messages.Clr.NOTE + bd.getX() + ", " + bd.getZ());
			sender.sendMessage(Messages.Clr.HEAD + "Radius: " + Messages.Clr.NOTE + bd.getRadius());
			sender.sendMessage(Messages.Clr.HEAD + "Shape: " + Messages.Clr.NOTE + bd.getShape().toString());
			sender.sendMessage(Messages.Clr.HEAD + "World: " + Messages.Clr.NOTE + bd.getWorld().getName());
			sender.sendMessage(Messages.Clr.HEAD + "Group: " + Messages.Clr.NOTE + ((bd.getGroup()==null)?"All":bd.getGroup()));
		}
	}
	
	public void listBorders(CommandSender sender, ArrayList<Border> bdList) {
		for (Border bd : bdList) {
			sender.sendMessage(Messages.Clr.NORM + "----------------------------");
			sender.sendMessage(Messages.Clr.HEAD + "Border: " + Messages.Clr.NOTE + bd.getName());
			sender.sendMessage(Messages.Clr.HEAD + "Center: " + Messages.Clr.NOTE + bd.getX() + ", " + bd.getZ());
			sender.sendMessage(Messages.Clr.HEAD + "Radius: " + Messages.Clr.NOTE + bd.getRadius());
			sender.sendMessage(Messages.Clr.HEAD + "Shape: " + Messages.Clr.NOTE + bd.getShape().toString());
			sender.sendMessage(Messages.Clr.HEAD + "World: " + Messages.Clr.NOTE + bd.getWorld().getName());
			sender.sendMessage(Messages.Clr.HEAD + "Group: " + Messages.Clr.NOTE + ((bd.getGroup()==null)?"All":bd.getGroup()));
		}
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("help", toList("borders.set", "borders.remove", "borders.toggle", "borders.update", "borders.check"));
		perms.put("set", toList("borders.set"));
		perms.put("remove", toList("borders.remove"));
		perms.put("check", toList("borders.check"));
		perms.put("toggle", toList("borders.toggle"));
		perms.put("update", toList("borders.update"));
		return perms;
	}

}
