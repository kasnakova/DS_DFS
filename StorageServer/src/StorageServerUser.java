import java.io.IOException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

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
			System.out.println("User left. Reason: " + e.getMessage());
		} finally {
			closeConnection();
		}
	}

	private void startUserThread() throws IOException {
		String message;
		do {
			message = this.in.readUTF().toLowerCase();
			if (message.startsWith(Constants.TYPE_INFO)) {
				onInfoCommand(message);
			} else if (message.startsWith(Constants.TYPE_READ)) {
				onReadCommand(message);
			} else if (message.startsWith(Constants.TYPE_WRITE)) {
				onWriteCommand(message);
			} 
		} while (true);
	}

	private void onInfoCommand(String message) {
		String filePath = message.split(Constants.DELIMITER)[1];
		String localFilePath = StorageServerMain.port + "/" + filePath;
		StringBuilder response =  new StringBuilder();
			File file = new File(localFilePath);
			if (file.exists()) {
				response.append(Constants.RES_SUCCESS);
				response.append(Constants.DELIMITER);
				response.append("Path: " + filePath);
				response.append(System.getProperty("line.separator"));
				response.append("Size: " + file.length() + " bytes");
				response.append(System.getProperty("line.separator"));
				String lastModified = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
						.format(new Date(file.lastModified()));
				response.append("Last modified: " + lastModified);
				response.append(System.getProperty("line.separator"));
				response.append("Node ID: " + StorageServerMain.port);
			} else {
				response.append(Constants.RES_ERROR);
				response.append(Constants.DELIMITER);
				response.append("File '" + filePath + "' does not exist in this storage server.");
			}

		send(response.toString());
	}
	
	private void onReadCommand(String message) {
		String filePath = message.split(Constants.DELIMITER)[1];
		String localFilePath = StorageServerMain.port + "/" + filePath;
		StringBuilder response =  new StringBuilder();
			File file = new File(localFilePath);
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
		String filePath = data[1];
		String localFilePath = StorageServerMain.port + "/" + filePath;
		String contents = data[2];
		StringBuilder response =  new StringBuilder();
			File file = new File(localFilePath);
			if (!file.exists()) {
				try {
					file.getParentFile().mkdirs();
					if (file.createNewFile()) {
						Files.write(Paths.get(localFilePath), contents.getBytes());
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