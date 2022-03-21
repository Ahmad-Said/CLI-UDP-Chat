package said.ahmad.ul.UDPChat.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import said.ahmad.ul.UDPChat.ServerReply;

public class ClientConnectionLess {
	public static final String GUEST_NAME = "Guest";
	// Key to client is his ipAddress and port
	private InetAddress clientAddress;
	private int clientPort;
	private String name;

	// there is no need to create DatagramPacket as class fields
	// as for each reply/send to client there will be creation for a new
	// DatagramPacket
	private boolean registered = false;

	public ClientConnectionLess(InetAddress clientAddress, int clientPort) throws IOException {
		this.clientAddress = clientAddress;
		this.clientPort = clientPort;
		name = GUEST_NAME;
	}

	public String getCommandsSentByClient(DatagramPacket recievedPacket) throws IOException {
		return new String(recievedPacket.getData(), 0, recievedPacket.getData().length, StandardCharsets.UTF_8).trim();
	}

	public DatagramPacket getReplyPacket(ServerReply reply, String message) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.write(reply.toString().getBytes(StandardCharsets.UTF_8));
		if (message != null && !message.isEmpty()) {
			message = " " + message;
			output.write(message.toString().getBytes(StandardCharsets.UTF_8));
		}
		output.close();
		byte buff[] = output.toByteArray();
		return new DatagramPacket(buff, buff.length, clientAddress, clientPort);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the registered
	 */
	public boolean isRegistered() {
		return registered;
	}

	/**
	 * @param registered the registered to set
	 */
	public void setRegistered(boolean registered) {
		this.registered = registered;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (clientAddress == null ? 0 : clientAddress.hashCode());
		result = prime * result + clientPort;
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
		ClientConnectionLess other = (ClientConnectionLess) obj;
		if (clientAddress == null) {
			if (other.clientAddress != null) {
				return false;
			}
		} else if (!clientAddress.equals(other.clientAddress)) {
			return false;
		}
		if (clientPort != other.clientPort) {
			return false;
		}
		return true;
	}
}
