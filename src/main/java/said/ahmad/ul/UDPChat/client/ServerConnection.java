package said.ahmad.ul.UDPChat.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.util.Pair;
import said.ahmad.ul.UDPChat.Command;
import said.ahmad.ul.UDPChat.ServerReply;
import said.ahmad.ul.UDPChat.UDPConstraintsException;

public class ServerConnection {
	private int timeOutToReconnect = 5;

	// server parameter needed to prepare DatagramPacket for sending command
	// to server
	private InetAddress host;
	private int port;
	// needed to send and receive DatagramPacket
	private DatagramSocket clientSocket;

	private Thread serverListener;
	private ServerLogger logger;
	private LinkedBlockingQueue<Pair<ServerReply, String>> logs;

	private String clientName;
	boolean expectingServerReply = false;

	public ServerConnection(InetAddress serverHost, int serverPort, DatagramSocket clientSocket) throws IOException {
		host = serverHost;
		port = serverPort;
		this.clientSocket = clientSocket;
		logs = new LinkedBlockingQueue<>();
	}

	public void startListening() {
		if (serverListener == null || serverListener.isInterrupted()) {
			serverListener = new Thread(() -> {
				while (true) {
					try {
						readReply();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			serverListener.start();
		}
	}

	public void stopListeting() {
		if (serverListener != null) {
			serverListener.interrupt();
		}
	}

	/** Note this will show messages from the server on system output */
	public void startThreadLogging() {
		logs.clear();
		if (logger != null) {
			logger.interrupt();
			logger = null;
		}
		logger = new ServerLogger(this);
		logger.start();
	}

	public void stopLogging() {
		if (logger != null) {
			logger.interrupt();
		}
	}

	public String getRegisterCommand(String clientName) {
		return Command.ENREGISTRER + ":" + clientName;
	}

	public void sendCommand(String command) throws IOException, UDPConstraintsException {
		if (expectingServerReply) {
			throw new UDPConstraintsException();
		}
		byte buf[] = command.getBytes(StandardCharsets.UTF_8);
		DatagramPacket toBeSend = new DatagramPacket(buf, buf.length, host, port);
		clientSocket.send(toBeSend);
		expectingServerReply = true;
	}

	public Pair<ServerReply, String> readReply() throws IOException {
		int sleepTimeOut = 0;
		boolean doReconnect = false;
		while (true) {
			try {
				if (sleepTimeOut != 0) {
					doReconnect = false;
					System.out.println("Reconneting in " + sleepTimeOut + " seconds");
					sleepTimeOut--;
					Thread.sleep(1000);
					if (sleepTimeOut == 0) {
						if (getClientName() != null) {
							expectingServerReply = false;
							sendCommand(getRegisterCommand(getClientName()));
						}
					}
					continue;
				}

				Pair<ServerReply, String> fullReply = null;

				byte buf[] = new byte[1024];
				DatagramPacket replyFromServer = new DatagramPacket(buf, buf.length);
				clientSocket.receive(replyFromServer);

				String replyMessageFromServer = new String(replyFromServer.getData(), 0,
						replyFromServer.getData().length, StandardCharsets.UTF_8).trim();
				String splitted[] = replyMessageFromServer.split(" ", 2);
				String reply = splitted[0];
				String message = splitted.length > 1 ? splitted[1] : "";
				ServerReply serverReply;
				try {
					serverReply = ServerReply.valueOfIgnoringCase(reply);
				} catch (Exception e) {
					serverReply = ServerReply.ERROR;
				}
				if (!serverReply.equals(ServerReply.INFO)) {
					expectingServerReply = false;
				}
				fullReply = new Pair<ServerReply, String>(serverReply, message);
				logs.add(fullReply);
				return fullReply;

			} catch (SocketException e) {
				if (e.getMessage().equals("Socket closed")) {
					// ignore socket closed exception and stop logging
					break;
				} else {
					e.printStackTrace();
					doReconnect = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
				doReconnect = true;
			} catch (InterruptedException e) {
				break;
			} catch (UDPConstraintsException e) {
				break;
			}
			if (doReconnect) {
				sleepTimeOut = timeOutToReconnect;
				System.err.println("Connection to server lost!");
			}
		}
		return new Pair<ServerReply, String>(ServerReply.ERROR, "Could not retrieve server reply ");
	}

	public void close() throws IOException {
		stopListeting();
		stopLogging();
		clientSocket.close();
	}

	/**
	 * @return the clientName
	 */
	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	/**
	 * @return the logs
	 */
	public LinkedBlockingQueue<Pair<ServerReply, String>> getLogs() {
		return logs;
	}
}
