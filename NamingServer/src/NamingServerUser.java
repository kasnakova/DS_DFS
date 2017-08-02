import java.io.IOException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class NamingServerUser extends Thread {
	String currentDir;
	Socket mySocket;
	DataInputStream in;
	DataOutputStream out;

	public NamingServerUser(Socket s) throws IOException {
		this.mySocket = s;
		this.in = new DataInputStream(s.getInputStream());
		this.out = new DataOutputStream(s.getOutputStream());
		this.currentDir = Constants.ROOT_FOLDER_NAME;
	}

	public void run() {
		try {
			this.startUserThread();
		} catch (IOException e) {
			System.err.println("User left. Reason: " + e.getMessage());
		} finally {
			closeConnection();
		}
	}

	private void startUserThread() throws IOException {
		String message;
		do {
			message = this.in.readUTF().toLowerCase();
			if (message.equals(Constants.CMD_INIT)) {

			} else if (message.startsWith(Constants.CMD_READ)) {
				onReadCommand(message);
			} else if (message.startsWith(Constants.CMD_WRITE)) {
				onWriteCommand(message);
			} else if (message.startsWith(Constants.CMD_DELETE)) {
				onDeleteCommand(message);
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
			} else if (message.startsWith(Constants.CMD_REGISTER)) {
				registerStorageServer(message);
			} else if (message.startsWith(Constants.TYPE_SUCCESS_WRITE)) {
				successInWritingToStorageServer(message);
			} else {
				send(Constants.TYPE_MSG + Constants.DELIMITER + "Please, enter a valid command!");
			}
		} while (!message.equalsIgnoreCase(Constants.CMD_EXIT));
	}

	private void onReadCommand(String message) throws IOException {
		String fileName = message.split(Constants.DELIMITER)[1];
		String response = Constants.TYPE_MSG + Constants.DELIMITER;
		String path = this.currentDir + "/" + fileName;
		File file = new File(path);
		if (file.exists()) {
			if (file.isDirectory()) {
				response += "'" + fileName + "' is a directory. You can open it but not read it like a file.";
			} else {
				List<String> content = Files.readAllLines(Paths.get(path));
				String address = content.get(0).trim();
				response = Constants.TYPE_READ + Constants.DELIMITER + address + " " + path;
			}
		} else {
			response += "Can't read file '" + fileName + "' because it does not exist in the current directory.";
		}

		send(response);
	}

	private void onWriteCommand(String message) throws IOException {
		String fileName = message.split(Constants.DELIMITER)[1];
		String response = Constants.TYPE_MSG + Constants.DELIMITER;
		String path = this.currentDir + "/" + fileName;
		File file = new File(path);
		if (!file.exists()) {
			String address = NamingServerMain.getAvailableStorageServerForWriting();
			if (address != null) {
				response = Constants.TYPE_WRITE + Constants.DELIMITER + address + " " + path;
			} else {
				response += "There are no available storage servers to which you can write your file. Sorry.";
			}
		} else {
			response += "File with name '" + fileName + "' already exists in the current directory.";
		}

		send(response);
	}

	private void onDeleteCommand(String message) {
		String fileName = message.split(Constants.DELIMITER)[1];
		String response = Constants.TYPE_MSG + Constants.DELIMITER;
		String path = this.currentDir + "/" + fileName;
		File file = new File(path);
		if (file.exists()) {
			if (!file.isDirectory()) {
				try {
					List<String> content = Files.readAllLines(Paths.get(path));
					String address = content.get(0).trim();
					NamingServerUser storage = NamingServerMain.getStorageServerByAddress(address);
					if (storage != null) {
						storage.out.writeUTF(Constants.TYPE_DELETE + Constants.DELIMITER + path);
						String storageResponse = storage.in.readUTF();
						String[] splitStorageResponse = storageResponse.split(Constants.DELIMITER);
						String result = splitStorageResponse[0];
						if (result.equals(Constants.RES_SUCCESS)) {
							if (file.delete()) {
								response += "'" + fileName + "' was successfully deleted";
							} else {
								response += "Something went wrong and '" + fileName + "' could not be deleted from the index.";
							}
						} else {
							String error = splitStorageResponse[1];
							response += "'" + fileName + "' not successfully deleted.\n" + error;
						}
					}
				} catch (IOException e) {
					response += "Something went wrong and '" + fileName + "' could not be deleted!\n" + e.getMessage();
				}
			} else {
				response += "'" + fileName + "' was not deleted since it is a directory";
			}
		} else {
			response += "Can't remove '" + fileName + "' it already does not exist in the current directory.";
		}

		send(response);
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
					// TODO: delete from storage servers
					// TODO: delete all files in the directory
					response += "Directory '" + fileName + "' was successfully removed along with all of its contents";
					// TODO: delete from storage servers
					// TODO: this should be in the delete file command
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

	private void registerStorageServer(String message) throws IOException {
		String port = message.split(Constants.DELIMITER)[1];
		String address = this.mySocket.getInetAddress().getHostAddress() + ":" + port;
		System.out.println("Added storage server: " + address);
		NamingServerMain.addStorageServer(address, this);
		send(Constants.RES_SUCCESS);
	}

	private void successInWritingToStorageServer(String message) throws IOException {
		String[] data = message.split(Constants.DELIMITER);
		String address = data[1];
		String path = data[2];
		String response = Constants.TYPE_MSG + Constants.DELIMITER;
		File file = new File(path);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			if (file.createNewFile()) {
				Files.write(Paths.get(path), address.getBytes());
				response += "File '" + path + "' was successfully indexed.";
			} else {
				response += "Failed to index file '" + path + "'";
			}
		} else {
			response += "File '" + path + "' already exists.";
		}

		send(response);
	}

	void send(String message) {
		try {
			this.out.writeUTF(message);
			this.out.flush();
		} catch (IOException e) {
			System.err.println("CANT'T SEND MESSAGE");
			this.closeConnection();
		}
	}

	public void closeConnection() {
		try {
			NamingServerMain.removeStorageServer(this);
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