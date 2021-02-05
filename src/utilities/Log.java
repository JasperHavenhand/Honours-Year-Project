package utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Based on the Log class from Sebastian Coope's COMP319 SMSManager. (ac.liv.comp319.utils.Log.java)
 * @author Jasper Havenhand
 * 
 */
public final class Log {
	
	private File logFile;
	
	private static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	private static final DateTimeFormatter LOG_ENTRY_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	
	/** The logs that have been opened in this application session. */
	private static HashMap<String,Log> sessionLogs = new HashMap<String,Log>();
	
	public static Log getLog(String name) {
		if (sessionLogs.containsKey(name)) {
			return sessionLogs.get(name);
		}
		Log log = new Log(name);
		sessionLogs.put(name, log);
		return log;
	}
	
	private Log(String name) {
		String logsFolder = Configuration.getInstance().getProperty("logsFolder");
		new File(logsFolder).mkdirs();
		LocalDateTime date = LocalDateTime.now();
		logFile = new File(logsFolder + File.separator + name + "_" 
				+ FILE_NAME_FORMAT.format(date)+".txt");
	}
	
	public void write(String text) {
		try {
			FileWriter writer = new FileWriter(logFile,true);
			
			LocalDateTime dateTime = LocalDateTime.now();
			String timestamp = "[" + LOG_ENTRY_FORMAT.format(dateTime) + "] ";
			
			writer.append(timestamp+text+"\n");
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeError(String text) {
		write("ERROR: " + text);
	}
	
	public void writeWarning(String text) {
		write("WARNING: " + text);
	}
	
	public void writeException(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		write(sw.toString());
		pw.close();
	}
}
