import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class NamingServerThread extends Thread {
	private Socket namingServerSocket;
	private DataInputStream namingServerIn;
	private DataOutputStream namingServerOut;

	public NamingServerThread(int storageServerPort) throws IOException {
		namingServerSocket = new Socket(Constants.NAMING_SERVER_HOST, Constants.NAMING_SERVER_PORT);
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
				if (split.length == 2) {
					String type = split[0];
					String data = split[1];
					switch (type) {
					case Constants.TYPE_DELETE:
						onDeleteCommand(message);
						break;
					case Constants.TYPE_HEARTBEAT:
						send(Constants.RES_HEARTBEAT);
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
