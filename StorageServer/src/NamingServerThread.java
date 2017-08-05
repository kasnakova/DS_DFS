import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class NamingServerThread extends Thread {
	private Socket namingServerSocket;
	private DataInputStream namingServerIn;
	private DataOutputStream namingServerOut;

	public NamingServerThread(int storageServerPort, String namingServerHost, int namingServerPort) throws IOException {
		namingServerSocket = new Socket(namingServerHost, namingServerPort);
		namingServerIn = new DataInputStream(namingServerSocket.getInputStream());
		namingServerOut = new DataOutputStream(namingServerSocket.getOutputStream());
		namingServerOut.writeUTF(Constants.CMD_REGISTER + Constants.DELIMITER + storageServerPort);
		namingServerOut.flush();
		String response = namingServerIn.readUTF();
		if (!response.equals(Constants.RES_SUCCESS)) {
			throw new IOException("Could not register with the Naming server");
		}

		System.out.println("CONNECTED TO NAMING SERVER");
		this.start();
	}

	public void run() {
		try {
			while (true) {
				String message = this.namingServerIn.readUTF();
				String[] split = message.split(Constants.DELIMITER);
				if (split.length >= 1) {
					String type = split[0];
					switch (type) {
					case Constants.TYPE_DELETE:
						onDeleteCommand(message);
						break;
					case Constants.TYPE_SIZE:
						onSize();
						break;
					case Constants.TYPE_REPLICA:
						onReplica(split[1], split[2]);
						break;
					default:
						break;
					}
				}
			}
		} catch (IOException e) {
			System.err.println("CONNECTION LOST");
			this.close();
		}
	}
	
	private void onSize(){
		String filePath = StorageServerMain.port + "/" + Constants.ROOT_FOLDER_NAME;
		File file = new File(filePath);
		System.out.println("MEMORY: " + file.getUsableSpace());
		send(String.valueOf(file.getUsableSpace()));
	}
	
	private void onReplica(String filePath, String address){
		String localFilePath = StorageServerMain.port + "/" + filePath;
		String[] splitAddress = address.split(":");
		String ip = splitAddress[0];
		int port = Integer.parseInt(splitAddress[1]);
		try (Socket storageServer = new Socket(ip, port);
				DataInputStream ssIn = new DataInputStream(storageServer.getInputStream());
				DataOutputStream ssOut = new DataOutputStream(storageServer.getOutputStream());
				BufferedReader reader = new BufferedReader(new FileReader(localFilePath))) {
			StringBuilder contents = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}

			String message = Constants.TYPE_WRITE + Constants.DELIMITER + filePath + Constants.DELIMITER
					+ contents.toString();
			ssOut.writeUTF(message);
			ssOut.flush();
			String response = ssIn.readUTF();
			String[] splitResponse = response.split(Constants.DELIMITER);
			String result = splitResponse[0];
			String resData = splitResponse[1];
			if (result.equals(Constants.RES_SUCCESS)) {
				System.out.println("Replica on " + address + ": " + resData);
				String namingServerMessage = Constants.TYPE_SUCCESS_REPLICA_WRITE + Constants.DELIMITER + address
						+ Constants.DELIMITER + filePath;
				send(namingServerMessage);
			} else {
				onReplicaCreationFail();
				System.out.println("Sorry, something went wrong and your replica was not created.\n" + resData);
			}
		} catch (IOException e) {
			onReplicaCreationFail();
			System.err.println("Sorry, replica not created because the specified file could not be read, try again later!\n" + e.getMessage());
			System.err.println(e.getMessage());
		}
	}
	
	private void onReplicaCreationFail(){
		send(Constants.RES_ERROR);
	}
	
	private void onDeleteCommand(String message) {
		String filePath = StorageServerMain.port + "/" + message.split(Constants.DELIMITER)[1];
		StringBuilder response = new StringBuilder();
		File file = new File(filePath);
		File directory = file.getParentFile();
		if (file.exists()) {
			if (file.delete()) {
				response.append(Constants.RES_SUCCESS);
				response.append(Constants.DELIMITER);
				deleteEmptyDirectories(directory);
			} else {
				response.append(Constants.RES_ERROR);
				response.append(Constants.DELIMITER);
				response.append("File '" + filePath + "' could not be deleted from this storage server.");
			}
		} else {
			response.append(Constants.RES_ERROR);
			response.append(Constants.DELIMITER);
			response.append("File '" + filePath + "' does not exist in this storage server.");
		}

		send(response.toString());
	}
	
	private void deleteEmptyDirectories(File directory){
		String name = directory.getName();
		if(name.equals(Constants.ROOT_FOLDER_NAME)){
			return;
		}
		
		File parent = directory.getParentFile();
		File[] children = directory.listFiles();
		if(children.length <= 0){
			directory.delete();
			deleteEmptyDirectories(parent);
		}
	}

	void send(String message) {
		try {
			this.namingServerOut.writeUTF(message);
			this.namingServerOut.flush();
		} catch (IOException e) {
			System.err.println("CANT'T SEND MESSAGE");
			this.close();
		}
	}

	void close() {
		try {
			if (this.namingServerIn != null)
				namingServerIn.close();
			if (this.namingServerOut != null)
				namingServerOut.close();
			if (this.namingServerSocket != null)
				namingServerSocket.close();
		} catch (IOException e) {
		} finally {
			this.interrupt();
		}
	}
}
