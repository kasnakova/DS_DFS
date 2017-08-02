import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.net.ServerSocket;

public class StorageServerMain {
	static int port;
	static NamingServerThread clientThread;

	public static void main(String[] args) {
		ServerSocket servSock;
		String ip = "unknown";
		try {
			if (args.length > 0) {
				port = Integer.parseInt(args[0]);
			} else {
				System.err.println(
						"Please, specify the port number on which to run this storage server as program argument!");
				return;
			}

			servSock = new ServerSocket(port);
			ip = servSock.getInetAddress().getHostAddress() + ":" + servSock.getLocalPort();
			clientThread = new NamingServerThread(port);
			System.out.println("Storage server started at IP: " + ip);
		} catch (IOException e) {
			System.err.println("Can't start storage and register server.\n" + e.getMessage());
			return;
		}

		if (createRootDirectory(port)) {
			while (true) {
				try {
					Socket newConnection = servSock.accept();
					StorageServerUser user = new StorageServerUser(newConnection);
					user.start();
				} catch (IOException e) {
					System.err.println("Error establishing connection. Reason: " + e.getMessage());
				}
			}
		} else {
			System.err.println("Storage server shutting down because it could not create its root directory!");
		}
	}

	private static boolean createRootDirectory(int port) {
		File portDir = new File(String.valueOf(port));
		if (!portDir.exists()) {
			if (portDir.mkdir()) {
				System.out.println("Port directory was created!");
				File root = new File(port + "/" + Constants.ROOT_FOLDER_NAME);
				if (!root.exists()) {
					if (root.mkdir()) {
						System.out.println("Root directory was created!");
						return true;
					} else {
						System.err.println("Failed to create root directory!");
					}
				}
			} else {
				System.err.println("Failed to create port directory!");
			}
		} else {
			return true;
		}

		return false;
	}
}
