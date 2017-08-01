import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;

public class User extends Thread {
	String currentDir;
	Socket mySocket;
	DataInputStream in;
	DataOutputStream out;

	public User(Socket s) throws IOException {
		this.mySocket = s;
		this.in = new DataInputStream(s.getInputStream());
		this.out = new DataOutputStream(s.getOutputStream());
		this.currentDir = Constants.ROOT_FOLDER_NAME;
	}

	public void run() {
		try {
			this.startUserChat();
		} catch (IOException e) {
			System.err.println("User left. Reason: " + e.getMessage());
		} finally {
			closeConnection();
		}
	}

	private void startUserChat() throws IOException {
		String message;
		do {
			message = this.in.readUTF().toLowerCase();
			if (message.equals(Constants.CMD_INIT)) {

			} else if (message.startsWith(Constants.CMD_READ)) {

			} else if (message.startsWith(Constants.CMD_WRITE)) {

			} else if (message.startsWith(Constants.CMD_DELETE)) {

			} else if (message.startsWith(Constants.CMD_INFO)) {

			} else if (message.startsWith(Constants.CMD_OPEN)) {
				onOpenCommand(message);
			} else if (message.equals(Constants.CMD_LIST)) {
				onListCommand(message);
			} else if (message.startsWith(Constants.CMD_MAKE)) {
				onMakeCommand(message);
			} else if (message.startsWith(Constants.CMD_REMOVE)) {
				onRemoveCommand(message);
			} else if (message.equals(Constants.CMD_EXIT)) {
				break;
			} else {
				send("Please, enter a valid command!");
			}
		} while (!message.equalsIgnoreCase(Constants.CMD_EXIT));
	}

	private void onOpenCommand(String message) throws IOException {
		String dirName = message.split(Constants.DELIMITER)[1];
		String response;
		if (dirName.equals("..")) {
			int index = this.currentDir.lastIndexOf("/");
			if (index == -1) {
				this.currentDir = Constants.ROOT_FOLDER_NAME;
			} else {
				this.currentDir = this.currentDir.substring(0, index);
			}

			response = Constants.TYPE_CURR_DIR + Constants.DELIMITER + this.currentDir;
		} else {
			String path = this.currentDir + "/" + dirName;
			File dir = new File(path);
			if (dir.exists()) {
				response = Constants.TYPE_CURR_DIR + Constants.DELIMITER + path;
				this.currentDir = path;
			} else {
				response = Constants.TYPE_MSG + Constants.DELIMITER + "Directoy '" + dirName
						+ "' does not exist in the current directory.";
			}
		}

		send(response);
	}

	private void onListCommand(String message) throws IOException {
		File dir = new File(this.currentDir);
		File[] files = dir.listFiles();
		StringBuilder response = new StringBuilder();
		response.append(Constants.TYPE_MSG);
		response.append(Constants.DELIMITER);
		if (files.length > 0) {
			for (File file : files) {
				response.append(file.getName());
				response.append(System.getProperty("line.separator"));
			}
		} else {
			response.append("Nothing to list here!");
		}

		send(response.toString());
	}

	private void onMakeCommand(String message) throws IOException {
		String dirName = message.split(Constants.DELIMITER)[1];
		String path = this.currentDir + "/" + dirName;
		String response = Constants.TYPE_MSG + Constants.DELIMITER;
		File file = new File(path);
		if (!file.exists()) {
			if (file.mkdir()) {
				response += "Directory '" + dirName + "' was successfully created";
			} else {
				response += "Failed to create directory '" + dirName + "'";
			}
		} else {
			response += "Directoy '" + dirName + "' already exists.";
		}

		send(response);
	}

	private void onRemoveCommand(String message) throws IOException {
		String fileName = message.split(Constants.DELIMITER)[1];
		String response = Constants.TYPE_MSG + Constants.DELIMITER;

		if (fileName.equals(Constants.ROOT_FOLDER_NAME)) {
			response += "You can't remove the root folder! Don't be evil!";
		} else {
			String path = this.currentDir + "/" + fileName;
			File file = new File(path);
			if (file.exists()) {
				if (file.isDirectory()) {
					//TODO: delete from storage servers 
					//TODO: delete all files in the directory
					response += "Directory '" + fileName + "' was successfully removed along with all of its contents";
					//TODO: delete from storage servers
					//TODO: this should be in the delete file command
					if (file.delete()) {
						response += "'" + fileName + "' was successfully removed";
					} else {
						response += "Something went wrong and '" + fileName + "' could not be removed";
					}
				} else {
					response += "'" + fileName + "' was not removed since it is not a directory";
				}
			} else {
				response += "Can't remove '" + fileName + "' it already does not exist in the current directory.";
			}
		}

		send(response);
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