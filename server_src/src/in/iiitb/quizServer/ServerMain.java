package in.iiitb.quizServer;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class ServerMain {

	public static void main(String[] args) {
		try {
			if (args.length < 1) {
				System.out.println("Please provide question bank file path");
				return;
			}
			
			initQuestionBank(args[0]);
			initSocket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void initQuestionBank(String _filePath) throws Exception {
		File file = new File(_filePath);
		
		if (!file.exists()) {
			throw new IllegalArgumentException("Question bank file doesn't exist: " + _filePath);
		}
		
		QuizQuestion.parseFile(file);
	}

	private static void initSocket() throws Exception {
		ServerSocket serverSocket = null;
		
		try {
			serverSocket = new ServerSocket(5000);
			int clientCount  = 0;
			
			while (clientCount < 3) {
				Socket socket = serverSocket.accept();
				
				System.out.println(new Date().toString() + ": Got new connection");
				
				QuizMaster.getInstance().addClient(socket);
				
				clientCount++;
			}
			
			System.out.println(new Date().toString() + ": Got all users connected. No more listening for new connections.");
		} finally {
			if (serverSocket != null)
				serverSocket.close();
		}
	}

}
