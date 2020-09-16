package in.iiitb.quizServer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class QuizQuestion {
	private static List<QuizQuestion> allQuestions;
	
	private String description;
	private List<String> choices;
	private int answerIndex;
	
	public QuizQuestion(Element _el) throws IllegalArgumentException {
		this.choices = new ArrayList<String>();
		
		this.parse(_el);
	}
	
	public static void parseFile(File _questionFile) throws Exception {
		allQuestions = new ArrayList<QuizQuestion>();
	
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Load the input XML document, parse it and return an instance of the
        // Document class.
        Document document = builder.parse(_questionFile);
        
        Element root = document.getDocumentElement();
        
        NodeList nodeList = root.getElementsByTagName("Question");
        
        if (nodeList == null || nodeList.getLength() < 3) {
			throw new IllegalArgumentException("Invalid xml, check Question");
		}
        
        for (int i = 0; i < nodeList.getLength(); i++) {
        	Element el = (Element) nodeList.item(i);
        	
        	QuizQuestion q = new QuizQuestion(el);
        	allQuestions.add(q);
        }
	}
	
	private void parse(Element _el) throws IllegalArgumentException {
		/*
		 * <Question ans="1">
		 * 	<Description><!CDATA[question text]></Description>
		 * 	<Options>
		 * 		<Option>option 1</Option>
		 * 		<Option>option 2</Option>
		 * 		<Option>option 3</Option>
		 * 	</Options>
		 * </Question>
		 */
		
		String temp = _el.getAttribute("ans");
		
		if (temp == null || temp.trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid xml, ans not specified");
		}
		
		this.answerIndex = Integer.parseInt(temp);
		
		// Parse description
		NodeList nodeList = _el.getElementsByTagName("Description");
		
		if (nodeList == null || nodeList.getLength() != 1) {
			throw new IllegalArgumentException("Invalid xml, check Description");
		}
		
		this.description = nodeList.item(0).getTextContent();
		
		// Parse Options
		nodeList = _el.getElementsByTagName("Option");
		
		if (nodeList == null || nodeList.getLength() < 2) {
			throw new IllegalArgumentException("Invalid xml, check Options");
		}
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			String option = nodeList.item(i).getTextContent();
			
			if (option == null || option.trim().isEmpty()) {
				throw new IllegalArgumentException("Invalid xml, check option");
			}
			
			this.choices.add(option.trim());
		}
	}
	
	public static List<QuizQuestion> getAllQuestion() {
		return new ArrayList<QuizQuestion>(allQuestions);
	}

	public String getDescription() {
		return this.description;
	}
	
	public List<String> getChoices() {
		return this.choices;
	}
	
	public int getAnswer() {
		return this.answerIndex;
	}
	
	private static void main(String [] args) {
		File file = new File("L:\\Work\\Java\\QuizServer\\out\\QuestionBank.xml");
		
		try {
			QuizQuestion.parseFile(file);
			
			List<QuizQuestion> questions = QuizQuestion.getAllQuestion();
			
			for (int i = 0; i < questions.size(); i++) {
				QuizQuestion q = questions.get(i);
				
				System.out.println("Question: " + q.description + ", choices: " + q.choices + ", ans: " + q.answerIndex);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
