import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ClientMain {
	static ClientThread clientThread;
	final static Scanner userInput = new Scanner(System.in);
	static String currentDir = Constants.ROOT_FOLDER_NAME.toUpperCase() + ">";
	static ConcurrentHashMap<String, File> chunksToBeWritten;
	static String localFilePath = null;
	static boolean shouldExit = false;

	public static void main(String[] args) {
		String namingServerHost = Constants.NAMING_SERVER_HOST;
		int namingServerPort = Constants.NAMING_SERVER_PORT;
		if (args.length > 1) {
			namingServerHost = args[0];
			namingServerPort = Integer.parseInt(args[1]);
		}

		clientThread = new ClientThread(namingServerHost, namingServerPort);

		String userChoice;
		String menu = getMenuString();
		System.out.println(menu);

		do {
			printCommandLine();
			userChoice = userInput.nextLine().toLowerCase();
			String[] userChoiceSplit = userChoice.split(" ");
			ClientLogger.log("Client input - " + userChoice);
			if (userChoiceSplit.length > 0) {
				String command = userChoiceSplit[0];
				String request = command;
				if (!command.equals(userChoice)) {
					String parameter = userChoice.substring(command.length() + 1);
					if (command.equals(Constants.CMD_REMOVE)) {
						String message = "Are you sure you want to remove directory '" + parameter
								+ "'. All files and directories in it will also be removed.";
						if (!confirmDirRemoval(message)) {
							continue;
						}
					} else if (command.equals(Constants.CMD_WRITE)) {
						if (isLocalFilePathValid(userChoiceSplit[2])) {
							parameter = userChoiceSplit[1];
							localFilePath = userChoiceSplit[2];
							chunk(localFilePath, parameter);
							continue;
						} else {
							continue;
						}

					} else {
						localFilePath = null;
					}

					request = command + Constants.DELIMITER + parameter;
				} else if (command.equals(Constants.CMD_INIT)) {
					String message = "Are you sure you want to initialize the DFS system? That will wipe out all exisitng files and directories!";
					if (!confirmDirRemoval(message)) {
						continue;
					}

					currentDir = Constants.ROOT_FOLDER_NAME + ">";
				} else if (command.equals(Constants.CMD_EXIT)) {
					shouldExit = true;
					break;
				}

				ClientMain.clientThread.sendMessage(request);
				ClientLogger.log("Send to naming server - " + request);
			} else {
				System.err.println("Please, enter a valid command!");
			}
		} while (!userChoice.equalsIgnoreCase(Constants.CMD_EXIT));

		userInput.close();
		clientThread.close();
	}

	public static File getChunk(String path) {
		return chunksToBeWritten.get(path);
	}

	public static void removeChunk(String path) {
		File file = chunksToBeWritten.remove(path);
		ClientLogger.log("Removing chunk - " + file.getAbsolutePath());
		if (file != null && path.contains(Constants.FORBIDDEN_SYMBOL)) {
			file.delete();
		}
	}

	private static void chunk(String localFilePath, String dfsPath) {
		File file = new File(localFilePath);
		long size = file.length();
		if (size > Constants.MAX_CHUNK_SIZE_BYTES) {
			try {
				chunksToBeWritten = splitFile(file, dfsPath);
			} catch (IOException e) {
				System.err.println("Sorry, file '" + localFilePath
						+ "' can't be written to the DFS right now. There is a poblem with its size.");
				chunksToBeWritten = null;
			}
		} else {
			chunksToBeWritten = new ConcurrentHashMap<String, File>();
			chunksToBeWritten.put(dfsPath, file);
		}

		for (String path : chunksToBeWritten.keySet()) {
			String request = Constants.CMD_WRITE + Constants.DELIMITER + path;
			ClientMain.clientThread.sendMessage(request);
			ClientLogger.log("Send to naming server - " + request);
		}
	}

	public static void printCommandLine() {
		System.out.println(ClientMain.currentDir.toUpperCase());
	}

	private static boolean isLocalFilePathValid(String localPath) {
		if (!localPath.contains(Constants.FILE_EXTENSION)) {
			System.out.println(
					"Only text files are allowed (i.e. with file extention " + Constants.FILE_EXTENSION + ")!");
			return false;
		}

		File file = new File(localPath);
		if (file.exists()) {
			return true;
		} else {
			System.out.println("'" + localPath + "' does not exist on this machine. Can't write to DFS.");
			return false;
		}
	}

	private static boolean confirmDirRemoval(String message) {
		System.out.println(message);
		System.out.println("yes/no");
		String answer = userInput.nextLine().toLowerCase();
		if (answer.equals("yes")) {
			return true;
		} else if (answer.equals("no")) {
			return false;
		} else {
			System.out.println("You should have answered with 'yes' or 'no'.");
			return false;
		}
	}

	private static ConcurrentHashMap<String, File> splitFile(File file, String dfsPath) throws IOException {
		int counter = 1;
		ConcurrentHashMap<String, File> files = new ConcurrentHashMap<String, File>();
		String eof = System.lineSeparator();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = br.readLine();
			while (line != null) {
				dfsPath = dfsPath.replace(Constants.FILE_EXTENSION, "");
				String newFileName = dfsPath + Constants.FORBIDDEN_SYMBOL + String.format("%03d", counter++)
						+ Constants.FILE_EXTENSION;
				File newFile = new File(file.getParent(), newFileName);
				try (OutputStream out = new BufferedOutputStream(new FileOutputStream(newFile))) {
					int fileSize = 0;
					while (line != null) {
						byte[] bytes = (line + eof).getBytes(Charset.defaultCharset());
						if (fileSize + bytes.length > Constants.MAX_CHUNK_SIZE_BYTES)
							break;
						out.write(bytes);
						fileSize += bytes.length;
						line = br.readLine();
					}
				}
				files.put(newFileName, newFile);
			}
		}
		return files;
	}

	private static String getMenuString() {
		StringBuilder menu = new StringBuilder();
		menu.append("\n\n-------Please choose what you would like to do-------\n");
		menu.append(Constants.CMD_INIT
				+ " - initialize the client storage on a new system (that will delete all previous files and folders and return the available size)\n");
		menu.append(Constants.CMD_READ
				+ " <file_name> - read a file from the DFS (you should specify the name of the file in the place of <file_name>)\n");
		menu.append(Constants.CMD_WRITE
				+ " <file_name> <local_file_path>- create a new file with a specified name and write the content from a local file to it\n");
		menu.append(Constants.CMD_DELETE + " <file_name> - delete a specified file from the DFS\n");
		menu.append(Constants.CMD_INFO
				+ " <file_name> - provides information about the file (any useful information - size, node id, etc.)\n");
		menu.append(Constants.CMD_OPEN + " <directory_name> - opens a directory with the provided name\n");
		menu.append(Constants.CMD_LIST + " - returns list of files, which are stored in the current directory\n");
		menu.append(Constants.CMD_MAKE + " <directory_name> - creates a directory with the specified name\n");
		menu.append(Constants.CMD_REMOVE + " <directory_name> - removes the specified directory\n");
		menu.append(Constants.CMD_EXIT + " - to exit the program\n");
		return menu.toString();
	}
}
