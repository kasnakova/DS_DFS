import java.util.Scanner;

public class ClientMain {
	final static ClientThread clientThread = new ClientThread(Constants.NAMING_SERVER_HOST, Constants.NAMING_SERVER_PORT);
	final static Scanner userInput = new Scanner(System.in);
	static String currentDir = Constants.ROOT_FOLDER_NAME.toUpperCase() + ">";

	public static void main(String[] args) {
		String userChoice;
		String menu = getMenuString();
		System.out.println(menu);
		
		do {
			printCommandLine();
			userChoice = userInput.nextLine().toLowerCase();
			String[] userChoiceSplit = userChoice.split(" ");
			if(userChoiceSplit.length > 0){
				String command = userChoiceSplit[0];
				String request = command;
				if(!command.equals(userChoice)){
					String parameter = userChoice.substring(command.length() + 1);
					if(command.equals(Constants.CMD_REMOVE)){
						if(!confirmDirRemoval(parameter)){
							continue;
						}
					}
					
					request = command + Constants.DELIMITER + parameter;
				}
				
				ClientMain.clientThread.sendMessage(request);
			} else {
				System.err.println("Please, enter a valid command!");
			}
		} while (!userChoice.equalsIgnoreCase(Constants.CMD_EXIT));

		userInput.close();
	}

	public static void printCommandLine(){
		System.out.println(ClientMain.currentDir.toUpperCase());
	}
	
	private static boolean confirmDirRemoval(String dir){
		System.out.println("Are you sure you want to remove directory '" + dir + "'. All files and directories in it will also be removed.");
		System.out.println("yes/no");
		String answer = userInput.nextLine().toLowerCase();
		if(answer.equals("yes")){
			return true;
		} else if(answer.equals("no")){
			return false;
		} else {
			System.out.println("You should have answered with 'yes' or 'no'.");
			return false;
		}
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
