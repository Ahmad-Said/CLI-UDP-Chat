package said.ahmad.ul.UDPChat;

public class UDPConstraintsException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -3611566426921358886L;

	public UDPConstraintsException() {
		super("Could not send new commands! Expecting server reply first on your previous command");
	}

}
