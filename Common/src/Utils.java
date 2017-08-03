
public class Utils {
	public static String getPathName(String path){
		String localPath = path;
		int index = path.indexOf(Constants.FORBIDDEN_SYMBOL);
		if(index > -1){
			int indexOfLastSlash = path.lastIndexOf(Constants.DIR_SEPARATOR);
			String filePath = path.substring(0, indexOfLastSlash);
			String fileName = path.substring(indexOfLastSlash + 1);
			String dirName = Constants.FORBIDDEN_SYMBOL + path.substring(indexOfLastSlash + 1, index);
			localPath = filePath + Constants.DIR_SEPARATOR + dirName + Constants.DIR_SEPARATOR + fileName;
		}
		
		return localPath;
	}
}
