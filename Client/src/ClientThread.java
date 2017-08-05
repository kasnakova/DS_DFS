import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.DataInputStream;

public class ClientThread extends Thread {
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;

	public ClientThread(String host, int port) {
		try {
			ClientLogger.log("Starting CLientThread");
			this.socket = new Socket(host, port);
			System.out.println("\n\nCONNECTED TO NAMING SERVER");
			this.in = new DataInputStream(this.socket.getInputStream());
			this.out = new DataOutputStream(this.socket.getOutputStream());
			this.start();
		} catch (IOException e) {
			System.err.println("\n\n****************CAN'T CONNECT TO NAMING SERVER****************");
			this.close();
		}
	}

	public void run() {
		try {
			ClientLogger.log("CLientThread started");
			while (!ClientMain.shouldExit) {
				String message = this.in.readUTF();
				ClientLogger.log("Received message: " + message);
				String[] split = message.split(Constants.DELIMITER);
				if (split.length >= 2) {
					String type = split[0];
					String data = split[1];
					switch (type) {
					case Constants.TYPE_MSG:
						System.out.println(data);
						break;
					case Constants.TYPE_CURR_DIR:
						ClientMain.currentDir = data + ">";
						break;
					case Constants.TYPE_READ:
						readFromStorageServer(message, Constants.TYPE_READ);
						break;
					case Constants.TYPE_INFO:
						readFromStorageServer(message, Constants.TYPE_INFO);
						break;
					case Constants.TYPE_WRITE:
						writeToStorageServer(data);
						break;
					default:
						break;
					}
				} else {
					System.err.println("Received a wierd response!");
				}

				ClientMain.printCommandLine();
			}
		} catch (IOException e) {
			this.close();
		}
	}

	void readFromStorageServer(String data, String type) {
		String[] split = data.split(Constants.DELIMITER);
		StringBuilder content = new StringBuilder();
		boolean success = true;
		for (int i = 1; i < split.length; i++) {
			String[] splitData = split[i].split(" ");
			String address = splitData[0];
			String filePath = splitData[1];
			ClientLogger.log("Reading from " + address + " file " + filePath);
			String[] splitAddress = address.split(":");
			String ip = splitAddress[0];
			int port = Integer.parseInt(splitAddress[1]);
			try (Socket storageServer = new Socket(ip, port);
					DataInputStream ssIn = new DataInputStream(storageServer.getInputStream());
					DataOutputStream ssOut = new DataOutputStream(storageServer.getOutputStream())) {
				String message = type + Constants.DELIMITER + filePath;
				ssOut.writeUTF(message);
				ssOut.flush();
				String response = ssIn.readUTF();
				String[] splitResponse = response.split(Constants.DELIMITER);
				String result = splitResponse[0];
				String resData = splitResponse[1];
				if (result.equals(Constants.RES_SUCCESS)) {
					content.append(resData);
					content.append(System.getProperty("line.separator"));
					content.append(System.getProperty("line.separator"));
				} else {
					System.out.println(
							"Sorry, something went wrong and you can't read the specified file right now.\n" + resData);
					success = false;
					break;
				}
			} catch (IOException e) {
				ClientLogger.log(e);
				System.err.println("Sorry, could not read the specified file, try again later!");
				System.err.println(e.getMessage());
				success = false;
				break;
			}
		}

		if (success) {
			System.out.println("-----------------------");
			if(type.equals(Constants.TYPE_INFO) && split.length > 2){
				System.out.println("This file is chunked. Below is information on the separate chunks:");
			}
			
			System.out.println(content.toString());
			System.out.println("-----------------------");
		}
	}

	void writeToStorageServer(String data) {
		String[] splitData = data.split(" ");
		String address = splitData[0];
		String filePath = splitData[1];
		String[] splitAddress = address.split(":");
		ClientLogger.log("Writing to " + address + " file " + filePath);
		String ip = splitAddress[0];
		int port = Integer.parseInt(splitAddress[1]);
		String chunkKey = filePath.substring(filePath.lastIndexOf(Constants.DIR_SEPARATOR) + 1);
		try (Socket storageServer = new Socket(ip, port);
				DataInputStream ssIn = new DataInputStream(storageServer.getInputStream());
				DataOutputStream ssOut = new DataOutputStream(storageServer.getOutputStream())) {
			File file = ClientMain.getChunk(chunkKey);
			StringBuilder contents = new StringBuilder();
			boolean isChunk = true;
			if (file == null) {
				file = new File(ClientMain.localFilePath);
				isChunk = false;
			}

			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				String line;
				while ((line = br.readLine()) != null) {
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			}catch (IOException e) {
				ClientLogger.log(e);
			}

			String remoteFilePath = Utils.getPathName(filePath);
			String message = Constants.TYPE_WRITE + Constants.DELIMITER + remoteFilePath + Constants.DELIMITER
					+ contents.toString();
			ssOut.writeUTF(message);
			ssOut.flush();
			String response = ssIn.readUTF();
			String[] splitResponse = response.split(Constants.DELIMITER);
			String result = splitResponse[0];
			String resData = splitResponse[1];
			if (result.equals(Constants.RES_SUCCESS)) {
				System.out.println(resData);
				String namingServerMessage = Constants.TYPE_SUCCESS_WRITE + Constants.DELIMITER + address
						+ Constants.DELIMITER + filePath;
				sendMessage(namingServerMessage);
			} else {
				System.out.println("Sorry, something went wrong and your new file was not saved to DFS.\n" + resData);
			}

			if(isChunk){
				ClientMain.removeChunk(chunkKey);
			}
		} catch (IOException e) {
			ClientLogger.log(e);
			System.err.println("Sorry, could not read the specified file, try again later!");
			System.err.println(e.getMessage());
		}
	}

	void sendMessage(String message) {
		try {
			ClientLogger.log("Sending message: " + message);
			this.out.writeUTF(message);
			this.out.flush();
		} catch (IOException e) {
			System.err.println("CANT'T SEND MESSAGE");
			this.close();
		} catch (NullPointerException e2) {
			System.err.println("YOU ARE NOT CONNECTED TO THE SERVER");
		}
	}

	void close() {
		try {
			if (this.in != null)
				in.close();
			if (this.out != null)
				out.close();
			if (this.socket != null)
				socket.close();
		} catch (IOException e) {
		} finally {
			this.interrupt();
		}
	}
}
