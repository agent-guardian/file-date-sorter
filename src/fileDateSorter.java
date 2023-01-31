import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class fileDateSorter {

	/***
	 * File representation of the CWD.
	 */
	private static File cwd;
	
	/***
	 * A HashMap<Date, File> for the valid directories in the CWD that files can be moved to.
	 */
	private static HashMap<Date, File> outputDirs;
	
	
	public static void main(String[] args) {
		cwd = new File(System.getProperty("user.dir"));
		
		//List of each folder for a given date
		outputDirs = new HashMap<Date, File>();
		
		ArrayList<File> files = new ArrayList<File>(Arrays.asList(cwd.listFiles()));
		
		removeDirs(files);
		
		for(File f : files) {
			
			if(!verifyFileType(f))
				continue;
			
			File outputDir = getOutputDir(f);
			
			try {
				Files.move(f.toPath(), new File(outputDir, f.getName()).toPath(), StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/***
	 * Returns the File representation of the appropriate output directory of the given file
	 * or creates one if the appropriate directory doesn't exist.
	 * 
	 * @param file The file to find the appropriate outputDir for
	 * @return The File representation of the appropriate outputDir
	 */
	public static File getOutputDir(File file) {
		Date date = getDate(file.getName());
		
		File outputDir = outputDirs.get(date);
		
		//If there is no outputDir for this date in outputDirs, make a new one and add it to outputDirs
		if(outputDir == null) {
			Calendar.Builder builder = new Calendar.Builder();
			Calendar cal = builder.build();
			cal.setTime(date);

			outputDir = new File(cwd, String.format("%n-%n-%n",
					cal.get(Calendar.DAY_OF_MONTH),
					cal.get(Calendar.MONTH),
					cal.get(Calendar.YEAR)).toString());
			
			outputDir.mkdir();

			outputDirs.put(date, outputDir);
		}
		
		return outputDir;
	}
	
	/***
	 * Removes all directories from the files array and adds and valid output directories into the outputDirs HashMap<Date, File>.
	 * A valid output directory is a folder who's name starts with a date in dd-mm-yyyy format.
	 * 
	 * @param files All the files and directories in the CWD.
	 */
	public static void removeDirs(ArrayList<File> files) {
		for(File f : files) {
			if(!f.isDirectory())
				continue;
			
			//if the directory starts with a date
			if(f.getName().matches("^\\d{1,2}[\\W_]\\d{1,2}[\\W_]\\d{4}")) {
				outputDirs.put(getDate(f.getName()), f);
				files.remove(f);
			}
		}
	}
	
	/***
	 * Converts the date in the beginning of the file name into a Date object. File name must start with a date of the format dd-mm-yyyy,
	 * this must be verified before the method call.
	 * 
	 * @param fileName The already verified name of the file.
	 * @return The Date object representing the date
	 */
	public static Date getDate(String fileName) {
		String[] dateValues = fileName.split("^\\d{1,2}[\\W_]\\d{1,2}[\\W_]\\d{4}")[0].split("[\\W_]");
		
		Calendar.Builder calBuilder = new Calendar.Builder();
		calBuilder.setDate(Integer.parseInt(dateValues[2]), Integer.parseInt(dateValues[1]), Integer.parseInt(dateValues[0]));
		
		return calBuilder.build().getTime();
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
}
