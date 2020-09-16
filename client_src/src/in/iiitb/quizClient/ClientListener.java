package in.iiitb.quizClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientListener extends Thread {
	private ClientQuizzy quizzy;
	
	public ClientListener(ClientQuizzy _quizzy) {
		this.quizzy = _quizzy;
	}
	
	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			try {
				String text = in.readLine();
				this.quizzy.processUserMessage(text);
			} catch (Exception e) {
				if (this.quizzy.completed)
					break;
				e.printStackTrace();
			}
		}
	}
}
