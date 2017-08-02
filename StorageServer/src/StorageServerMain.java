import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.net.ServerSocket;

public class StorageServerMain {
	private static Socket namingServerSocket;
	private static DataInputStream namingServerIn;
	private static DataOutputStream namingServerOut;
	private static int port;

	public static void main(String[] args) {
		// TODO: make the naming server ip command line args
		ServerSocket servSock;
		String ip = "unknown"; 
		try {
			port = new Random().nextInt(1000) + 1000;
			servSock = new ServerSocket(port);
			ip = servSock.getInetAddress().getHostAddress() + ":" + servSock.getLocalPort();
			System.out.println("Storage server started at IP: " + ip);
		} catch (IOException e) {
			System.err.println("Can't start storage server");
			return;
		}
		
		if(register() && createRootDirectory(port)){
		while (true) {
			try {
				Socket newConnection = servSock.accept();
				User user = new User(newConnection);
				user.start();
			} catch (IOException e) {
				System.err.println("Error establishing connection. Reason: " + e.getMessage());
			}
		}} else {
			System.err.println("Storage server shutting down because it could not register with the Naming server!");
		}
	}
	
	private static boolean createRootDirectory(int port){
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
        }
		
		return false;
	}
	
	private static boolean register(){
		try {
			namingServerSocket = new Socket(Constants.NAMING_SERVER_HOST, Constants.NAMING_SERVER_PORT);
			System.out.println("CONNECTED TO NAMING SERVER");
			namingServerIn = new DataInputStream(namingServerSocket.getInputStream());
			namingServerOut = new DataOutputStream(namingServerSocket.getOutputStream());
			namingServerOut.writeUTF(Constants.CMD_REGISTER + Constants.DELIMITER + port);
			namingServerOut.flush();
			String response = namingServerIn.readUTF();
			if(response.equals(Constants.RES_SUCCESS)){
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}
}
