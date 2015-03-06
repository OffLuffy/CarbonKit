package net.teamcarbon.carbonkit.tasks;

		import net.teamcarbon.carbonkit.utils.TriviaRound;

public class NextQuestionTask implements Runnable {
	@Override
	public void run() { if (TriviaRound.getActiveRound() != null) TriviaRound.getActiveRound().displayNextQuestion(); }
}
