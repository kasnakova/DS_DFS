import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientLogger {
	public static void log(String data){
		File file = new File(Constants.CLIENT_LOG_PATH);
		
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
			}
		}

		try(BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), true))) {
			String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
					.format(new Date());
			bw.write(date + ": " + data + System.getProperty("line.separator"));
		} catch (IOException e) {
		} 
	}
	
	public static void log(Exception ex){
		log(ex.getMessage());
	}
}
