/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package said.ahmad.ul.UDPChat.client;

import javafx.util.Pair;
import said.ahmad.ul.UDPChat.ServerReply;

class ServerLogger extends Thread {
	ServerConnection server;

	public ServerLogger(ServerConnection server) {
		this.server = server;
	}

	@Override
	public void run() {
		Pair<ServerReply, String> message_in;

		while (true) {
			try {
				message_in = server.getLogs().take();
				if (message_in.getValue() != null && !message_in.getValue().isEmpty()) {
					System.out.println(message_in.getValue());
					System.out.println("-----------------------------------------------");
				}
			} catch (InterruptedException e) {
				break;
			}
		}
	}

}
