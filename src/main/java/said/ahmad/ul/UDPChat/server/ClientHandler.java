/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package said.ahmad.ul.UDPChat.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import said.ahmad.ul.UDPChat.Command;
import said.ahmad.ul.UDPChat.CommandFormatException;
import said.ahmad.ul.UDPChat.ServerReply;
import said.ahmad.ul.UDPChat.ServerReplyException;

public class ClientHandler {
	Server server;
	ClientConnectionLess client;

	public ClientHandler(ClientConnectionLess clientConnection, Server server) throws IOException {
		client = clientConnection;
		this.server = server;
		server.addClient(client, this);
	}

	public String getClientHelp() {
		return "Your name is " + client.getName() + "\n"
				+ Stream.of(Command.values()).filter(c -> !c.equals(Command.ENREGISTRER))
						.map(c -> Command.getInfoFormat(c)).collect(Collectors.joining("\n"));
	}

	public void answerRequests(DatagramPacket receivedRequestPacket) {
		String messageFromClient;
		String tokens[];
		Command command;
		try {
			messageFromClient = client.getCommandsSentByClient(receivedRequestPacket);
//				System.out.println(client.name + " send " + messageFromClient);
			tokens = messageFromClient.split(":", 2);
			try {
				command = Command.valueOf(tokens[0].toUpperCase());
			} catch (Exception e) {
				sendReply(ServerReply.ERROR, "Invalid Command! " + Command.getInfoFormat(Command.HELP));
				return;
			}
			if (!client.isRegistered()) {
				if (!command.equals(Command.ENREGISTRER) || tokens.length != 2) {
					sendReply(ServerReply.ERROR,
							"Require Registration first. " + Command.getInfoFormat(Command.ENREGISTRER));
					return;
				}
				if (server.hasClientWithName(tokens[1])) {
					sendReply(ServerReply.ERROR, "A client with such name already exist!\nPlease try another name.");
					return;
				}
				client.setName(tokens[1]);
				sendReply(ServerReply.ENREGISTREMENT_OK,
						"Hello " + client.getName() + ", Welcome to our tiny chat app\n" + getClientHelp());
				client.setRegistered(true);
				System.out.println(client.getName() + " connected!");
				return;
			}
			try {
				switch (command) {
				case HELP:
					sendReply(ServerReply.COMMAND_RECIEVED, getClientHelp());
					break;
				case ENREGISTRER:
					sendReply(ServerReply.COMMAND_RECIEVED,
							"You already registered\n" + "Your name is: " + client.getName());
					break;
				case END:
					closeconnection();
					break;
				case BROADCAST:
					if (tokens.length != 2) {
						throw new CommandFormatException(command);
					}
					sendReply(ServerReply.COMMAND_RECIEVED, null);
					broadcastMessage(tokens[1]);
					break;
				case MESSAGE:
					tokens = messageFromClient.split(":", 3);
					if (tokens.length != 3) {
						throw new CommandFormatException(command);
					}
					sendReply(ServerReply.COMMAND_RECIEVED, null);
					sendMessage(tokens[1], tokens[2]);
					break;
				case LIST:
					sendReply(ServerReply.COMMAND_RECIEVED, server.getClientsList());
					break;
				default:
					sendReply(ServerReply.ERROR, command + " Command is not yet supported\n");
					break;
				}
			} catch (CommandFormatException e) {
				sendReply(ServerReply.ERROR, e.getMessage());
			} catch (ServerReplyException e) {
				sendReply(ServerReply.ERROR, e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// end single reply on request
		} catch (

		IOException ex) {
			ex.printStackTrace();
			System.err.println("Connection to " + client.getName() + " get lost!");
			closeconnection();
		}
	}

	private void sendReply(ServerReply reply, String message) throws IOException {
		server.sendReplyDatagramPacket(client.getReplyPacket(reply, message));
	}

	private void sendMessage(String otherClientName, String message) throws ServerReplyException, IOException {
		server.sendInfoMessage(client.getName(), otherClientName, message);
	}

	private void broadcastMessage(String message) {
		server.broadCastMessage(client.getName(), message);
	}

	public void closeconnection() {
		server.removeClient(client);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (client == null ? 0 : client.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ClientHandler other = (ClientHandler) obj;
		if (client == null) {
			if (other.client != null) {
				return false;
			}
		} else if (!client.equals(other.client)) {
			return false;
		}
		return true;
	}
}
