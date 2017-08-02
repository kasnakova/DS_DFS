import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.io.DataOutputStream;
import java.io.DataInputStream;

public class ClientThread extends Thread {
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;

	public ClientThread(String host, int port) {
		try {
			this.socket = new Socket(host, port);
			System.out.println("CONNECTED TO NAMING SERVER");
			this.in = new DataInputStream(this.socket.getInputStream());
			this.out = new DataOutputStream(this.socket.getOutputStream());
			this.start();
		} catch (IOException e) {
			System.err.println("CAN'T CONNECT TO NAMING SERVER");
			this.close();
		}
	}

	public void run() {
		try {
			while (true) {
				String message = this.in.readUTF();
				String[] split = message.split(Constants.DELIMITER);
				if (split.length == 2) {
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
						readFromStorageServer(data);
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
			System.err.println("CONNECTION LOST");
			this.close();
		}
	}

	void readFromStorageServer(String data) {
		String[] splitData = data.split(" ");
		String address = splitData[0];
		String filePath = splitData[1];
		String[] splitAddress = address.split(":");
		String ip = splitAddress[0];
		int port = Integer.parseInt(splitAddress[1]);
		try (Socket storageServer = new Socket(ip, port);
				DataInputStream ssIn = new DataInputStream(storageServer.getInputStream());
				DataOutputStream ssOut = new DataOutputStream(storageServer.getOutputStream())) {
			String message = Constants.TYPE_READ + Constants.DELIMITER + filePath;
			ssOut.writeUTF(message);
			ssOut.flush();
			String response = ssIn.readUTF();
			String[] splitResponse = response.split(Constants.DELIMITER);
			String result = splitResponse[0];
			String resData = splitResponse[1];
			if (result.equals(Constants.RES_SUCCESS)) {
				System.out.println(filePath + ":");
				System.out.println(resData);
			} else {
				System.out.println(
						"Sorry, something went wrong and you can't read the specified file right now.\n" + resData);
			}
		} catch (IOException e) {
			System.err.println("Sorry, could not read the specified file, try again later!");
			System.err.println(e.getMessage());
		}
	}

	void writeToStorageServer(String data) {
		String[] splitData = data.split(" ");
		String address = splitData[0];
		String filePath = splitData[1];
		String[] splitAddress = address.split(":");
		String ip = splitAddress[0];
		int port = Integer.parseInt(splitAddress[1]);
		try (Socket storageServer = new Socket(ip, port);
				DataInputStream ssIn = new DataInputStream(storageServer.getInputStream());
				DataOutputStream ssOut = new DataOutputStream(storageServer.getOutputStream())) {
			List<String> fileContents = Files.readAllLines(Paths.get(ClientMain.localFilePath));
			StringBuilder contents = new StringBuilder();
			for (String line : fileContents) {
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
				System.out.println(resData);
				String namingServerMessage = Constants.TYPE_SUCCESS_WRITE + Constants.DELIMITER + address
						+ Constants.DELIMITER + filePath;
				sendMessage(namingServerMessage);
			} else {
				System.out.println("Sorry, something went wrong and your new file was not saved to DFS.\n" + resData);
			}
		} catch (IOException e) {
			System.err.println("Sorry, could not read the specified file, try again later!");
			System.err.println(e.getMessage());
		}
	}

	void sendMessage(String message) {
		try {
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
