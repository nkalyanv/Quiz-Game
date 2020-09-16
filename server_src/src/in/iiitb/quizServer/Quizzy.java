package in.iiitb.quizServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Quizzy extends Thread {
	private QuizMaster master;
	private String userName;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private QuizzyMode mode;
	private int score;

	public Quizzy(Socket _socket, QuizMaster _master) throws Exception {
		this.master = _master;
		this.socket = _socket;
		this.out = new PrintWriter(this.socket.getOutputStream());
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.mode = QuizzyMode.Begin;
	}

	public void sendMessage(String _message) throws Exception {
		this.out.println(_message);
		this.out.flush();
	}

	public void setMode(QuizzyMode _mode) {
		this.mode = _mode;
	}
	
	public QuizzyMode getMode() {
		return this.mode;
	}
	
	public void run() {
		while(true) {
			try {
				String text = this.in.readLine();
				this.master.processMessage(text, this);
			} catch (Exception e) {
				if (QuizMaster.COMPLETED)
					break;
				e.printStackTrace();
			}
		}
	}

	public int incrementScore() {
		return ++this.score;
	}
	
	public int getScore() {
		return this.score;
	}

	public void setUserName(String _name) {
		this.userName = _name;
	}
	
	public String getUserName() {
		return this.userName;
	}
}
