package net.teamcarbon.carbonkit.utils.CarbonTrivia;

import java.util.*;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.events.triviaEvents.TriviaCancelledEvent;
import net.teamcarbon.carbonkit.events.triviaEvents.TriviaEndEvent;
import net.teamcarbon.carbonkit.events.triviaEvents.TriviaStartEvent;
import net.teamcarbon.carbonkit.modules.CarbonTriviaModule;
import net.teamcarbon.carbonkit.tasks.NextQuestionTask;
import net.teamcarbon.carbonkit.tasks.SkipQuestionTask;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonlib.Misc.TypeUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonlib.Misc.MiscUtils;

@SuppressWarnings("UnusedDeclaration")
public class TriviaRound {

	// Static Members
	private static TriviaRound activeRound = null;
	/**
	 * @return Returns the currently active (started or not) TriviaRound
	 */
	public static TriviaRound getActiveRound() { return activeRound; }
	/**
	 * Removes the TriviaRound object from the activeRound variable
	 */
	public static void clearActiveRound() { activeRound = null; }
	/**
	 * Starts a new TriviaRound (does not announce who started it)
	 */
	public static void newTriviaRound() { if (activeRound == null) activeRound = new TriviaRound(null); }
	/**
	 * Starts a new TriviaRound using the specified Object for the started by message
	 * @param name The String name, CommandSender (if a Player), or Player to broadcast having started the round
	 */
	public static void newTriviaRound(Object name) { if (activeRound == null) activeRound = new TriviaRound(name); }

	// Instance data members

	private boolean started = false;
	private Question curQuestion = null;
	private int cur = 0;
	private boolean answered = false;
	private HashMap<UUID, Integer> points = new HashMap<UUID, Integer>();
	private List<UUID> blacklist = new ArrayList<UUID>();
	private List<Question> questions;

	private final CarbonTriviaModule ctm = CarbonTriviaModule.inst;
	private final FileConfiguration tconf = CarbonKit.getConfig(ConfType.TRIVIA);
	private final long duration = tconf.getLong("quiz-options.question-duration-seconds") * 20L;
	private final long interval = tconf.getLong("quiz-options.question-interval-seconds") * 20L;
	private final boolean random = tconf.getBoolean("quiz-options.random", true);
	private final int qCount = tconf.getInt("quiz-options.count", 6);

	// Only used to indicate equality in overriden equals() and hashCode() methods
	private final UUID id = UUID.randomUUID();

	/**
	 * Constructs and initializes a new round of Trivia
	 * @param name The name to broadcast as having started the round (String, Player, or CommandSender)
	 */
	public TriviaRound(Object name) {
		if (!CarbonTriviaModule.inst.isEnabled()) return;
		if (!started) {
			TriviaStartEvent tse = new TriviaStartEvent(this);
			Bukkit.getPluginManager().callEvent(tse);
			if (!tse.isCancelled()) {
				announce(name);
				startTrivia();
			} else { CarbonKit.log.debug("TriviaStartEvent cancelled"); }
		} else { CarbonKit.log.warn("Trivia event cannot start, it is already running!"); }
	}

	// Private Methods

	// Announces the start of a TriviaRound, includes a started by message if the passed in object is a Player or String
	private void announce(Object o) {
		if (!(o instanceof String) && !(o instanceof CommandSender) && !(o instanceof Player)) return;
		mbc(CustomMessage.CT_START.noPre());
		if (o != null && (o instanceof Player || o instanceof String)) {
			HashMap<String, String> rep = new HashMap<String, String>();
			rep.put("{STARTER}", o instanceof Player?((Player)o).getName():(String)o);
			mbc(MiscUtils.massReplace(CustomMessage.CT_PROVIDED_BY.noPre(), rep));
		}
	}

	// Initializes the questions list and displays the first question in the list
	private void startTrivia() {
		List<Question> qList = Question.getQuestions();
		if (random) Collections.shuffle(qList);
		questions = qList.subList(0, qCount);
		started = true;
		nextQuestion();
	}

	// Rewards the Player{s} provided if they're online.
	private void rewardUsers(List<UUID> winners) {
		CarbonKit.log.debug("Attempting to reward winner(s)");
		if (CarbonKit.getConfig(ConfType.TRIVIA).isList("rewards.items")
				&& !CarbonKit.getConfig(ConfType.TRIVIA).getStringList("rewards.items").isEmpty()) {
			ItemStack[] rewards = new ItemStack[CarbonKit.getConfig(ConfType.TRIVIA).getStringList("rewards.items").size()];
			double money = CarbonKit.getConfig(ConfType.TRIVIA).getDouble("rewards.money", 0.0);
			int i = 0;
			for (String item : CarbonKit.getConfig(ConfType.TRIVIA).getStringList("rewards.items")) {
				Material mat = null;
				short data = (short)0;
				int amount = 1;
				String[] parts = item.split(" ");
				if (parts[0].contains(":")) {
					String[] matParts = parts[0].split(":");
					if (MiscUtils.getMaterial(matParts[0]) != null)
						mat = MiscUtils.getMaterial(matParts[0]);
					if (TypeUtils.isInteger(matParts[1]))
						data = (short)Integer.parseInt(matParts[1]);
				} else if (MiscUtils.getMaterial(parts[0]) != null) {
					mat = MiscUtils.getMaterial(parts[0]);
				}
				if (parts.length > 1 && TypeUtils.isInteger(parts[1])) {
					amount = Integer.parseInt(parts[1]);
				}
				if (mat == null) {
					CarbonKit.log.warn("Invalid trivia reward specified, skipping reward: " + item);
					continue;
				}
				ItemStack is = new ItemStack(mat, amount);
				is.setDurability(data);
				rewards[i] = is;
				i++;
			}
			for (UUID id : winners) {
				if (Bukkit.getPlayer(id) != null) {
					if (Bukkit.getPlayer(id).isOnline()) {
						Player pl = Bukkit.getPlayer(id);
						if (money > 0.0) CarbonKit.econ.depositPlayer(pl, money);
						HashMap<Integer, ItemStack> excess = pl.getInventory().addItem(rewards);
						if (!excess.isEmpty()) {
							pl.sendMessage(CarbonTriviaModule.mpre + CustomMessage.CT_EXCESS_REWARDS.noPre());
							for (ItemStack is : excess.values())
								pl.getWorld().dropItem(pl.getLocation(), is);
						}
						pl.sendMessage(CarbonTriviaModule.mpre + CustomMessage.CT_REWARDED.noPre());
					} else {
						CarbonKit.log.debug("A player being rewarded was not online: " + Bukkit.getPlayer(id).getName());
					}
				} else {
					CarbonKit.log.debug("A UUID in the rewarded list could not be converted to a Player");
				}
			}
		} else {
			CarbonKit.log.warn("Error found with the rewards portion of the config, cannot parse rewards for players!");
		}

	}

	// Public Methods

	/**
	 * Displays the next questions in queue and resets the answered state
	 */
	public void displayNextQuestion() {
		answered = false;
		if (started) {
			Question q = questions.get(cur);
			q.display = System.currentTimeMillis();
			qbc(q.getQuestion());
			CarbonKit.log.debug("Displaying question " + (cur + 1) + " of " + qCount + " - \"" + q.getQuestion() + "\"");
			curQuestion = q;
			cur++;
			Bukkit.getScheduler().scheduleSyncDelayedTask(CarbonKit.inst, new SkipQuestionTask(q), duration);
		}
	}

	/**
	 * Fetches the TriviaRound's UUID (which is assigned to a random UUID when initialized)
	 * @return Returns the TriviaRound's UUID
	 */
	public UUID getId() { return id; }

	/**
	 * Fetches the currently active question or null if there is none.
	 * @return Returns a Question object of this TriviaRounds currently active question
	 */
	public Question getCurrentQuestion() { return curQuestion; }

	/**
	 * Indicates if the currently active question has been answered
	 * @return Returns true if the question has been answered, false otherwise
	 */
	public boolean isQuestionAnswered() { return answered; }

	/**
	 * Sets the question's answered state to true so that it can't be answered again
	 */
	public void setQuestionAnswered() { answered = true; }

	/**
	 * Indicates if the Player is blacklisted from this TriviaRound
	 * @param p The Player to check
	 * @return Returns true if the Player is not allowed to answer, false otherwise
	 */
	public boolean isBlacklisted(Player p) { return blacklist.contains(p.getUniqueId()); }

	/**
	 * Adds the specified Player to the blacklist and resets their points
	 * @param p The Player to blacklist
	 */
	public void blacklistPlayer(Player p) {
		if (!blacklist.contains(p.getUniqueId())) { blacklist.add(p.getUniqueId()); }
		if (hasPoints(p)) { resetPoints(p); }
	}

	/**
	 * Indicates if the specified Player has an entry in the Points map
	 * @param p The Player to check
	 * @return Returns true if the points map contains they Player (even if they have 0 points)
	 */
	public boolean hasPoints(Player p) { return points.containsKey(p.getUniqueId()); }

	/**
	 * Removes the Player's entry in the points map
	 * @param p The Player whose points to reset
	 */
	public void resetPoints(Player p) { if (hasPoints(p)) { points.remove(p.getUniqueId()); } }

	/**
	 * Sets the amount of points the Player has
	 * @param p The Player to set points for
	 * @param pnts Returns the amount of points
	 */
	public void setPoints(Player p, int pnts) { points.put(p.getUniqueId(), pnts); }

	/**
	 * Adds points for the Player (even if the points map doesn't have an entry for the Player)
	 * @param p The Player to add points for
	 * @param pnts The amount of points to add (can be negative to subtract points)
	 */
	public void addPoints(Player p, int pnts) { setPoints(p, hasPoints(p) ? getPoints(p) + pnts : pnts); }

	/**
	 * Fetches the amount of points the Player has
	 * @param p The Player to get points for
	 * @return Returns the amount of points the Player has or 0 if the points map doesn't have an entry for them
	 */
	public int getPoints(Player p) { return hasPoints(p) ? points.get(p.getUniqueId()) : 0; }

	/**
	 * Clears data from lists and maps stored in this object
	 */
	public void flushData() {
		points.clear();
		blacklist.clear();
		questions.clear();
		curQuestion = null;
	}

	/**
	 * Clears data and stops this trivia round and rewards the winning Player(s)
	 */
	public void stopTrivia() {
		if (started) {
			started = false;
			TriviaEndEvent tee = new TriviaEndEvent(this);
			CarbonKit.pm.callEvent(tee);
			int top = 0;
			List<UUID> winners = new ArrayList<UUID>();
			for (UUID id : points.keySet()) {
				if (points.get(id) > top) {
					winners.clear();
					winners.add(id);
					top = points.get(id);
				} else if (points.get(id) == top)
					winners.add(id);
			}
			for (UUID id : blacklist) {
				if (Bukkit.getPlayer(id) != null)
					if (winners.contains(id))
						winners.remove(id);
			}
			HashMap<String, String> rep = new HashMap<String, String>();
			if (winners.size() == 0) {
				mbc(CustomMessage.CT_NO_WINS.noPre());
			} else if (winners.size() == 1) {
				if (Bukkit.getPlayer(winners.get(0)) != null) {
					rep.put("{PLAYER}", Bukkit.getPlayer(winners.get(0)).getName());
					rep.put("{POINTS}", points.get(winners.get(0)) + "");
				} else {
					rep.put("{PLAYER}", "Herobrine");
					rep.put("{POINTS}", "-666");
				}
				mbc(MiscUtils.massReplace(CustomMessage.CT_PLAYER_WINS.noPre(), rep));
				rewardUsers(winners);
			} else if (winners.size() > 1) {
				rep.put("{POINTS}", points.get(winners.get(0)) + "");
				String wins = "";
				for (UUID id : winners)
					if (Bukkit.getPlayer(id) != null && Bukkit.getPlayer(id).isOnline())
						wins += Bukkit.getPlayer(id).getName() + ", ";
				wins = wins.substring(0, wins.length() - 2);
				rep.put("{PLAYERS}", wins);
				mbc(MiscUtils.massReplace(CustomMessage.CT_MULTI_WINS.noPre(), rep));
				rewardUsers(winners);
			} else {
				CarbonKit.log.warn("Something strange happened with the winner list!");
				mbc(CustomMessage.CT_GENERIC_END.noPre());
			}
			flushData();
			TriviaRound.clearActiveRound();
		}
	}

	/**
	 * Clears data and stops this trivia round without rewarding the winning Player(s)
	 */
	public void cancelTrivia() {
		if (started) {
			started = false;
			TriviaCancelledEvent tce = new TriviaCancelledEvent(this);
			CarbonKit.pm.callEvent(tce);
			mbc(CustomMessage.CT_NO_WINS.noPre());
			flushData();
			TriviaRound.clearActiveRound();
		}
	}

	/**
	 * Starts the delayed task which will display the next question in queue after the configurable delay has passed
	 */
	public void nextQuestion() {
		if (started) {
			if (cur >= qCount)
				stopTrivia();
			else {
				Bukkit.getScheduler().scheduleSyncDelayedTask(CarbonKit.inst,
						new NextQuestionTask(), ((curQuestion == null) ? 20L : interval));
			}
		}
	}

	/**
	 * Sets the answered state of the question, broadcasts that the question was answered and calls nextQuestion(),
	 * this does not check the answer, it only broadcasts that it was answered and prepares the next question.
	 * @param p The Player whom answered the question
	 * @param ans The String answer that the Player provided as an answer
	 */
	public void answerQuestion(Player p, String ans) {
		answered = true;
		HashMap<String, String> rep = new HashMap<String, String>();
		rep.put("{PLAYER}", p.getName());
		rep.put("{ANSWER}", ans);
		mbc(MiscUtils.massReplace(CustomMessage.CT_ANSWERED.noPre(), rep));
		UUID id = p.getUniqueId();
		addPoints(p, 1);
		CarbonKit.log.debug(p.getName() + " answered with '" + ans + "', now has " + getPoints(p) + " points");
		nextQuestion();
	}

	/**
	 * Broadcasts that the answer was not answered in time and skips to the next question.
	 */
	public void skipQuestion() {
		CarbonKit.log.debug("No one answered this question, skipping it.");
		mbc(CustomMessage.CT_SKIPPED.noPre());
		nextQuestion();
	}

	/**
	 * Indicates if the TriviaRound has been started
	 * @return Returns true if the TriviaRound is started
	 */
	public boolean isStarted() { return started; }

	/**
	 * Sends a message to all online players with permission to view CarbonTrivia messages
	 * @param msg The message to send
	 */
	public static void bc(String msg) { MiscUtils.permBroadcast("carbonkit.carbontrivia.view", msg); }

	/**
	 * Sends a message to all online players with permission to view CarbonTrivia messages and prepends a message prefix
	 * @param msg The message to send
	 */
	public static void mbc(String msg) { bc(CarbonTriviaModule.mpre + msg); }

	/**
	 * Sends a message to all online players with permission to view CarbonTrivia messages and prepends a question prefix
	 * @param msg The message to send
	 */
	public static void qbc(String msg) { bc(CarbonTriviaModule.qpre + msg); }

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof TriviaRound)) return false;
		TriviaRound tr = (TriviaRound)obj;
		return new EqualsBuilder()
				.append(id, tr.getId())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(id)
				.toHashCode();
	}
}
