package net.teamcarbon.carbonkit.tasks;

import net.teamcarbon.carbonkit.utils.Question;
import net.teamcarbon.carbonkit.utils.TriviaRound;

public class SkipQuestionTask implements Runnable {
	private Question question;
	/**
	 * Initializes a delayed task to skip the current question. If the Question provided does not match
	 * the active question when this executes, it does nothing (to save the hassle of cancelling events)
	 * @param question The question to skip
	 */
	public SkipQuestionTask(Question question) { this.question = question; }
	@Override
	public void run() {
		TriviaRound round = TriviaRound.getActiveRound();
		if (round != null) {
			if (round.getCurrentQuestion().equals(question)) {
				round.skipQuestion();
			}
		}
	}
}
