import java.io.IOException;
import java.net.Socket;
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
					default:
						break;
					}
				} else {
					System.err.println("Received a wierd response!");
				}
				
				ClientMain.printCommandLine();
			}
		} catch (IOException e) {
			System.out.println("CONNECTION LOST");
			this.close();
		}
	}

	void sendMessage(String message) {
		try {
			this.out.writeUTF(message);
			this.out.flush();
		} catch (IOException e) {
			System.out.println("CANT'T SEND MESSAGE");
			this.close();
		} catch (NullPointerException e2) {
			System.out.println("YOU ARE NOT CONNECTED TO THE SERVER");
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
