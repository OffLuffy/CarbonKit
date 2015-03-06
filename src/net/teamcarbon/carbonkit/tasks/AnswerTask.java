package net.teamcarbon.carbonkit.tasks;

import net.teamcarbon.carbonkit.utils.Question;
import org.bukkit.entity.Player;

@SuppressWarnings("UnusedDeclaration")
public class AnswerTask implements Runnable {
	private Question q;
	private Player p;
	private String a;
	public AnswerTask(Player player, String answer, Question question) {
		p = player;
		a = answer;
		q = question;
	}
	@Override
	public void run() { /*TriviaRound.answerQuestion(p, q.getAnswer(a));*/ }
}
