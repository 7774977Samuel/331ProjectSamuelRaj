package commandserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler {
	// The client socket
	private Socket client;

	// The input stream reader
	private BufferedReader clientInput;

	// The output stream writer
	private PrintWriter clientOutput;

	/**
	 * ClientHandler constructor
	 * 
	 * @param clientIn
	 *            the client socket
	 * @throws IOException
	 */
	public ClientHandler(Socket clientIn) throws IOException {
		client = clientIn;
		// Create the reader/writer
		clientInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
		clientOutput = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);

	}

	/**
	 * Handle the client
	 * 
	 * @throws IOException
	 */
	public void handleClient() throws IOException {
		boolean isConnected = true;

		System.out.println(Thread.currentThread() + " Handling new client connection: " + client);

		// Send the welcome message to the client
		sendMessage("Welcome to my CommandServer!");

		while (isConnected) {
			// Read a message from the client
			String message = clientInput.readLine();

			System.out.println(Thread.currentThread() + " Received message: " + message);

			// Get the first word (eg. 'echo hello world', first word is 'echo')
			String command = message.split(" ")[0].toUpperCase();

			if (command.equals("DISCONNECT")) {
				sendMessage("Bye!");
				// Set the isConnected flag to false to cause the while loop to
				// stop
				isConnected = false;
			}

			else if (command.equals("ECHO")) {
				// Get everything after the first word (eg. 'echo hello world',
				// will be 'hello world')
				String echoMessage = message.substring(message.indexOf(" ") + 1);
				// Echo it back
				sendMessage(echoMessage);
			}

			else if (command.equals("ADD")) {
				// Split the message around the space characters
				// eg. For input "add 1 3"
				// parameters[0] = "add"
				// parameters[1] = "1"
				// parameters[2] = "3"
				String[] parameters = message.split(" ");

				try {
					// Use Integer.parseInt to convert from String to int
					int a = Integer.parseInt(parameters[1]);
					int b = Integer.parseInt(parameters[2]);
					
					int result = a + b;

					sendMessage(a + " + " + b + " = " + result);
				} catch (NumberFormatException ex) {
					//An error occured parsing the values
					sendMessage("Unable to parse value: " + ex);
				}

				
			}
			else if (command.equals("SUBTRACT")) {
				// Split the message around the space characters
				// eg. For input "add 1 3"
				// parameters[0] = "add"
				// parameters[1] = "1"
				// parameters[2] = "3"
				String[] parameters = message.split(" ");

				try {
					// Use Integer.parseInt to convert from String to int
					int a = Integer.parseInt(parameters[1]);
					int b = Integer.parseInt(parameters[2]);
					
					int result = a - b;

					sendMessage(a + " - " + b + " = " + result);
				} catch (NumberFormatException ex) {
					//An error occured parsing the values
					sendMessage("Unable to parse value: " + ex);
				}

				
			}
			

			else if (command.equals("MULTIPLY")) {
				// Split the message around the space characters
				// eg. For input "add 1 3"
				// parameters[0] = "add"
				// parameters[1] = "1"
				// parameters[2] = "3"
				String[] parameters = message.split(" ");

				try {
					// Use Integer.parseInt to convert from String to int
					int a = Integer.parseInt(parameters[1]);
					int b = Integer.parseInt(parameters[2]);
					
					int result = a * b;

					sendMessage(a + " x " + b + " = " + result);
				} catch (NumberFormatException ex) {
					//An error occured parsing the values
					sendMessage("Unable to parse value: " + ex);
				}

				
			}

			else {
				sendMessage("Unknown command [" + command + "]");
			}

		}

		// Close in/outputs
		clientOutput.close();
		clientInput.close();

		System.out.println(Thread.currentThread() + " Closing client connection: " + client);

		// Close the socket
		client.close();
	}

	/**
	 * Send a String message to the connected client
	 * 
	 * @param message
	 *            the message to send
	 */
	private void sendMessage(String message) {
		clientOutput.println("SERVER: " + message);
	}}