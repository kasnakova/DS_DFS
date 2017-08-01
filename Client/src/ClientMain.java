import java.util.Scanner;

public class ClientMain {
	// TODO: remove hard-coded things
	final static String host = "localhost";
	final static int port = 1111;
	final static ClientThread clientThread = new ClientThread(ClientMain.host, ClientMain.port);
	final static Scanner userInput = new Scanner(System.in);

	public static void main(String[] args) {
		String userChoice;
		String currentDir = Constants.ROOT_FOLDER_NAME.toUpperCase() + ">";
		String menu = getMenuString();
		System.out.println(menu);
		
		do {
			System.out.println(currentDir);
			userChoice = userInput.nextLine().toLowerCase();
			
			if(userChoice.equals(Constants.CMD_INIT)){
				
			} else if(userChoice.startsWith(Constants.CMD_READ)){
				
			} else if(userChoice.startsWith(Constants.CMD_WRITE)){
				
			} else if(userChoice.startsWith(Constants.CMD_DELETE)){
				
			} else if(userChoice.startsWith(Constants.CMD_INFO)){
				
			} else if(userChoice.startsWith(Constants.CMD_OPEN)){
				
			} else if(userChoice.equals(Constants.CMD_LIST)){
				
			} else if(userChoice.startsWith(Constants.CMD_MAKE)){
				onMakeCommand(userChoice);
			} else if(userChoice.startsWith(Constants.CMD_REMOVE)){
				
			} else if(userChoice.equals(Constants.CMD_EXIT)){
				break;
			} else {
				System.err.println("Please, enter a valid command!");
			}
				
			// ClientMain.clientThread.sendMessage(userChoice);

		} while (!userChoice.equalsIgnoreCase("exit"));

		userInput.close();
	}

	private static void onOpenCommand(){
		System.out.println("Please enter the name of the new directory: ");
		String directoryName = userInput.nextLine();
		ClientMain.clientThread.sendMessage("make" + Constants.DELIMITER + directoryName);
	}
	
	private static void onMakeCommand(String userChoice){
		String directoryName = userChoice.substring(Constants.CMD_MAKE.length() + 1);
		ClientMain.clientThread.sendMessage(Constants.CMD_MAKE + Constants.DELIMITER + directoryName);
	}
	
	private static String getParameterFromCommand(String command, String userChoice){
		String parameter = userChoice.substring(command.length() + 1);
		return parameter;
	}
	
	private static String getMenuString() {
		StringBuilder menu = new StringBuilder();
		menu.append("-------Please choose what you would like to do-------\n");
		menu.append(Constants.CMD_INIT + " - initialize the client storage on a new system (that will delete all previous files and folders and return the available size)\n");
		menu.append(Constants.CMD_READ + " <file_name> - read a file from the DFS (you should specify the name of the file in the place of <file_name>)\n");
		menu.append(Constants.CMD_WRITE + " <file_name> - create a new file with a specified name and write content to it\n");
		menu.append(Constants.CMD_DELETE + " <file_name> - delete a specified file from the DFS\n");
		menu.append(Constants.CMD_INFO + " <file_name> - provides information about the file (any useful information - size, node id, etc.)\n");
		menu.append(Constants.CMD_OPEN + " <directory_name> - opens a directory with the provided name\n");
		menu.append(Constants.CMD_LIST + " - returns list of files, which are stored in the current directory\n");
		menu.append(Constants.CMD_MAKE + " <directory_name> - creates a directory with the specified name\n");
		menu.append(Constants.CMD_REMOVE + " <directory_name> - removes the specified directory\n");
		menu.append(Constants.CMD_EXIT + " - to exit the program\n");
		return menu.toString();
	}
}
