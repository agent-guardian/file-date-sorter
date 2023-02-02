import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
			
			if(outputDir == null)
				continue;
			
			try {
				Files.move(f.toPath(), new File(outputDir, f.getName()).toPath(), StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/***
	 * Returns the File representation of the appropriate output directory of the given file
	 * or creates one if the appropriate directory doesn't exist. Returns null if the creation
	 * date couldn't be read. If the time of day of the creation date of file is earlier than
	 * 4 am it will consider the file created on the day before, this is coded to use the current
	 * local time zone.
	 * 
	 * @param file The file to find the appropriate outputDir for.
	 * @return The File representation of the appropriate outputDir or null if the creation date
	 * of file couldn't be read.
	 */
	public static File getOutputDir(File file) {
		Date date = getDate(file);
		
		if(date == null)
			return null;
		
		Calendar.Builder builder = new Calendar.Builder();
		Calendar cal = builder.build();
		cal.setTime(date);
		
		if(cal.get(Calendar.HOUR_OF_DAY) < 4)
			cal.add(Calendar.DAY_OF_MONTH, -1);
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		date = cal.getTime();
		
		File outputDir = outputDirs.get(date);
		
		//If there is no outputDir for this date in outputDirs, make a new one and add it to outputDirs
		if(outputDir == null) {
			
			outputDir = new File(cwd, 
					cal.get(Calendar.DAY_OF_MONTH) + "-" +
					(cal.get(Calendar.MONTH) + 1) + "-" +
					cal.get(Calendar.YEAR));
			
			outputDir.mkdir();

			outputDirs.put(date, outputDir);
		}
		
		return outputDir;
	}
	
	/***
	 * Gets the Date object representation of the creation date of the given file. Returns null if the creation date couldn't be read.
	 * 
	 * @param file
	 * @return The Date representation of file's creation date or null if the creation date couldn't be read.
	 */
	public static Date getDate(File file) {
		BasicFileAttributes attr;
		
		try {
			attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return Date.from(attr.creationTime().toInstant());
	}
	
	/***
	 * Removes all directories from the files array and adds and valid output directories into the outputDirs HashMap<Date, File>.
	 * A valid output directory is a folder who's name starts with a date in dd-mm-yyyy format.
	 * 
	 * @param files All the files and directories in the CWD.
	 */
	public static void removeDirs(ArrayList<File> files) {
		Iterator<File> itr = files.iterator();
		while(itr.hasNext()) {
			File f = itr.next();
			if(!f.isDirectory())
				continue;
			
			Matcher m = Pattern.compile("^\\d{1,2}[\\W_]\\d{1,2}[\\W_]\\d{4}").matcher(f.getName());
			
			//If the folder name doesn't start with a name.
			if(!m.find()) {
				itr.remove();
				continue;
			}
			
			String[] dateValues = m.group().split("[\\W_]");
			
			Calendar.Builder calBuilder = new Calendar.Builder();
			calBuilder.setDate(Integer.parseInt(dateValues[2]), Integer.parseInt(dateValues[1]) - 1, Integer.parseInt(dateValues[0]));
			
			Date date = calBuilder.build().getTime();
			
			outputDirs.put(date, f);
			itr.remove();
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
}
