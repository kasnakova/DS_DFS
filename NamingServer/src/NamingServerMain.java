import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class NamingServerMain {
	static ArrayList<User> users = new ArrayList<User>(10);
	static int port = 1111;
	static List<String> badWords = new ArrayList<String>();

	public static void main(String[] args) {
		// Starting server
		ServerSocket servSock;
		try {
			servSock = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Can't start server");
			return;
		}
		System.out.println("Server started");
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
				User u = new User(newConnection);
				u.start();
			} catch (IOException e) {
				System.err.println("ERR: Error establishing connection. Reason: " + e.getMessage());
			}
		}
	}

	static void fillBadWords() {
		badWords.add("fuck");
		badWords.add("bastard");
		badWords.add("bitch");
		badWords.add("whore");
		badWords.add("shit");
		badWords.add("asshole");
		badWords.add("slut");
		badWords.add("crap");
		badWords.add("dick");
		badWords.add("pussy");
		badWords.add("ass");
		badWords.add("motherfucker");
		badWords.add("nigga");
	}

	synchronized static boolean removeUser(User u) {
		for(User user : users){
			if (user.equals(u)) {
				users.remove(user);
				System.out.println("User " + user.username + " removed");
				return true;
			}
		}

		return false;
	}

	synchronized static boolean usernameIsFree(String username) {
		boolean result = true;
		for (User u : users) {
			if (u.username.equals(username)) {
				result = false;
				break;
			}
		}
		return result;
	}

	synchronized static void sendToAll(String message) throws IOException {
		for (User u : users) {
			u.send(message);
		}
	}

	synchronized static boolean isMessageBad(String message) {
		message = message.toLowerCase();
		for (String word : badWords) {
			if (message.contains(word)) {
				return true;
			}
		}

		return false;
	}

	synchronized static String formatServiceMessage(String message) {
		String result = "-------" + message.toUpperCase() + "-------";
		return result;
	}

	synchronized static String formatAllUsers() {
		StringBuilder result = new StringBuilder();
		result.append(formatServiceMessage("List of connected clients"));
		for (User user : users) {
			result.append("\n--");
			result.append(user.username);
		}

		result.append("\n----------------------------------------");
		return result.toString();
	}

	synchronized static User findByUsername(String username) {
		User result = null;
		for (User u : users) {
			if (u.username.equals(username)) {
				result = u;
				break;
			}
		}
		return result;
	}
}
