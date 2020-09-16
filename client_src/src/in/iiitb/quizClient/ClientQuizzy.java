package in.iiitb.quizClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ClientQuizzy extends Thread {
	private static final String separator = "|@#@|";
	
	private String userName;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private QuizzyMode mode;
	private Map<String, String> scores;
	private ClientListener clientListener;
	public boolean completed;

	public ClientQuizzy(Socket _socket) throws Exception {
		this.socket = _socket;
		this.out = new PrintWriter(this.socket.getOutputStream());
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.mode = QuizzyMode.Begin;
		this.scores = new HashMap<String, String>();
		
		System.out.print("Enter Your Name: ");
		// Start client listener to read stdin messages from the user
		this.clientListener = new ClientListener(this);
		this.clientListener.start();
	}

	public void sendMessage(String _message) throws Exception {
		this.out.println(_message);
		this.out.flush();
	}
	
	public QuizzyMode getMode() {
		return this.mode;
	}
	
	public void run() {
		while(true) {
			try {
				String text = this.in.readLine();
				this.processMessage(text);
			} catch (Exception e) {
				if (this.completed)
					break;
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void processUserMessage(String _text) throws Exception {
		switch (this.mode) {
		case Begin:
			// Got name of the user
			// send it to server
			this.userName = _text;
			this.sendMessage("N:" + _text);
			this.mode = QuizzyMode.WaitForNextQuestion;
			System.out.println("Please wait ...");
			break;
		case QuestionRead:
			if (_text.startsWith("Y")) {
				this.sendMessage("B:Y");
				this.mode = QuizzyMode.Buzzer;
			}
			break;
		case Buzzer:
			try {
				int answer = Integer.parseInt(_text);
				this.sendMessage("A:" + answer);
			} catch(Exception e) {
				System.out.println("Please enter valid number: ");
			}
		}
	}
	
	/**
	 * Messages received from server to process
	 * @param _text
	 * @throws Exception
	 */
	public synchronized void processMessage(String _text) throws Exception {
		switch(this.mode) {
		
		default:
			if (_text.startsWith("Q:")) {
				this.processQuestion(_text.substring(2));
				this.mode = QuizzyMode.QuestionRead;
			} else if (_text.startsWith("S:")) {
				this.processScore(_text.substring(2), false);
			} else if (_text.startsWith("R:")) {
				this.processScore(_text.substring(2), true);
				this.completed = true;
				System.exit(0);
			} else if (_text.startsWith("W:")) {
				this.mode = QuizzyMode.WaitForNextQuestion;
				this.displayScores();
				System.out.println("\nPlease wait for next question ...");
			} else if (_text.startsWith("A:")) {
				this.mode = QuizzyMode.Buzzer;
				
				System.out.print("Enter Answer: ");
			}  
			
			break;
		}
	}

	private void processScore(String _text, boolean _testCompleted) {
		// <name><sep><score><sep><name>...
		StringTokenizer tokens = new StringTokenizer(_text, separator);
		
		this.scores.clear();
		
		String winner = "";
		int highScore = 0;
		
		while (tokens.hasMoreTokens()) {
			String name = tokens.nextToken();
			int score = Integer.parseInt(tokens.nextToken());
			
			if (score > highScore) {
				highScore = score;
				winner = name;
			}
			
			this.scores.put(name, score+"");
		}
		
		this.displayScores();
		
		if (_testCompleted) {
			if (winner.equals(this.userName)) {
				System.out.println("Congratulations for winning the competition !!!");
			} else {
				System.out.println("Sorry for not winning the contest. Next time better luck.");
			}
		}
	}

	private void displayScores() {
		// Clear screen
		//System.out.print("\033[H\033[2J");
		//Based on OS clear line.
	    //System.out.flush();
	    
	    if (this.scores.size() == 0)
	    	return;
	    
	    System.out.println("Scores:");
	    
	    for (String name : this.scores.keySet()) {
	    	String score = this.scores.get(name);
	    	
	    	System.out.println(name + " : " + score);
	    }
	}

	private void processQuestion(String _text) {
		// <description><sep><option 1><sep><option 2><sep>....
		StringTokenizer tokens = new StringTokenizer(_text, separator);
		
		String description = tokens.nextToken();
		
		System.out.println("\n\n" + description);
		
		int oNum = 1;
		
		while (tokens.hasMoreTokens()) {
			String option = tokens.nextToken();
			
			System.out.println(oNum++ + ". " + option);
		}
		
		System.out.print("\nType 'Y' to click the buzzer: ");
	}
}
