import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class User extends Thread {
	String username;
	Socket mySocket;
	DataInputStream in;
	DataOutputStream out;

	public User(Socket s) throws IOException {
		this.mySocket = s;
		this.in = new DataInputStream(s.getInputStream());
		this.out = new DataOutputStream(s.getOutputStream());
	}

	public void run() {
		try {
			out.writeUTF("SERVER: Please reply with your username");
			this.username = in.readUTF();
			while (!NamingServerMain.usernameIsFree(this.username)) {
				out.writeUTF("SERVER: Username is taken. Enter a new name");
				this.username = in.readUTF();
			}

			NamingServerMain.users.add(this);
			out.writeUTF("SERVER: Welcome to the chat and please watch your language (I am watching you...)");
			NamingServerMain.sendToAll(NamingServerMain.formatServiceMessage(username + " joined the chat"));
			this.startUserChat();

		} catch (IOException e) {
			System.err.println("ERR: User left. Reason: " + e.getMessage());
		} finally {
			NamingServerMain.removeUser(this);
			closeConnection();
		}
	}

	private void startUserChat() throws IOException {
		String message;
		do {
			message = this.in.readUTF();
			if (message.equalsIgnoreCase("#exit")) {
				NamingServerMain.sendToAll(NamingServerMain.formatServiceMessage(username + " left the chat"));
			} else if (message.equalsIgnoreCase("#list")) {
				NamingServerMain
						.sendToAll(NamingServerMain.formatServiceMessage(username + " requested a list of the chat clients"));
				NamingServerMain.sendToAll(NamingServerMain.formatAllUsers());
			} else if (NamingServerMain.isMessageBad(message)) {
				if (NamingServerMain.removeUser(this)) {
					send(NamingServerMain.formatServiceMessage("You are being removed for bad language!"));
					closeConnection();
					NamingServerMain.sendToAll(NamingServerMain.formatServiceMessage(username + " was removed for bad language!"));
				}
			} else if (message.contains("make")) {
				String dirName = Constants.ROOT_FOLDER_NAME + "/" + message.split(Constants.DELIMITER)[1];
				String response = "";
				File file = new File(dirName);
		        if (!file.exists()) {
		            if (file.mkdir()) {
		            	response = "Directory '" + dirName + "' was successfully created";
		            } else {
		            	response = "Failed to create directory '" + dirName + "'";
		            }
		        } else {
		        	response = "Directoy '" + dirName + "' already exists.";
		        }
		        
		        NamingServerMain.sendToAll(response);
			} else {
				NamingServerMain.sendToAll(this.username.toUpperCase() + ": " + message);
			}
		} while (!message.equalsIgnoreCase("#exit"));
	}

	void send(String message) throws IOException {
		out.writeUTF(message);
	}

	public void closeConnection() {
		try {
			if (this.in != null)
				in.close();
			if (this.out != null)
				out.close();
			if (this.mySocket != null)
				mySocket.close();
		} catch (IOException e) {
			System.err.println("ERR: Can't close user socket. Reason: " + e.getMessage());
		}
	}
}