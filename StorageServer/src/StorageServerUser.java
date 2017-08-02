import java.io.IOException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StorageServerUser extends Thread {
	Socket mySocket;
	DataInputStream in;
	DataOutputStream out;

	public StorageServerUser(Socket s) throws IOException {
		this.mySocket = s;
		this.in = new DataInputStream(s.getInputStream());
		this.out = new DataOutputStream(s.getOutputStream());
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

			} else if (message.startsWith(Constants.TYPE_READ)) {
				onReadCommand(message);
			} else if (message.startsWith(Constants.TYPE_WRITE)) {
				onWriteCommand(message);
			} else if (message.startsWith(Constants.CMD_INFO)) {

			} else if (message.startsWith(Constants.CMD_OPEN)) {
				
			} else if (message.equals(Constants.CMD_LIST)) {
				//onListCommand(message);
			} else if (message.startsWith(Constants.CMD_MAKE)) {
				//onMakeCommand(message);
			} else if (message.startsWith(Constants.CMD_REMOVE)) {
				//onRemoveCommand(message);
			} else if (message.equals(Constants.CMD_EXIT)) {
				break;
			} else {
				send("Please, enter a valid command!");
			}
		} while (!message.equalsIgnoreCase(Constants.CMD_EXIT));
	}

	private void onReadCommand(String message) {
		String filePath = StorageServerMain.port + "/" + message.split(Constants.DELIMITER)[1];
		StringBuilder response =  new StringBuilder();
			File file = new File(filePath);
			if (file.exists()) {
				try(BufferedReader br = new BufferedReader(new FileReader(file))){
					response.append(Constants.RES_SUCCESS);
					response.append(Constants.DELIMITER);
					String line = "";
					while((line = br.readLine()) != null){
						response.append(line);
						response.append(System.getProperty("line.separator"));
					}
				} catch(IOException e){
					response.append(Constants.RES_ERROR);
					response.append(Constants.DELIMITER);
					response.append(e.getMessage());
				}
			} else {
				response.append(Constants.RES_ERROR);
				response.append(Constants.DELIMITER);
				response.append("File '" + filePath + "' does not exist in this storage server.");
			}

		send(response.toString());
	}
	
	private void onWriteCommand(String message) {
		String[] data = message.split(Constants.DELIMITER);
		String filePath = StorageServerMain.port + "/" + data[1];
		String contents = data[2];
		StringBuilder response =  new StringBuilder();
			File file = new File(filePath);
			if (!file.exists()) {
				try {
					file.getParentFile().mkdirs();
					if (file.createNewFile()) {
						Files.write(Paths.get(filePath), contents.getBytes());
						response.append(Constants.RES_SUCCESS);
						response.append(Constants.DELIMITER);
						response.append("File '" + filePath + "' successfully created.");
					} else {
						response.append(Constants.RES_ERROR);
						response.append(Constants.DELIMITER);
						response.append("Failed to create file '" + filePath + "'");
					}
				} catch(IOException e){
					response.append(Constants.RES_ERROR);
					response.append(Constants.DELIMITER);
					response.append(e.getMessage());
				}
			} else {
				response.append(Constants.RES_ERROR);
				response.append(Constants.DELIMITER);
				response.append("File '" + filePath + "' already exists in this storage server.");
			}

		send(response.toString());
	}
	

//
//	private void onListCommand(String message) throws IOException {
//		File dir = new File(this.currentDir);
//		File[] files = dir.listFiles();
//		StringBuilder response = new StringBuilder();
//		response.append(Constants.TYPE_MSG);
//		response.append(Constants.DELIMITER);
//		if (files.length > 0) {
//			for (File file : files) {
//				response.append(file.getName());
//				response.append(System.getProperty("line.separator"));
//			}
//		} else {
//			response.append("Nothing to list here!");
//		}
//
//		send(response.toString());
//	}
//
//	private void onMakeCommand(String message) throws IOException {
//		String dirName = message.split(Constants.DELIMITER)[1];
//		String path = this.currentDir + "/" + dirName;
//		String response = Constants.TYPE_MSG + Constants.DELIMITER;
//		File file = new File(path);
//		if (!file.exists()) {
//			if (file.mkdir()) {
//				response += "Directory '" + dirName + "' was successfully created";
//			} else {
//				response += "Failed to create directory '" + dirName + "'";
//			}
//		} else {
//			response += "Directoy '" + dirName + "' already exists.";
//		}
//
//		send(response);
//	}
//
//	private void onRemoveCommand(String message) throws IOException {
//		String fileName = message.split(Constants.DELIMITER)[1];
//		String response = Constants.TYPE_MSG + Constants.DELIMITER;
//
//		if (fileName.equals(Constants.ROOT_FOLDER_NAME)) {
//			response += "You can't remove the root folder! Don't be evil!";
//		} else {
//			String path = this.currentDir + "/" + fileName;
//			File file = new File(path);
//			if (file.exists()) {
//				if (file.isDirectory()) {
//					//TODO: delete from storage servers 
//					//TODO: delete all files in the directory
//					response += "Directory '" + fileName + "' was successfully removed along with all of its contents";
//					//TODO: delete from storage servers
//					//TODO: this should be in the delete file command
//					if (file.delete()) {
//						response += "'" + fileName + "' was successfully removed";
//					} else {
//						response += "Something went wrong and '" + fileName + "' could not be removed";
//					}
//				} else {
//					response += "'" + fileName + "' was not removed since it is not a directory";
//				}
//			} else {
//				response += "Can't remove '" + fileName + "' it already does not exist in the current directory.";
//			}
//		}
//
//		send(response);
//	}

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