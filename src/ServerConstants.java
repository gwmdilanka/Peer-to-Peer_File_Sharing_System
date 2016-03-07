
public class ServerConstants {
	public static final int CHAT_MESSAGE = 0; //for chat message	
	public static final int CLIENT_LEAVE = 1; //for exiting the service
	public static final int CHAT_BROADCAST = 2; //for broadcasting the message
	public static final int PRIVATE_MESSAGE = 3; // for private message on a selected client	
	public static final int CLIENT_JOIN = 4;  //client currently using the service
	public static final int REGISTER_BROADCAST = 5; //for broadcasting the newly joined client to the other clients
	public static final int CLIENT_PUBLISH = 6; //for publishing files
	public static final int SEND_FILE= 7;//Send file to remote client
	public static final int CLIENT_SEARCH = 8; //A remote client looking for a file will contact the server
	public static final int CLIENT_PING = 9;//A response from the client indicating that the client is still connected to the system
	public static final int SERVER_PING = 10; //Server periodically send ping message to connected clients.	
	public static final int REMOTE_FETCH= 11;//A remote client send a file to another client.	
	public static final int REMOTE_FETCH_ERROR=12;//Send message to client if file not available.	
	public static final int REMOTE_FETCH_SUCCESS = 13;//Remote fetching success message.	
	public static final int NO_SELECTED_CLIENT = -1; //no client selected on the client list for private messaging.
	public static final int FILE_PORT = 14;//Download port number.
}
