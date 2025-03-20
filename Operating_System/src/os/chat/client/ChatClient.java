package os.chat.client;


import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Vector;

import os.chat.server.ChatServerInterface;
import os.chat.server.ChatServerManagerInterface;
/**
 * This class implements a chat client that can be run locally or remotely to
 * communicate with a {@link ChatServer} using RMI.
 */
public class ChatClient implements CommandsFromWindow,CommandsFromServer {
	
	// declaring the skelton
	private CommandsFromServer skeleton;
	
	// reference to the remote instance of ChatServerManager and declaration of the registry
	private ChatServerManagerInterface chatServerManager;
	private Registry registry;
	
	// hash map with the rooms and the stub for each room
	private HashMap<String, ChatServerInterface> myRooms;
	/**
	 * The name of the user of this client
	 */
	private String userName;
	
	// host ip address
	private String hostIP;
  /**
   * The graphical user interface, accessed through its interface. In return,
   * the GUI will use the CommandsFromWindow interface to call methods to the
   * ChatClient implementation.
   */
	private final CommandsToWindow window ;
	
  /**
   * Constructor for the <code>ChatClient</code>. Must perform the connection to the
   * server. If the connection is not successful, it must exit with an error.
   * 
   * @param window reference to the GUI operating the chat client
   * @param userName the name of the user for this client
   * @since Q1
   */
	public ChatClient(CommandsToWindow window, String userName) {
		this.window = window;
		this.userName = userName;
		myRooms = new HashMap<String, ChatServerInterface> ();
		
		// get ip
		try {
			this.hostIP = Inet4Address.getLocalHost().getHostAddress();
			System.out.println("Host IP: " + hostIP);
		} catch (UnknownHostException e) {
			System.err.println("Could not get host IP Address");
			e.printStackTrace();
		}
		try {
			// retrieve the registry
			registry = LocateRegistry.getRegistry(hostIP);
			//lookup for the reference to the ChatServerManager stub
			chatServerManager = (ChatServerManagerInterface)registry.lookup("ChatServerManager");
		} catch (RemoteException e) {
			System.err.println("can not locate registry");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("can not lookup for ChatServerManager");
			e.printStackTrace();
		}

		try {
			skeleton = (CommandsFromServer)UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			System.err.println("can not export the object");
			e.printStackTrace();
		}
		System.out.printf("%s skeleton was created\n", userName);
	}

	/*
	 * Implementation of the functions from the CommandsFromWindow interface.
	 * See methods description in the interface definition.
	 */

	/**
	 * Sends a new <code>message</code> to the server to propagate to all clients
	 * registered to the chat room <code>roomName</code>.
	 * @param roomName the chat room name
	 * @param message the message to send to the chat room on the server
	 */
	public void sendText(String roomName, String message) {
		try {
			// calling the publish function from chatServer
			myRooms.get(roomName).publish(message, userName);
		} catch (RemoteException e) {
			System.err.println("Can not publish this text: " + message);
		}
	}

	/**
	 * Retrieves the list of chat rooms from the server (as a {@link Vector}
	 * of {@link String}s)
	 * @return a list of available chat rooms or an empty Vector if there is
	 * none, or if the server is unavailable
	 * @see Vector
	 */
	public Vector<String> getChatRoomsList() {
		
		// Call the getRoomsList() method from the ChatServerManager with RMI
 		try {
			return chatServerManager.getRoomsList();
		} catch (RemoteException e) {
			System.err.println("can not call ChatServerManager.getRoomsList()");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Join the chat room. Does not leave previously joined chat rooms. To
	 * join a chat room we need to know only the chat room's name.
	 * @param name the name (unique identifier) of the chat room
	 * @return <code>true</code> if joining the chat room was successful,
	 * <code>false</code> otherwise
	 */
	public boolean joinChatRoom(String roomName) {
		
		try {
			myRooms.put(roomName, (ChatServerInterface)registry.lookup("room_" + roomName));
			myRooms.get(roomName).register(skeleton);
		} catch (NotBoundException e) {
			System.err.printf("can not lookup for %s\n", "room_" + roomName);
			e.printStackTrace();
			return false;
		} catch (RemoteException e) {
			System.err.printf("%s can not register to \"%s\" chat room\n", userName, roomName);
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Leaves the chat room with the specified name
	 * <code>roomName</code>. The operation has no effect if has not
	 * previously joined the chat room.
	 * @param roomName the name (unique identifier) of the chat room
	 * @return <code>true</code> if leaving the chat room was successful,
	 * <code>false</code> otherwise
	 */	
	public boolean leaveChatRoom(String roomName) {
		
		try {
			myRooms.get(roomName).unregister(skeleton);
		} catch (RemoteException e) {
			System.err.printf("%s can not unregister to \"%s\" chat room\n", userName, roomName);
			e.printStackTrace();
		}		
		return true;
	}

    /**
     * Creates a new room named <code>roomName</code> on the server.
     * @param roomName the chat room name
     * @return <code>true</code> if chat room was successfully created,
     * <code>false</code> otherwise.
     */
	public boolean createNewRoom(String roomName) {
		// Create a new room calling the createRoom method from chat server manager
		try {
			chatServerManager.createRoom(roomName);
			return true;
		} catch (RemoteException e) {
			System.err.println("can not create the " + roomName + " room");
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * Implementation of the functions from the CommandsFromServer interface.
	 * See methods description in the interface definition.
	 */
	
	
	/**
	 * Publish a <code>message</code> in the chat room <code>roomName</code>
	 * of the GUI interface. This method acts as a proxy for the
	 * {@link CommandsToWindow#publish(String chatName, String message)}
	 * interface i.e., when the server calls this method, the {@link
	 * ChatClient} calls the 
	 * {@link CommandsToWindow#publish(String chatName, String message)} method 
	 * of it's window to display the message.
	 * @param roomName the name of the chat room
	 * @param message the message to display
	 */
	public void receiveMsg(String roomName, String message) {
		// publish the message to the chat window
		window.publish(roomName, message);
	}
	
	public String getUserName() {
		return userName;
	}
	// This class does not contain a main method. You should launch the whole program by launching ChatClientWindow's main method.
}
