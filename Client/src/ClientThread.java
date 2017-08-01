import java.io.IOException;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;

public class ClientThread extends Thread {
	public boolean running = false;
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;

	public ClientThread(String host, int port) {
		try {
			this.socket = new Socket(host, port);
			System.out.println("CONNECTED");
			this.in = new DataInputStream(this.socket.getInputStream());
			this.out = new DataOutputStream(this.socket.getOutputStream());
			this.start();
			running = true;
		} catch (IOException e) {
			System.out.println("CAN'T CONNECT TO SERVER");
			this.close();
		}
	}

	public void run() {
		try {
			while (true) {
				System.out.println(this.in.readUTF());
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
			running = false;
			this.interrupt();
		}
	}
}
