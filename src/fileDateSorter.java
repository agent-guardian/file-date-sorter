import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;

public class fileDateSorter {

	public static void main(String[] args) {
		File cwd = new File(System.getProperty("user.dir"));
		
		//List of each folder for a given date
		HashMap<Date, File> outputDirs = new HashMap<Date, File>();
		
		File[] files = cwd.listFiles();
		
		for(File f : files) {
			
			if(!verifyFileType(f))
				continue;
			
		}
	}
	
	/***
	 * Checks to make sure that the file is not of MIME type "application". This prevents the program from moving itself.
	 * 
	 * @param file File to be verified.
	 * @return Returns true if a valid file type or false otherwise.
	 */
	public static boolean verifyFileType(File file) {
		if(!file.isFile())
			return false;
		
		String mimeType = null;
		
		try {
			mimeType = Files.probeContentType(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//unknown MIME type
		if(mimeType == null)
			return false;
		
		DataFlavor mime = null;
		
		try {
			mime = new DataFlavor(mimeType);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		//unknown MIME type
		if(mime == null)
			return false;
		
		if(mime.getPrimaryType().equals("application"))
			return false;
		
		return true;
	}
	
	public static boolean move(File file, File cwd, HashMap<Date, File> outputDirs) {
		
	}
}
