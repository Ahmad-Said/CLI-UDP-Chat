package said.ahmad.ul.UDPChat;

public enum Command {
	HELP, ENREGISTRER, END, BROADCAST, MESSAGE, LIST;

	public static String getInfoFormat(Command command) {
		switch (command) {
		case HELP:
			return "To display Commands help, type " + HELP;
		case ENREGISTRER:
			return "To register as new client, type " + ENREGISTRER + ":Client_Name";
		case END:
			return "To close the connection, type " + END;
		case BROADCAST:
			return "To Broadcast a message, type " + BROADCAST + ":your_message";
		case MESSAGE:
			return "To Send a message to specified client, type " + MESSAGE + ":destination_Client_Name:your_message";
		case LIST:
			return "To list all connected clients, type " + LIST;
		default:
			return null;
		}
	}

	public static Command valueOfIgnoringCase(String command) {
		return Command.valueOf(command.toUpperCase());
	}

	public boolean equalsIgnoringCase(String command) {
		return toString().toUpperCase().equals(command.toUpperCase());
	}
}
