/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package said.ahmad.ul.UDPChat.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import said.ahmad.ul.UDPChat.ServerInfo;
import said.ahmad.ul.UDPChat.ServerReply;
import said.ahmad.ul.UDPChat.ServerReplyException;

public class Server {
	private static ExecutorService clientsServerExecutors = Executors.newCachedThreadPool();

	public static final int SERVER_PORT = ServerInfo.SERVER_PORT;

	private DatagramSocket servSock;

	private final HashMap<ClientConnectionLess, ClientHandler> clients = new HashMap<>();

	/**
	 * Start a server and listens for any connection to be made to this socket and
	 * serve it. The server will keep running until interrupted by user.
	 */
	public Server() {
		try {
			servSock = new DatagramSocket(SERVER_PORT);
			System.out.println("Starting server at port " + servSock.getLocalPort());
		} catch (IOException ex) {
			System.out.println("Unable to connecto to this port" + servSock.getLocalPort());
			return;
		}
		startListening();
	}

	private void startListening() {
		while (true) {
			try {
				// listen to clients commands
				byte reciviedBytesBug[] = new byte[1024];
				DatagramPacket request = new DatagramPacket(reciviedBytesBug, reciviedBytesBug.length);
				servSock.receive(request);

				// preparing to send reply
				ClientConnectionLess currentConnectionLess = new ClientConnectionLess(request.getAddress(),
						request.getPort());
				ClientHandler currentClientHandler;
				if (clients.containsKey(currentConnectionLess)) {
					currentClientHandler = clients.get(currentConnectionLess);
				} else {
					currentClientHandler = new ClientHandler(currentConnectionLess, this);
				}
				clientsServerExecutors.execute(() -> currentClientHandler.answerRequests(request));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void removeClient(ClientConnectionLess client) {
		clients.remove(client);
	}

	public void addClient(ClientConnectionLess clientConnection, ClientHandler clientHandler) {
		clients.put(clientConnection, clientHandler);
	}

	public boolean hasClientWithName(String name) {
		return clients.keySet().stream().filter(c -> c.getName().equals(name)).findAny().isPresent();
	}

	public void broadCastMessage(String senderName, String message) {
		clients.keySet().stream().forEach(c -> {
			try {
				sendReplyDatagramPacket(c.getReplyPacket(ServerReply.INFO, senderName + " broadcasted: " + message));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void sendInfoMessage(String sender, String otherClientName, String message)
			throws ServerReplyException, IOException {
		Optional<ClientConnectionLess> otherClient = clients.keySet().stream()
				.filter(c -> c.getName().equals(otherClientName)).findAny();
		if (otherClient.isPresent()) {
			sendReplyDatagramPacket(
					otherClient.get().getReplyPacket(ServerReply.INFO, sender + " saying to you: " + message));
		} else {
			throw new ServerReplyException(otherClientName + "Name not found in the list: " + getClientsList());
		}
	}

	public void sendReplyDatagramPacket(DatagramPacket toBeSendPacket) throws IOException {
		servSock.send(toBeSendPacket);
	}

	public String getClientsList() {
		return "There are " + clients.size() + " clients:\n\t- "
				+ clients.keySet().stream().map(c -> c.getName()).collect(Collectors.joining("\n\t- "));
	}

}
