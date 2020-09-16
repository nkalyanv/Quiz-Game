package in.iiitb.quizServer;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class QuizMaster {
	private static final String separator = "|@#@|";
	private static final int TOTAL_TESTERS = 2;
	private static final int MIN_SCORE = 3;
	public static boolean COMPLETED = false;
	
	private static QuizMaster instance;
	private List<QuizQuestion> questions;
	private List<Quizzy> quizzies;
	private int joinedCount;	// Quizzies entered their names
	private Random rand;
	private Quizzy buzzedQuizzy;
	private QuizQuestion currentQuestion;
	private Quizzy highScorer;
	
	private QuizMaster() {
		this.quizzies = new ArrayList<Quizzy>();
		this.joinedCount = 0;
		this.rand = new Random(System.nanoTime());
		this.questions = QuizQuestion.getAllQuestion();
	}
	
	public static synchronized QuizMaster getInstance() {
		if (instance == null)
			instance = new QuizMaster();
		
		return instance;
	}
	
	public void addClient(Socket _socket) throws Exception {
		if (this.quizzies.size() >= TOTAL_TESTERS)
			return;
		
		Quizzy q = new Quizzy(_socket, this);
		
		this.quizzies.add(q);
		
		q.start();	// Run in a separate thread
	}

	public void processMessage(String text, Quizzy quizzy) throws Exception {
		if (text == null)
			return;
		
		text = text.trim();
		
		switch (quizzy.getMode()) {
		case Begin:	// waiting for name
			if (text.startsWith("N:")) {
				quizzy.setUserName(text.substring(2));
				
				System.out.println(new Date().toString() + ": Got user name: " + text.substring(2));
				
				this.joinedCount++;
				
				if (this.joinedCount == TOTAL_TESTERS) {
					this.broadcastQuestion();
				}
			}
			
			break;
		case QuestionRead:	// Question sent, waiting for buzzer
			if (text.startsWith("B:")) {
				this.processBuzzer(quizzy);
			}
			break;
			
		case Buzzer:
			if (text.startsWith("A:")) {
				this.processAnswer(text, quizzy);
			}
			break;
		}
	}

	private void processAnswer(String _text, Quizzy _quizzy) throws Exception {
		System.out.println(new Date().toString() + ": Got answer: " + _text.substring(2) + ", from: " + _quizzy.getUserName());
		
		// text: A:<answer index>
		String strAnswerIndex = _text.substring(2);
		int answerIndex = Integer.parseInt(strAnswerIndex);
		
		if (this.currentQuestion.getAnswer() == answerIndex) {
			// correct answer
			int score = _quizzy.incrementScore();
			
			// Set high scorer
			if (this.highScorer == null)
				this.highScorer = _quizzy;
			else if (this.highScorer.getScore() < score)
				this.highScorer = _quizzy;
		}
		
		
		// Check if we're done with the test
		// Check if anyone got 5 or more
		// Check if questions are empty
		if (this.questions.isEmpty() || (this.highScorer != null && this.highScorer.getScore() >= MIN_SCORE)) {
			System.out.println(new Date().toString() + ": Sending the final results");
			
			this.broadcastScores(true);
			
			// End the process
			System.out.println("Test is completed. Have a good day!");
			COMPLETED = true;
			Thread.sleep(10000);
			System.exit(0);
		} else {

			// broadcast scores
			this.broadcastScores(false);
			this.broadcastQuestion();
		}
	}

	private void broadcastScores(boolean _testCompleted) throws Exception {
		System.out.println(new Date().toString() + ": Sending the scores");
		
		StringBuilder text = new StringBuilder(256);
		
		if (_testCompleted)
			text.append("R:");	// End of test, publish results
		else
			text.append("S:");	// More questions to go, publish current scores
		
		for (int i = 0; i < this.quizzies.size(); i++) {
			Quizzy q = this.quizzies.get(i);
			text.append(separator + q.getUserName() + separator + q.getScore());
		}
		
		for (int i = 0; i < this.quizzies.size(); i++) {
			Quizzy q = this.quizzies.get(i);
			q.sendMessage(text.toString());
		}
	}

	private synchronized void processBuzzer(Quizzy _quizzy) throws Exception {
		System.out.println(new Date().toString() + ": Recived the buzzer: " + _quizzy.getUserName());
		
		if (this.buzzedQuizzy == null) {
			// First user
			this.buzzedQuizzy = _quizzy;
			_quizzy.setMode(QuizzyMode.Buzzer);
			
			System.out.println(new Date().toString() + ": Buzzer accepted from " + _quizzy.getUserName());
			
			for (int i = 0; i < this.quizzies.size(); i++) {
				Quizzy q = this.quizzies.get(i);
				
				if (q == _quizzy) {	// skip the current buzzer
					q.sendMessage("A:Y"); // Prompt the client to enter answer
					continue;
				}
				
				q.setMode(QuizzyMode.WaitForNextQuestion);
				q.sendMessage("W:Wait for next question");
			}
		}
	}

	private void broadcastQuestion() throws Exception {
		System.out.println(new Date().toString() + ": Sending question");
		
		// Get random number
		int qIndex = this.rand.nextInt(this.questions.size());
		
		this.currentQuestion = this.questions.remove(qIndex);
		
		String qText = String.format("Q:%s", currentQuestion.getDescription());
		List<String> options = currentQuestion.getChoices();
		
		for (int i = 0; i < options.size(); i++) {
			qText += separator + options.get(i);
		}
		
		// reset previous buzzer
		this.buzzedQuizzy = null;
		
		for (int i = 0; i < this.quizzies.size(); i++) {
			Quizzy q = this.quizzies.get(i);
			q.setMode(QuizzyMode.QuestionRead);
			q.sendMessage(qText);
		}
	}
}
