/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package said.ahmad.ul.UDPChat.client;

import static java.lang.System.exit;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Scanner;

import javafx.util.Pair;
import said.ahmad.ul.UDPChat.Command;
import said.ahmad.ul.UDPChat.ServerInfo;
import said.ahmad.ul.UDPChat.ServerReply;
import said.ahmad.ul.UDPChat.UDPConstraintsException;

public class Client {
	public static String serverHost = "localhost";
	public static final int SERVER_PORT = ServerInfo.SERVER_PORT;

	private DatagramSocket clientUDPSocket;
	public int clientLocalPort;

	ServerConnection server;

	public Client() {
		Pair<ServerReply, String> messageFromServer;

		String messageToServer;

		try {
			clientUDPSocket = new DatagramSocket();
			clientLocalPort = clientUDPSocket.getLocalPort();

			System.out.println(
					"Connecting To " + serverHost + ":" + SERVER_PORT + " with source port: " + clientLocalPort);
			server = new ServerConnection(new InetSocketAddress(serverHost, SERVER_PORT).getAddress(), SERVER_PORT,
					clientUDPSocket);
			server.startThreadLogging();
			System.out.println("Connected!");
		} catch (IOException ex) {
			System.err.println("Host not reachable!");
			exit(0);
		}
		try {
			Scanner inputClient = new Scanner(System.in);

			boolean registered = false;

			System.out.print("Please Enter your name: ");

			String clientName = inputClient.nextLine();
			String registerCommand = Command.ENREGISTRER + ":" + clientName;
			try {
				server.sendCommand(registerCommand);
			} catch (UDPConstraintsException e1) {
				e1.printStackTrace();
			}
			messageFromServer = server.readReply();
			ServerReply messageParsedReply = messageFromServer.getKey();

			if (messageParsedReply.equals(ServerReply.ENREGISTREMENT_OK)) {
				// registration successful
				server.setClientName(clientName);
				registered = true;
			} else {
				// registration failed
				System.out.println("registration failed");
				System.exit(0);
				inputClient.close();
				return;
			}

			// Message reception will be inside a thread running function
			server.startListening();

			do {
				messageToServer = inputClient.nextLine();
				try {
					server.sendCommand(messageToServer);
				} catch (UDPConstraintsException e) {
					System.err.println(e.getMessage());
				}
			} while (!Command.END.equalsIgnoringCase(messageToServer));

			// This is the END clause
			inputClient.close();
			System.out.println("Connection is closing...");
			server.close();
			System.out.println("Connection succesfully closed...");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.exit(0);
	}
}
