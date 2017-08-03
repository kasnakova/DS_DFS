
public class Constants {
	public static final String ROOT_FOLDER_NAME = "root";
	
	public static final String CMD_INIT = "init";
	public static final String CMD_READ = "read";
	public static final String CMD_WRITE = "write";
	public static final String CMD_DELETE = "delete";
	public static final String CMD_INFO = "info";
	public static final String CMD_OPEN = "open";
	public static final String CMD_LIST = "list";
	public static final String CMD_MAKE = "make";
	public static final String CMD_REMOVE = "remove";
	public static final String CMD_EXIT = "exit";
	public static final String CMD_REGISTER = "register";
	
	public static final String TYPE_MSG = "message";
	public static final String TYPE_HEARTBEAT = "heartbeat";
	public static final String TYPE_SIZE = "size";
	public static final String TYPE_CURR_DIR = "change_current_dir";
	public static final String TYPE_READ = "read_from_storage_server";
	public static final String TYPE_WRITE = "write_from_storage_server";
	public static final String TYPE_SUCCESS_WRITE = "success_write_to_storage_server";
	public static final String TYPE_SUCCESS_REPLICA_WRITE = "success_replica_write_to_storage_server";
	public static final String TYPE_INFO = "info_from_storage";
	public static final String TYPE_REPLICA = "replica_to_storage";
	public static final String TYPE_DELETE = "delete_from_storage";
	public static final String TYPE_DELETE_RESPONSE = "delete_response_from_storage";
	
	public static final String RES_SUCCESS = "success";
	public static final String RES_ERROR = "error";
	public static final String RES_HEARTBEAT = "I_am_alive";

	public static final String DELIMITER = "#`~";
	
	public static final String FILE_EXTENSION = ".txt";
	
	public static final String NAMING_SERVER_HOST = "127.0.0.1";
	public static final int NAMING_SERVER_PORT = 1111;
	
	public static final int HEARTBEAT_INTERVAL_MILIS = 1000;
	public static final int REPLICA_INTERVAL_MILIS = 5000;
}
