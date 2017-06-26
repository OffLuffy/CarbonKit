package net.teamcarbon.carbonkit.commands.CarbonTools;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.utils.FormattedMessage;
import net.teamcarbon.carbonkit.utils.Messages.Clr;
import net.teamcarbon.carbonkit.utils.MiscUtils;
import net.teamcarbon.carbonkit.utils.NumUtils;
import net.teamcarbon.carbonkit.utils.TypeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

@SuppressWarnings("UnusedDeclaration")
public class TicketCommand extends ModuleCmd {

	private String l;
	private CommandSender s;
	private boolean isConsole;
	private static final String p = "ticket.";
	private static HashMap<UUID, Integer> delConfirm = new HashMap<>();
	private static HashMap<UUID, String> lastSearch = new HashMap<>();
	private static String lastConsoleSearch;

	public TicketCommand(Module module) { super(module, "ticket"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		l = label.toLowerCase();
		s = sender;
		isConsole = !(s instanceof Player);

		final int PER_PAGE = 10;

		if (args.length < 1) {
			showHelp();
		} else {
			String fa = args[0].toLowerCase();
			if (MiscUtils.eq(args[0], "new", "create", "n", "c")) {
				if (!mod.perm(s, p + "create")) {
					s.sendMessage(mod.getCoreMsg("no-perm", false));
					return;
				}
				if (args.length < 5 || MiscUtils.implode(" ", 1, args).length() < 16) {
					s.sendMessage(Clr.RED + "/" + l + " " + fa + " <msg>" + Clr.GRAY + " - Creates a new ticket (message is required)");
					return;
				}
				int tid = createTicket(s instanceof Player ? ((Player) s) : null, MiscUtils.implode(" ", 1, args));
				s.sendMessage(Clr.GOLD + "[Tickets] " + Clr.AQUA + "created ticket #" + tid);
				List<Player> avoid = isConsole ? new ArrayList<>() : MiscUtils.quickList((Player) s);
				MiscUtils.permBroadcast(p + "notify.create", avoid, Clr.DARKAQUA + "[Tickets] " + Clr.NOTE +
						(isConsole ? "Console" : s.getName()) + " created ticket #" + tid);
			} else if (MiscUtils.eq(args[0], "append", "a")) {
				if (!mod.perm(s, p + "append.others", p + "append.self")) {
					s.sendMessage(mod.getCoreMsg("no-perm", false));
					return;
				}
				if (args.length < 3) {
					s.sendMessage(Clr.RED + "/" + l + " " + fa + " <ID> <msg>" + Clr.GRAY + " - Adds text to a ticket. (ID and message is required)");
					return;
				}
				if (!TypeUtils.isInteger(args[1])) {
					s.sendMessage(Clr.RED + "Ticket ID must be a number! /" + l + " " + fa + " <ID> <msg>");
					return;
				}
				int tid = Integer.parseInt(args[1]);
				if (!ticketExists(tid)) {
					s.sendMessage(Clr.RED + "Couldn't find a ticket with ID: " + tid);
					return;
				}
				if (isConsole || mod.perm(s, p + "append.others")
						|| (isAuthor(((Player) s).getUniqueId(), tid) && mod.perm(s, p + "append.self"))) {
					appendToTicket(tid, MiscUtils.implode(" ", 2, args));
					s.sendMessage(Clr.GOLD + "[Tickets] " + Clr.AQUA + "Modified ticket #" + tid);
					List<Player> avoid = MiscUtils.quickList((Player) s);
					MiscUtils.permBroadcast(p + "notify.append", avoid, Clr.DARKAQUA + "[Tickets] " + Clr.NOTE +
							(isConsole ? "Console" : s.getName()) + " modified ticket #" + tid);
				}
			} else if (MiscUtils.eq(args[0], "delete", "remove", "del", "rem")) {
				if (!mod.perm(s, p + "delete.others", p + "delete.self")) {
					s.sendMessage(mod.getCoreMsg("no-perm", false));
					return;
				}
				if (args.length < 2) {
					s.sendMessage(Clr.RED + "/" + l + " " + fa + " <ID>" + Clr.GRAY + " - Deletes a ticket (ID is required)");
					return;
				}
				if (!TypeUtils.isInteger(args[1])) {
					s.sendMessage(Clr.RED + "Ticket ID must be a number! /" + l + " " + fa + " <ID>");
					return;
				}
				int tid = Integer.parseInt(args[1]);
				if (!ticketExists(tid)) {
					s.sendMessage(Clr.RED + "Couldn't find a ticket with ID: " + tid);
					return;
				}
				if (isConsole || mod.perm(s, p + "delete.others")
						|| (isAuthor(((Player) s).getUniqueId(), tid) && mod.perm(s, p + "delete.self"))) {
					if (!isConsole && !delConfirm.containsKey(((Player) s).getUniqueId())) {
						FormattedMessage fm = new FormattedMessage("Are you sure you want to delete ticket #" + tid + "?");
						fm.color(ChatColor.RED).then(" Yes ").command("/" + l + " " + fa + " " + tid)
								.color(ChatColor.DARK_RED).style(ChatColor.UNDERLINE).then(" No ")
								.command("/" + l + " cancel").color(ChatColor.GREEN).style(ChatColor.UNDERLINE);
						fm.send((Player) s);
						delConfirm.put(((Player) s).getUniqueId(), tid);
						return;
					}
					if (!isConsole && delConfirm.containsKey(((Player) s).getUniqueId())) delConfirm.remove(((Player) s).getUniqueId());
					deleteTicket(tid);
					s.sendMessage(Clr.GOLD + "[Tickets] " + Clr.AQUA + "Deleted ticket #" + tid);
					List<Player> avoid = MiscUtils.quickList((Player) s);
					MiscUtils.permBroadcast(p + "notify.delete", avoid, Clr.DARKAQUA + "[Tickets] " + Clr.NOTE +
							(isConsole ? "Console" : s.getName()) + " deleted ticket #" + tid);
				}
			} else if (MiscUtils.eq(args[0], "info", "i")) {
				if (!mod.perm(s, p + "info.others", p + "info.self")) {
					s.sendMessage(mod.getCoreMsg("no-perm", false));
					return;
				}
				if (args.length < 2) {
					s.sendMessage(Clr.RED + "/" + l + " " + fa + " <ID>" + Clr.GRAY + " - Shows ticket info (ID is required)");
					return;
				}
				if (!TypeUtils.isInteger(args[1])) {
					s.sendMessage(Clr.RED + "Ticket ID must be a number! /" + l + " " + fa + " <ID> <msg>");
					return;
				}
				int tid = Integer.parseInt(args[1]);
				if (!ticketExists(tid)) {
					s.sendMessage(Clr.RED + "Couldn't find a ticket with ID: " + tid);
					return;
				}
				if (isConsole || mod.perm(s, p + "info.others") || (isAuthor(((Player) s).getUniqueId(), tid)
						&& mod.perm(s, p + "info.self"))) {
					try {
						Calendar cal = Calendar.getInstance();
						Ticket t = new Ticket(tid);
						cal.setTimeInMillis(t.time);
						MiscUtils.printHeader(s, "Ticket #" + tid + " Info");
						if (!t.consoleAuthor) {
							s.sendMessage(Clr.AQUA + "Author ID: " + Clr.DARKAQUA + t.author.toString());
							s.sendMessage(Clr.AQUA + "Author Name: " + Clr.DARKAQUA + t.authorName);
						} else { s.sendMessage(Clr.AQUA + "Author: " + Clr.DARKAQUA + "Console"); }
						s.sendMessage(Clr.AQUA + "Time: " + Clr.DARKAQUA + getTimePrint(cal));
						if (t.witnesses.size() > 0)
							s.sendMessage(Clr.AQUA + "Witnesses: " + Clr.DARKAQUA + MiscUtils.implode(", ",0, t.witnesses.toArray()));
						s.sendMessage(Clr.AQUA + "Message: " + Clr.DARKAQUA + t.msg);
						FormattedMessage fm = new FormattedMessage("");
						if (isConsole || mod.perm(s, p + "delete.others") || (isAuthor(((Player) s).getUniqueId(), tid)
								&& mod.perm(s, p + "delete.self"))) {
							fm.then("[Delete]").command("/" + l + " delete " + tid).color(ChatColor.DARK_RED)
									.tooltip("Click to delete this ticket");
							fm.send((Player) s);
						}
						MiscUtils.printFooter(s);
					} catch (Exception e) {
						sender.sendMessage(Clr.RED + "There was an error loading the specified ticket");
					}
				}
			} else if (MiscUtils.eq(args[0], "listall", "viewall", "la", "va")) {
				if (!mod.perm(s, p + "view.all")) {
					s.sendMessage(mod.getCoreMsg("no-perm", false));
					return;
				}
				int page = 1;
				if (args.length > 1 && TypeUtils.isInteger(args[1])) page = Integer.parseInt(args[1]);
				List<Ticket> tickets = new ArrayList<>();
				for (int tid : getAllTicketIds()) try { tickets.add(new Ticket(tid)); } catch(Exception ignore) {}
				if (tickets.size() < 1) { sender.sendMessage(Clr.DARKAQUA + "No tickets to view"); return; }
				int pages = tickets.size() % PER_PAGE > 0 ? tickets.size() / PER_PAGE + 1 : tickets.size() / PER_PAGE;
				page = NumUtils.normalizeInt(page, 1, pages);
				int low = (page-1) * PER_PAGE, high = ((page-1) * PER_PAGE) + PER_PAGE;
				MiscUtils.printHeader(s, "All Tickets (pg " + page + "/" + pages + ")");
				for (Ticket t : tickets.subList(low, NumUtils.normalizeInt(high, 0, tickets.size()))) { listTicket(t, true); }
				if (isConsole)
					MiscUtils.printFooter(s);
				else
					MiscUtils.printPaginatedFooter((Player) s, pages, page, "/" + l + " " + fa + " %d");
			} else if (MiscUtils.eq(args[0], "list", "view", "l", "v")) {
				if (!mod.perm(s, p + "view")) {
					s.sendMessage(mod.getCoreMsg("no-perm", false));
					return;
				}
				int page = 1;
				if (args.length > 1 && TypeUtils.isInteger(args[1])) page = Integer.parseInt(args[1]);
				List<Ticket> tickets = new ArrayList<>();
				for (int tid : getAllTicketIds()) try {
					Ticket t = new Ticket(tid);
					if (t.consoleAuthor && isConsole || isAuthor(((Player)s).getUniqueId(), tid))
						tickets.add(t);
				} catch(Exception ignore) {}
				if (tickets.size() < 1) { sender.sendMessage(Clr.DARKAQUA + "No tickets to view"); return; }
				int pages = tickets.size() % PER_PAGE > 0 ? tickets.size() / PER_PAGE + 1 : tickets.size() / PER_PAGE;
				page = NumUtils.normalizeInt(page, 1, pages);
				int low = (page-1) * PER_PAGE, high = ((page-1) * PER_PAGE) + PER_PAGE;
				MiscUtils.printHeader(s, "Your Tickets (pg " + page + "/" + pages + ")");
				for (Ticket t : tickets.subList(low, NumUtils.normalizeInt(high, 0, tickets.size()))) { listTicket(t, false); }
				if (isConsole)
					MiscUtils.printFooter(s);
				else
					MiscUtils.printPaginatedFooter((Player) s, pages, page, "/" + l + " " + fa + " %d");
			} else if (MiscUtils.eq(args[0], "search", "find", "s", "f")) {
				if (!mod.perm(s, p + "search.others", p + "search.self")) {
					s.sendMessage(mod.getCoreMsg("no-perm", false));
					return;
				}
				if (args.length < 2) {
					s.sendMessage(Clr.RED + "/" + l + " " + fa + " <query|#>" + Clr.GRAY + " - Searches tickets or views " +
							"a search result page. (query or a page number is required)");
					return;
				}
				List<Ticket> found;
				UUID ownerFilter = ((isConsole || mod.perm(s, p + "search.others")) ? null : ((Player) s).getUniqueId());
				int page = 1;
				if (TypeUtils.isInteger(args[1])) { // Specified page number
					CarbonKit.log.debug("Page provided for search: " + Integer.parseInt(args[1]));
					if ((isConsole && (lastConsoleSearch == null || lastConsoleSearch.isEmpty()))
							|| (!isConsole && !lastSearch.containsKey(((Player) s).getUniqueId()))) {
						s.sendMessage(Clr.RED + "Search for something first! /" + l + " " + fa + " <query>");
						return;
					}
					String query = isConsole ? lastConsoleSearch : lastSearch.get(((Player) s).getUniqueId());
					found = searchTickets(query, ownerFilter);
					page = Integer.parseInt(args[1]);
				} else {
					CarbonKit.log.debug("New search created");
					String query = MiscUtils.implode(" ", 2, args);
					found = searchTickets(MiscUtils.implode(" ", 2, query), ownerFilter);
					if (!isConsole) lastSearch.put(((Player) s).getUniqueId(), query);
					else lastConsoleSearch = query;
					sender.sendMessage(Clr.NOTE + "View other pages with " + Clr.DARKAQUA + "/" + l + " " + fa + " <pageNum>");
				}
				if (found.isEmpty()) { s.sendMessage(Clr.DARKAQUA + "Could not find tickets matching the search"); return; }
				int low = (page-1) * PER_PAGE, high = ((page-1) * PER_PAGE) + PER_PAGE;
				int pages = found.size() % PER_PAGE > 0 ? found.size() / PER_PAGE + 1 : found.size() / PER_PAGE;
				MiscUtils.printHeader(s, "Ticket Search Results");
				for (Ticket t : found.subList(low, NumUtils.normalizeInt(high, 0, found.size()))) listTicket(t, mod.perm(s, p + "search.others"));
				if (isConsole)
					MiscUtils.printFooter(s);
				else
					MiscUtils.printPaginatedFooter((Player) s, pages, page, "/" + l + " " + fa + " %d");
			} else if (MiscUtils.eq(args[0], "cancel")) {
				if (delConfirm.containsKey(((Player) s).getUniqueId())) {
					delConfirm.remove(((Player) s).getUniqueId());
					s.sendMessage(Clr.DARKAQUA + "Cancelled ticket delete request");
				}
			} else {
				showHelp();
			}
		}
	}

	private void listTicket(Ticket t, boolean showName) {
		int nl = showName ? 32 : 48;
		if (isConsole) {
			String msg = t.msg.length() > nl ? t.msg.substring(0, nl) + " ..." : t.msg;
			s.sendMessage(Clr.GRAY + "[" + t.id + "] " + (showName ? t.authorName + ": " : "") + Clr.DARKAQUA + msg);
		} else {
			FormattedMessage fm = new FormattedMessage("");
			if (isConsole || mod.perm(s, p + "delete.others") || (isAuthor(((Player) s).getUniqueId(), t.id)
					&& mod.perm(s, p + "delete.self"))) {
				fm.then("[\u00D7]").command("/" + l + " delete " + t.id).color(ChatColor.RED).tooltip("Delete");
			}
			fm.then("[" + t.id + "]").color(ChatColor.GRAY);
			if (isConsole || mod.perm(s, p + "info.others") || (isAuthor(((Player) s).getUniqueId(), t.id)
					&& mod.perm(s, p + "info.self"))) {
				fm.command("/" + l + " info " + t.id).tooltip("View Info");
			}
			if (showName) {
				fm.then(" " + t.authorName + ":").color(ChatColor.GRAY);
				if (mod.perm(s, p + "search.others", p + "search.self"))
					fm.command("/" + l + " search " + t.authorName).tooltip("Search tickets for \"" + t.authorName + "\"");
			}
			fm.then(" " + (t.msg.length() > nl ? t.msg.substring(0, nl) + " ..." : t.msg)).color(ChatColor.DARK_AQUA);
			fm.send((Player) s);
		}
	}
	private String getTimePrint(Calendar cal) {
		String[] m = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
		return m[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR) + " @ "
				+ hr(cal) + ":" + pad(cal.get(Calendar.MINUTE)) + " " + ampm(cal) + " " + cal.getTimeZone().getDisplayName(false, TimeZone.SHORT);
	}
	private String hr(Calendar cal) { return pad(cal.get(Calendar.HOUR) == 0 ? 12 : cal.get(Calendar.HOUR)); }
	private String pad(int i) { return (i < 10 ? "0":"") + i; }
	private String ampm(Calendar cal) { return cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM"; }

	private List<Ticket> searchTickets(String query, UUID ownerFilter) {
		query = query.toLowerCase();
		List<Ticket> tickets = new ArrayList<>();
		for (int tid : getAllTicketIds()) {
			try {
				Ticket t = new Ticket(tid);
				if ((ownerFilter == null || (!t.consoleAuthor && t.author.equals(ownerFilter))))
					if (t.msg.toLowerCase().contains(query) || t.authorName.toLowerCase().contains(query))
						tickets.add(t);
			} catch (Exception ignore) {}
		}
		return tickets;
	}
	private List<Integer> getAllTicketIds() {
		List<Integer> tids = new ArrayList<>();
		for (String key : mod.getData().getConfigurationSection("ticket-data.tickets").getKeys(false))
			if (TypeUtils.isInteger(key)) tids.add(Integer.parseInt(key));
		return tids;
	}
	private int getNextTID() {
		int tid = mod.getData().getInt("ticket-data.next-ticket-id", 0);
		while (ticketExists(tid)) { ++tid; }
		return tid;
	}
	private void incNextTID(boolean save) {
		mod.getData().set("ticket-data.next-ticket-id", getNextTID());
		if (save) CarbonKit.saveConfig(ConfType.DATA);
	}
	private boolean isAuthor(UUID pid, int tid) {
		try {
			Ticket t = new Ticket(tid);
			return t.author != null && pid.equals(t.author);
		} catch (Exception e) { return false; }
	}
	private boolean ticketExists(int id) { try { new Ticket(id); return true; } catch (Exception e) { return false; } }

	private int createTicket(Player author, String msg) {
		int tid = getNextTID();
		String path = "ticket-data.tickets." + tid + ".";
		mod.getData().set(path + "author", author == null ? "CONSOLE" : author.getUniqueId().toString() + "|" + author.getName());
		mod.getData().set(path + "message", msg);
		mod.getData().set(path + "time", System.currentTimeMillis());
		if (Bukkit.getOnlinePlayers().size() > 0) {
			List<String> witnesses = new ArrayList<>();
			for (Player p : Bukkit.getOnlinePlayers()) if (!p.equals(author)) witnesses.add(p.getName());
			mod.getData().set(path + "witnesses", witnesses);
		}
		incNextTID(false);
		CarbonKit.saveConfig(ConfType.DATA);
		return tid;
	}
	private void appendToTicket(int id, String msg) {
		try {
			Ticket t = new Ticket(id);
			mod.getData().set("ticket-data.tickets." + t.id + ".message", t.msg + msg);
			CarbonKit.saveConfig(ConfType.DATA);
		} catch (Exception ignore) {}
	}
	private void deleteTicket(int id) {
		mod.getData().set("ticket-data.tickets." + id, null);
		CarbonKit.saveConfig(ConfType.DATA);
	}

	/* short-hand to send the block of help text */
	private void showHelp() {
		if (s == null) return;
		if (mod.perm(s, p + "create", p + "append.others", p + "append.self", p + "delete.others", p + "delete.self",
				p + "info.others", p + "info.self", p + "view.all", p + "view", p + "search.others", p + "search.self")) {
			MiscUtils.printHeader(s, "Ticket Help");
			s.sendMessage(Clr.NOTE + "Tickets are used to report infractions, suggest new ideas, etc. If " +
					"reporting rule breakers, try to include the time of the incident and any relevant details. " +
					"If you need to add text to the ticket, use " + Clr.DARKAQUA + "/" + l + " append");
			s.sendMessage(Clr.NOTE + "The time and players online when created are recorded.");
			MiscUtils.printDivider(s);
			h("new <msg>", "Creates a new ticket", p + "create");
			h("append <ID> <msg>", "Adds text to existing ticket", p + "append.others", p + "append.self");
			h("delete <ID>", "Deletes a ticket", p + "delete.others", p + "delete.self");
			h("info <ID>", "View info about a ticket", p + "info.others", p + "info.self");
			h("listall [#]", "Lists all tickets", p + "view.all");
			h("list [#]", "Lists your tickets", p + "view");
			h("search <query|#>", "Search tickets or view result page", p + "search.others", p + "search.self");
			MiscUtils.printFooter(s);
		} else {
			s.sendMessage(mod.getCoreMsg("no-perm", false));
		}
	}
	/* short-hand to perm-check and print a command help line */
	private void h(String args, String desc, String... perms) {
		if (perms.length == 0 || mod.perm(s, perms)) {
			if (l == null || l.isEmpty()) l = "ticket";
			String cp = Clr.AQUA + "/%s %s " + Clr.GRAY + "- %s";
			s.sendMessage(String.format(Locale.ENGLISH, cp, l, args, desc));
		}
	}

	/* Makes the list and listall commands easier to filter */
	private class Ticket {
		public int id;
		public String msg, authorName;
		public List<String> witnesses;
		public long time;
		public UUID author;
		public boolean consoleAuthor;

		public Ticket(int id) throws TicketLoadException {
			if (!mod.getData().contains("ticket-data.tickets." + id)) throw new TicketLoadException("Ticket doesn't exist");
			String path = "ticket-data.tickets." + id;
			ConfigurationSection md = mod.getData().getConfigurationSection(path);
			this.id = id;
			this.msg = md.getString("message", "");
			this.witnesses = md.contains("witnesses") ? md.getStringList("witnesses") : new ArrayList<>();
			this.time = md.getLong("time", 0);
			this.consoleAuthor = md.getString("author", "CONSOLE").equalsIgnoreCase("CONSOLE");
			if (!consoleAuthor) {
				try {
					String[] parts = md.getString("author").split("\\|");
					author = UUID.fromString(parts[0]);
					authorName = parts[1];
				} catch(Exception ignore) {}
			} else {
				author = null;
				authorName = "CONSOLE";
			}
		}
	}

	private class TicketLoadException extends Exception {
		public TicketLoadException(String reason) { super("Failed to load Ticket" + ((reason == null || reason.isEmpty()) ? "" : ": " + reason)); }
	}

}
