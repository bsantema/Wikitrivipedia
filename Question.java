import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.jsoup.helper.*;
import org.jsoup.internal.*;
import org.jsoup.parser.*;
import org.jsoup.safety.*;

public class Question {
	private String question;
	private String answerA; // 1
	private String answerB; // 2
	private String answerC; // 3
	private String answerD; // 4

	public String correctAnswer;
	public int correctAnswerNumber; // 1-4

	public String getAnswerURL()
	{
		return "https://en.wikipedia.org/wiki/" + correctAnswer;
	}
	
	public Question(String page, int questionFormat) {

	}

	public Question() // Default: Random page
	{
		while (true) {
			Document doc;
			try {
				boolean invalidArticle = true;
				Elements temp;
				String title = "";
				doc = null;

				while (invalidArticle) {
					doc = Jsoup.connect("https://en.wikipedia.org/wiki/Special:Random").get();
					temp = doc.select("h1.firstHeading");
					title = "";
					Element e = temp.get(0).getElementsByTag("h1").first();
					title = e.text();

					boolean hasTable = false;
					Element i = e;
					// ignore tables
					while (i.hasParent()) {
						i = i.parent();
						if ((e.parent() + "").contains("table"))
							hasTable = true;
					}
					if (hasTable)
						continue;

					if (title.matches(".*\\d+.*")) // a number
						continue;

					if (title.contains("list") || title.contains("List"))
						continue;

					invalidArticle = false;
				}

				Elements text = doc.select("div.mw-parser-output").first().getElementsByTag("p");

				Element e = text.first();
				if ((e + "").contains("mw-empty-elt")) {
					e = text.get(1);
				}
				String s = blockAnswer(e);
				s = phraseQuestion(s);
				question = s;

				title = title.replaceAll("\\s*\\([^\\)]*\\)\\s*", " ");
				// title = phraseQuestion(title);
				correctAnswer = title;

				if ((question.contains("this") || question.contains("This")))// TODO: fix (doesnt work)
				{
					String qn = question.toLowerCase();
					qn = qn.substring(0, qn.indexOf("this")) + qn.substring(qn.indexOf("this") + 4, qn.length());
					if (qn.contains("this")) {
						question = null;
						throw new Exception();
					}
				}
				
				if (question.length() > 150)// If its longer than 150 characters, find a new article
				{
					question = null;
					throw new Exception();
				}

				correctAnswer = title;

				String[] answers = getAnswerChoices(doc.location(), correctAnswer);
				String[] allAnswers = new String[4];
				for (int i = 0; i < 3; i++) {
					allAnswers[i] = answers[i];
				}
				allAnswers[3] = correctAnswer;
				answerA = allAnswers[0];
				answerB = allAnswers[1];
				answerC = allAnswers[2];
				answerD = allAnswers[3];
				answerA = answerA.replaceAll("\\s*\\([^\\)]*\\)\\s*", " ");
				answerB = answerB.replaceAll("\\s*\\([^\\)]*\\)\\s*", " ");
				answerC = answerC.replaceAll("\\s*\\([^\\)]*\\)\\s*", " ");
				answerD = answerD.replaceAll("\\s*\\([^\\)]*\\)\\s*", " ");
				Random rand = new Random();
				int r = rand.nextInt(4); // starts at 0, so add 1

				switch (r) {
				case 0:
					answerA = correctAnswer;
					answerD = allAnswers[0];
					correctAnswerNumber = 1;
					break;
				case 1:
					answerB = correctAnswer;
					answerD = allAnswers[1];
					correctAnswerNumber = 2;
					break;
				case 2:
					answerC = correctAnswer;
					answerD = allAnswers[2];
					correctAnswerNumber = 3;
					break;
				case 3:
					correctAnswerNumber = 4;
					break;
				default:
					throw new Exception();
				}

			} catch (Exception e) {
				// try again
			}
			if (question != null) {
				break;
			}

		}
	}

	private static String phraseQuestion(String q) {
		Scanner s = new Scanner(q);
		ArrayList<String> words = new ArrayList<>();
		while (s.hasNext()) {
			words.add(s.next());
		}

		for (int i = 0; i < words.size(); i++) {
			if (i != 0 && (words.get(i - 1).equalsIgnoreCase("the") || words.get(i - 1).equalsIgnoreCase("a"))
					&& words.get(i).contains("***"))
				words.set(i, "____");

			if (words.get(i).contains("***")) {
				if (words.get(i + 1).contains("are") || words.get(i + 1).contains("were"))
					words.set(i, words.get(i).replace("***", "these"));
				else
					words.set(i, words.get(i).replace("***", "this"));
			}

		}
		int thisCount = 0;
		for(int i = 0; i<words.size(); i++)
		{
			if(words.get(i).contains("this") || words.get(i).contains("these"))
			{
				thisCount++;
				if(thisCount > 1)
				{
					words.set(i, "something else ");
				}
			}
		}
		
		if (words.get(0).charAt(0) == 't')
			words.set(0, words.get(0).replaceFirst("t", "T"));

		String phrased = String.join(" ", words);
		return phrased;
	}

	private static String blockAnswer(Element e) {
		String s = e + "";
		s = s.substring(3, s.length() - 4);
		ArrayList<Integer> bTagIndeces = new ArrayList<>();

		while (s.contains("<b>")) {
			int bTagStart = s.indexOf("<b>");
			int bTagEnd = s.indexOf("</b>");
			String sub = s.substring(bTagStart, bTagEnd + 4);
			bTagIndeces.add(bTagStart + 3);
			s = s.replace(sub, "");
			s = new StringBuilder(s).insert(bTagStart, "***").toString();

		}
		s = Jsoup.parse(s).text();
		s = s.replaceAll("\\[[0-9]\\]", "");
		s = s.replaceAll("\\s*\\([^\\)]*\\)\\s*", " ");

		String formatted = s;
		if (formatted.contains("No."))
			s = s.substring(0, s.indexOf(".", s.indexOf(".") + 1));
		else {
			try {
				s = s.substring(0, s.indexOf("."));
			} catch (StringIndexOutOfBoundsException exc) {
				// Leave s unaltered
			}

		}

		s = Normalizer.normalize(s, Normalizer.Form.NFC);
		s = s.replaceAll("[A-Z]\\.", "");
		return s;
	}

	private static String getRandomAnswerChoice() throws Exception {
		while (true) {
			Document doc;
			doc = Jsoup.connect("https://en.wikipedia.org/wiki/Special:Random").get();
			Elements temp = doc.select("h1.firstHeading");
			String title = "";
			Element e = temp.get(0).getElementsByTag("h1").first();
			title = e.text();

			if (title.matches(".*\\d+.*")) // a number
				continue;

			if (title.contains("list") || title.contains("List"))
				continue;

			if (title.length() > 30)
				continue;

			return title;
		}
	}

	private static String[] getAnswerChoices(String URL, String correctAnswer) throws Exception {
		try {
			String[] choices = new String[3];
			int numChoices = 0;

			ArrayList<String> validChoices = new ArrayList<>();
			ArrayList<String> categories = new ArrayList<>();
			Document doc;
			try {
				doc = Jsoup.connect(URL).get();
				Elements temp = doc.select("div#mw-normal-catlinks.mw-normal-catlinks");
				Element u = temp.get(0).getElementsByTag("ul").first();
				for (int i = 0; i < u.childNodeSize(); i++) {
					categories.add(u.child(i).child(0).attr("href"));
				}
				if (categories.size() == 0) {
					throw new Exception();
				}

			} catch (Exception e) {
				String[] rands = { getRandomAnswerChoice(), getRandomAnswerChoice(), getRandomAnswerChoice() };
				return rands;
			}

			validChoices = getValidChoices(categories.get(0), correctAnswer);
			if (validChoices.size() < 4) {
				for (int i = 0; i < validChoices.size(); i++) {
					choices[i] = validChoices.get(i);
					numChoices++;
				}
			} else {
				return pickElements(3, validChoices);
			}

			validChoices = getValidChoices(categories.get(1), correctAnswer);
			if (validChoices.size() < 4) {
				String[] temp = pickElements(3 - numChoices, validChoices);
				for (int i = numChoices; i < 3; i++) {
					choices[i] = temp[i];
					i++;
				}

			} else {
				return pickElements(3, validChoices);
			}

			return choices;
		} catch (Exception e) {
			String[] rands = { getRandomAnswerChoice(), getRandomAnswerChoice(), getRandomAnswerChoice() };
			return rands;
		}
	}

	public static ArrayList<String> getValidChoices(String categoryPageURL, String correctAnswer) {
		ArrayList<String> validChoices = new ArrayList<>();
		Document doc;
		try {
			doc = Jsoup.connect("https://en.wikipedia.org" + categoryPageURL).get();
			Elements temp = doc.select("li");
			for (Element choice : temp) {
				String s = choice.getElementsByTag("a").first().text();
				validChoices.add(s);
				if (validChoices.size() > 50)
					break;
			}
			// remove invalid choices
			for (String c : validChoices) {

				if (c.matches(".*\\d+.*")) // a number
					validChoices.remove(c);

				if (c.contains("list") || c.contains("List"))
					validChoices.remove(c);

				if (c.contains("Category") || c.contains("category"))
					validChoices.remove(c);

				if (c.equals(correctAnswer))
					validChoices.remove(c);
			}

		} catch (Exception e) {
			if (validChoices.size() != 0) {
				for (String c : validChoices) {

					if (c.matches(".*\\d+.*")) // a number
						validChoices.remove(c);

					if (c.contains("list") || c.contains("List"))
						validChoices.remove(c);

					if (c.contains("Category") || c.contains("category"))
						validChoices.remove(c);

					if (c.equals(correctAnswer))
						validChoices.remove(c);
				}
			}
		}

		return validChoices;
	}

	public static String[] pickElements(int numElements, ArrayList<String> choices) {
		String[] elems = new String[numElements];
		Random rand = new Random();
		for (int i = 0; i < numElements; i++) {
			int r = rand.nextInt(choices.size()); // starts at 0, so add 1
			elems[i] = choices.get(r);
			choices.remove(r);
		}

		return elems;
	}

	public String getCorrectAnswer() {
		return correctAnswer;
	}

	public String getQuestion() {
		return question;
	}

	public String getAnswerA() {
		return answerA;
	}

	public String getAnswerB() {
		return answerB;
	}

	public String getAnswerC() {
		return answerC;
	}

	public String getAnswerD() {
		return answerD;
	}

	public int getCorrectAnswerNumber() {
		return correctAnswerNumber;
	}

	@Override
	public String toString() {
		return question + "\n\nA: " + answerA + "\nB: " + answerB + "\nC: " + answerC + "\nD: " + answerD;
	}
}
