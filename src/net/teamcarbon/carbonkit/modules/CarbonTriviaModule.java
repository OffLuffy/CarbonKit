package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.events.triviaEvents.TriviaAnswerEvent;
import net.teamcarbon.carbonkit.events.triviaEvents.TriviaUserBlacklistedEvent;
import net.teamcarbon.carbonkit.utils.*;
import net.teamcarbon.carbonkit.utils.CarbonTrivia.Question;
import net.teamcarbon.carbonkit.utils.CarbonTrivia.TriviaRound;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.CarbonTrivia.CarbonTriviaCommand;

@SuppressWarnings("UnusedDeclaration")
public class CarbonTriviaModule extends Module {
	public static CarbonTriviaModule inst;
	public CarbonTriviaModule() throws DuplicateModuleException { super("CarbonTrivia", "ctrivia", "trivia", "ctr", "ct"); }

	public static String mpre;
	public static String qpre;

	private static FileConfiguration tconf;

	public void initModule() {
		inst = this;
		Question.loadQuestions();
		mpre = CustomMessage.CT_MPREFIX.noPre();
		qpre = CustomMessage.CT_QPREFIX.noPre();
		tconf = CarbonKit.getConfig(ConfType.TRIVIA);
		addCmd(new CarbonTriviaCommand(this));
		registerListeners();
	}
	public void disableModule() {
		TriviaRound r = TriviaRound.getActiveRound();
		if (r != null) r.cancelTrivia();
		unregisterListeners();
	}
	public void reloadModule() {
		disableModule();
		CarbonKit.reloadDefConfig();
		CarbonKit.reloadConfig(ConfType.DATA);
		CarbonKit.reloadConfig(ConfType.TRIVIA);
		initModule();
	}
	protected boolean needsListeners() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler
	public void chat(AsyncPlayerChatEvent e) {
		if (!isEnabled()) return;
		long t = System.currentTimeMillis();
		final TriviaRound r = TriviaRound.getActiveRound();
		if (r != null && r.isStarted() && !r.isQuestionAnswered()) {
			final Player p = e.getPlayer();
			if (!r.isBlacklisted(p) && MiscUtils.perm(p, "carbonkit.carbontrivia.answer")) {
				Question q = r.getCurrentQuestion();
				String m = e.getMessage();
				if (q.checkAnswer(m)) {
					final String ans = q.getAnswer(m);
					if (tconf.getBoolean("quiz-options.anti-cheat", true) && ans.length() * 200 > t - q.display) {
						TriviaAnswerEvent tae = new TriviaAnswerEvent(p, ans, true);
						CarbonKit.pm.callEvent(tae);
						if (!tae.isCancelled()) {
							TriviaUserBlacklistedEvent tube = new TriviaUserBlacklistedEvent(p);
							CarbonKit.pm.callEvent(tube);
							if (!tube.isCancelled()) {
								r.blacklistPlayer(p);
								p.sendMessage(mpre + Clr.RED + "Auto-answer detected, you've been disqualified");
								CarbonKit.log.warn(p.getName() + " is answering too quickly, disqualifying them for this round");
								TriviaRound.mbc(CustomMessage.CT_CHEAT_DETECT.noPre().replace("{PLAYER}", p.getName()));
							}
						}
					} else {
						TriviaAnswerEvent tae = new TriviaAnswerEvent(p, ans, false);
						CarbonKit.pm.callEvent(tae);
						if (!tae.isCancelled()) {
							CarbonKit.log.debug(p.getName() + " answered the question with: " + ans);
							r.setQuestionAnswered();
							// Delayed so it doesn't display before the player's message is sent
							Bukkit.getScheduler().runTaskLater(CarbonKit.inst, new Runnable() {
								@Override
								public void run() { r.answerQuestion(p, ans); }
							}, 2L);
						}
					}
				}
			}
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
}
