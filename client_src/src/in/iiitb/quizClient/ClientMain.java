package in.iiitb.quizClient;

import java.net.Socket;

public class ClientMain {

	public static void main(String[] args) {
		String server = "localhost";
		
		if (args.length > 0)
			server = args[0];
		
		Socket socket;
		try {
			socket = new Socket(server, 5000);
			ClientQuizzy q = new ClientQuizzy(socket);
			
			q.start();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
