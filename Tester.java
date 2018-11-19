import java.util.ArrayList;
import java.util.Random;
import java.lang.StringBuilder;
import java.text.Normalizer;
import java.util.Scanner;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.jsoup.helper.*;
import org.jsoup.internal.*;
import org.jsoup.parser.*;
import org.jsoup.safety.*;

public class Tester {
	public static void main(String[] args)
	{
		
			Question q = new Question();
			System.out.println(q);
		    System.out.println(q.getAnswerURL());
		
		
	}
	
	public static ArrayList<String> getPages()
	{
		return null;
	}
	
	public static String phraseQuestion(String q)
	{
		Scanner s = new Scanner(q);
		ArrayList<String> words = new ArrayList<>();
		while(s.hasNext())
		{
			words.add(s.next());
		}
		
		for(int i = 0; i<words.size(); i++)
		{
			if(i != 0 && words.get(i-1).equalsIgnoreCase("the") && words.get(i).contains("***"))
				words.set(i, "____");
			
			if(words.get(i).contains("***"))
			{
				if(words.get(i+1).contains("are"))
					words.set(i, words.get(i).replace("***", "these"));
				else
					words.set(i, words.get(i).replace("***", "this"));
			}
				
		}
		if(words.get(0).charAt(0) == 't')
			words.set(0, words.get(0).replaceFirst("t", "T"));
		
		
		String phrased = String.join(" ", words);
		return phrased;
	}
	
	public static String blockAnswer(Element e)
	{
		String s = e + "";
		s = s.substring(3, s.length()-4);
		ArrayList<Integer> bTagIndeces = new ArrayList<>();
		
		while(s.contains("<b>"))
		{
			int bTagStart = s.indexOf("<b>");
			int bTagEnd = s.indexOf("</b>");
			String sub = s.substring(bTagStart, bTagEnd+4);
			bTagIndeces.add(bTagStart + 3);
			s = s.replace(sub, "");
			s = new StringBuilder(s).insert(bTagStart, "***").toString();
			
		}
		s = Jsoup.parse(s).text();
		s = s.replaceAll("\\[[0-9]\\]", "");
		s = s.replaceAll("\\s*\\([^\\)]*\\)\\s*", " ");
		
		String formatted = s;
		if(formatted.contains("No."))
			s = s.substring(0, s.indexOf(".", s.indexOf(".") + 1));
		else
		{
			try
			{
				s = s.substring(0, s.indexOf(".")); 
			}
			catch(StringIndexOutOfBoundsException exc)
			{
				//Leave s unaltered
			}
			
		}
			

		s = Normalizer.normalize(s, Normalizer.Form.NFC);
		return s;
	}
	

}


