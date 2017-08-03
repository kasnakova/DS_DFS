import java.io.File;
import java.util.List;
import java.util.Scanner;

public class ClientMain {
	final static ClientThread clientThread = new ClientThread(Constants.NAMING_SERVER_HOST,
			Constants.NAMING_SERVER_PORT);
	final static Scanner userInput = new Scanner(System.in);
	static String currentDir = Constants.ROOT_FOLDER_NAME.toUpperCase() + ">";
	static String localFilePath = null;
	static boolean shouldExit = false;

	public static void main(String[] args) {
		String userChoice;
		String menu = getMenuString();
		System.out.println(menu);

		do {
			printCommandLine();
			userChoice = userInput.nextLine().toLowerCase();
			String[] userChoiceSplit = userChoice.split(" ");
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
				} else if(command.equals(Constants.CMD_EXIT)){
					shouldExit = true;
					break;
				}

				ClientMain.clientThread.sendMessage(request);
			} else {
				System.err.println("Please, enter a valid command!");
			}
		} while (!userChoice.equalsIgnoreCase(Constants.CMD_EXIT));

		userInput.close();
		clientThread.close();
	}

//	private static List<String> getChunks(String localFilePath){
//		
//	}
//	
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

	private static String getMenuString() {
		StringBuilder menu = new StringBuilder();
		menu.append("-------Please choose what you would like to do-------\n");
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
