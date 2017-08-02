import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class NamingServerMain {
	private static ConcurrentHashMap<String, NamingServerUser> storageServers = new ConcurrentHashMap<String, NamingServerUser>();

	public static void main(String[] args) {
		// Starting server
		ServerSocket servSock;
		try {
			servSock = new ServerSocket(Constants.NAMING_SERVER_PORT);
		} catch (IOException e) {
			System.err.println("Can't start naming server");
			return;
		}
		System.out.println("Naming server started");
		File root = new File(Constants.ROOT_FOLDER_NAME);
        if (!root.exists()) {
            if (root.mkdir()) {
                System.out.println("Root directory was created!");
            } else {
                System.err.println("Failed to create root directory!");
            }
        }
		// Accepting new users
		while (true) {
			try {
				Socket newConnection = servSock.accept();
				NamingServerUser user = new NamingServerUser(newConnection);
				user.start();
			} catch (IOException e) {
				System.err.println("Error establishing connection. Reason: " + e.getMessage());
			}
		}
	}
	
	public static void addStorageServer(String storageServerAddress, NamingServerUser user){
		storageServers.put(storageServerAddress, user);
	}
	
	public static NamingServerUser getStorageServerByAddress(String storageServerAddress){
		return storageServers.get(storageServerAddress);
	}
	
	public static void removeStorageServer(NamingServerUser user){
		String key = null;
		for(String k : storageServers.keySet()){
			if(user.equals(storageServers.get(k))){
				key = k;
				break;
			}
		}
		
		if(key != null){
			storageServers.remove(key);
		}
	}
	
	public static String getAvailableStorageServerForWriting(){
		//TODO: make it choose according to available memory
		Enumeration<String> addresses = storageServers.keys();
		int rand = new Random().nextInt(storageServers.size());
		while(addresses.hasMoreElements()){
			String address = addresses.nextElement();
			rand--;
			if(rand <= 0){
				return address;
			}
		}
		
		return null;
	}

//	synchronized static boolean removeUser(User u) {
//		for(User user : users){
//			if (user.equals(u)) {
//				users.remove(user);
//				System.out.println("User " + user.username + " removed");
//				return true;
//			}
//		}
//
//		return false;
//	}
//
//	synchronized static boolean usernameIsFree(String username) {
//		boolean result = true;
//		for (User u : users) {
//			if (u.username.equals(username)) {
//				result = false;
//				break;
//			}
//		}
//		return result;
//	}
//	synchronized static String formatAllUsers() {
//		StringBuilder result = new StringBuilder();
//		result.append(formatServiceMessage("List of connected clients"));
//		for (User user : users) {
//			result.append("\n--");
//			result.append(user.username);
//		}
//
//		result.append("\n----------------------------------------");
//		return result.toString();
//	}
//
//	synchronized static User findByUsername(String username) {
//		User result = null;
//		for (User u : users) {
//			if (u.username.equals(username)) {
//				result = u;
//				break;
//			}
//		}
//		return result;
//	}
}
