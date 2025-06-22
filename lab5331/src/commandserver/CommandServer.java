package commandserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CommandServer{

	/**
	 * The ServerSocket to listen on
	 */
	private ServerSocket serverSocket;

	/**
	 * CommandServer constructor
	 * 
	 * @param portIn
	 *            The port to listen on for incoming connections
	 * @throws IOException
	 */
	public CommandServer(int portIn) throws IOException {
		System.out.println("Creating CommandServer on port " + portIn);
		serverSocket = new ServerSocket(portIn);
	}

	/**
	 * Start accepting connections
	 */
	public void start() {
		while (true) {

			try {
				// Accept an incoming connection
				Socket client = serverSocket.accept();
				
				//Create a ClientHandler for the client
				ClientHandler handler = new ClientHandler(client);
				
				//Handle the client
				handler.handleClient();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		CommandServer server = new CommandServer(50050);

		// Start the CommandServer thread (calls run() in a new thread)
		server.start();
	}
}