package me.offluffy.carbonkit.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.Module;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ModuleChatUtils extends Module {
	private List<String> replace, filter, bold;
	private String message;
	public ModuleChatUtils() throws DuplicateModuleException {
		super("ChatUtilities", "chatutils", "cu");
	}

	@Override
	public void initModule() {
		replace = new ArrayList<String>(CarbonKit.config.getConfigurationSection(getName() + ".replace").getKeys(false));
		filter = new ArrayList<String>(CarbonKit.config.getStringList(getName() + ".filter"));
		bold = new ArrayList<String>(CarbonKit.config.getStringList(getName() + ".bold"));
		stringLengthSort(replace);
		stringLengthSort(filter);
		stringLengthSort(bold);
	}

	@Override
	public void disableModule() {}

	@Override
	protected boolean hasListeners() { return true; }

	@Override
	public boolean hasDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/
	
	@EventHandler(ignoreCancelled=false)
	public void chatEvent(AsyncPlayerChatEvent e) {
		if (isEnabled()) {
			message = e.getMessage();
			String out = message;
			Collections.sort(replace);
			for (String r : replace)
				out = out.replaceAll("(?i)" + r.toLowerCase(), getReplacement(r.toLowerCase()));
			for (String f : filter)
				out = out.replaceAll("(?i)" + f.toLowerCase(), getFiltered(f));
			for (String b : bold)
				out = out.replaceAll("(?i)" + b.toLowerCase(), getBolded(b));
			if (out.charAt(out.length()-1) == ' ')
				out = out.substring(0, out.length()-1);
			e.setMessage(ChatColor.translateAlternateColorCodes('&', out));
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
	
	private String getBlank(int length) {
		String blank = "";
		for (int i = 0; i < length; i++)
			blank += "_";
		return blank;
	}
	
	private String getReplacement(String word) {
		for (String w : replace)
			if (w.equalsIgnoreCase(word)) {
				String replacement = "&r" + CarbonKit.config.getString(getName() + ".replace." + w, getBlank(word.length()));
				replacement += "&r" + getColorAt(message.toLowerCase().indexOf(word.toLowerCase()));
				return replacement;
			}
		return null;
	}
	
	private String getFiltered(String word) {
		return "&k" + word + "&r" + getColorAt(message.toLowerCase().indexOf(word.toLowerCase()));
	}
	
	private String getBolded(String word) {
		return "&l" + word + "&r" + getColorAt(message.toLowerCase().indexOf(word.toLowerCase()));
	}
	
	private String getColorAt(int index) {
		HashMap<Integer,Character> colors = new HashMap<Integer,Character>();
		Matcher m = Pattern.compile("[§&]([a-fA-Fk-oK-O])").matcher(message);
		while (m.find())
			colors.put((Integer)message.indexOf(m.group(0)), m.group(0).toLowerCase().charAt(0));
		String color = "";
		for (int i = 0; i <= index; i++) {
			if (colors.containsKey(i))
				color += colors.get(i);
		}
		return (cleanColors(color));
	}
	
	private String cleanColors(String colorString) {
		HashMap<Integer,Character> colors = new HashMap<Integer,Character>();
		HashMap<Integer,Character> format = new HashMap<Integer,Character>();
		String finColor = "";
		Integer lastNum = 0;
		for (int i = 0; i < colorString.length(); i++)
			if ((colorString.charAt(i)+"").matches("[a-f]"))
				colors.put(i, colorString.charAt(i));
			else if ((colorString.charAt(i)+"").matches("[k-o]"))
				format.put(i, colorString.charAt(i));
		for (Integer i : colors.keySet()) {
			lastNum = i;
			finColor = "&" + colors.get(i);
		}
		for (Integer i : format.keySet())
			if (i > lastNum)
				finColor += "&" + format.get(i);
		return ChatColor.translateAlternateColorCodes('&', finColor);
	}
	
	private void stringLengthSort(List<String> list) {
		List<String> newList = new ArrayList<String>();
		int maxLength = 0;
		for (String s : list)
			if (s.length() > maxLength)
				maxLength = s.length();
		for (int i = 0; i < maxLength; i++)
			for (String s : list)
				if (s.length() == maxLength-i)
					newList.add(s);
		list.clear();
		list = newList;
	}
}
