package said.ahmad.ul.UDPChat;

public class CommandFormatException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = 7686804001396388678L;

	Command command;

	public CommandFormatException(Command command) {
		super();
		this.command = command;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + "Invalid Command Format, Expected: " + Command.getInfoFormat(command);
	}

}
