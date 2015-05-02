package net.teamcarbon.carbonkit.utils.CarbonTrivia;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class Question {
	public static List<Question> questions;
	private List<String> answers;
	private String question;
	public long display;
	public Question(String question, List<String> answers) {
		this.question = question;
		this.answers = answers;
	}
	public static void loadQuestions() {
		FileConfiguration tconf = CarbonKit.getConfig(ConfType.TRIVIA);
		if (tconf.contains("questions") && tconf.getConfigurationSection("questions").getKeys(false).size() > 0) {
			ConfigurationSection qSect = tconf.getConfigurationSection("questions");
			if (questions != null && !questions.isEmpty()) questions.clear();
			else if (questions == null) questions = new ArrayList<Question>();
			for(String s : qSect.getKeys(false)) {
				if (qSect.isList(s) && qSect.getStringList(s).size() > 0) {
					Question q = new Question(s, qSect.getStringList(s));
					questions.add(q);
				} else CarbonKit.log.warn("A question was found with no answers. This one will not be used:" + s);
			}
		} else CarbonKit.log.warn("It seems that CarbonTrivia has no questions to use! Add some in the config.yml!");
	}
	public String getQuestion() { return question; }
	public List<String> getAnswers() { return answers; }
	public static List<Question> getQuestions() { return new ArrayList<Question>(questions); }
	public static String getQuestion(int i) { return questions.get(i).getQuestion(); }
	public static List<String> getAnswers(int i) { return questions.get(i).getAnswers(); }
	public boolean checkAnswer(String ans) { return getAnswer(ans) != null; }
	public String getAnswer(String ans) {
		for (String a : answers) if(ans.toLowerCase().contains(a.toLowerCase())) return a;
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Question)) return false;
		Question q = (Question)obj;
		return new EqualsBuilder()
				.append(q.getQuestion(), getQuestion())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getQuestion())
				.toHashCode();
	}
}
