package os.chat.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import os.chat.client.CommandsFromServer;

/**
 * Each instance of this class is a server for one room.
 * <p>
 * At first there is only one room server, and the names of the room available
 * is fixed.
 * <p>
 * Later you will have multiple room server, each managed by its own
 * <code>ChatServer</code>. A {@link ChatServerManager} will then be responsible
 * for creating and adding new rooms.
 */
public class ChatServer implements ChatServerInterface {
	
	private String roomName;
	private Vector<CommandsFromServer> registeredClients;
	
	// single client skeleton
	private CommandsFromServer client;
	
	// declare the registry
	private Registry registry;
  /**
   * Constructs and initializes the chat room before registering it to the RMI
   * registry.
   * @param roomName the name of the chat room
   */
	public ChatServer(String roomName){
		this.roomName = roomName;
		registeredClients = new Vector<CommandsFromServer>();
		
		try {
			// create the stub
			ChatServerInterface stub = (ChatServerInterface)UnicastRemoteObject.exportObject(this, 0);
			registry = LocateRegistry.getRegistry(1099);
			registry.rebind("room_" + roomName, stub);
		} catch (RemoteException e) {
			System.err.println("can not export the object");
			e.printStackTrace();
		}
		System.out.println("ChatServer was created");
	}

	/**
	 * Publishes to all subscribed clients (i.e. all clients registered to a
	 * chat room) a message send from a client.
	 * @param message the message to propagate
	 * @param publisher the client from which the message originates
	 */	
	public void publish(String message, String publisher) {
		// iterating through all the registered clients to retrieve their messages
		for (int i = 0; i < registeredClients.size(); i++) {
			client = registeredClients.get(i);
			try {
				client.receiveMsg(roomName, publisher + ": " + message);
				System.out.println("  [server] publishing '" + message + "' from '" + publisher + "'");
			} catch (RemoteException e) {
				System.err.println("Can not receive message from client");
				unregister(client);
				i--; // stay inbound because of the change of size
				System.out.println("user unregistered from room " + roomName);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Registers a new client to the chat room.
	 * @param client the name of the client as registered with the RMI
	 * registry
	 */
	public void register(CommandsFromServer client) {
		registeredClients.add(client);
	}

	/**
	 * Unregisters a client from the chat room.
	 * @param client the name of the client as registered with the RMI
	 * registry
	 */
	public void unregister(CommandsFromServer client) {
		registeredClients.remove(client);
	}
	
}
