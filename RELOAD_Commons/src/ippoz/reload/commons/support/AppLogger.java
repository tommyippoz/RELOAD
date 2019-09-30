/**
 * 
 */
package ippoz.reload.commons.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The Class AppLogger.
 * Logs all the events in a java Logger and in the related file.
 *
 * @author Tommy
 */
public class AppLogger {
	
	/** The logger. */
	private static Logger logger;	
	
	/** The console flag. */
	private static boolean console;
	
	private static String handlerFilename;
	
	/**
	 * Instantiates a new application logger.
	 *
	 * @param logName the log name
	 * @param logFolder the log folder
	 * @param console the console flag
	 */
	private AppLogger(String logName, String logFolder, boolean console) { 
	    FileHandler fh;
		try {  
	    	AppLogger.console = console;
	    	logger = Logger.getLogger("RELOADLogger"); 
	    	if(logFolder != null) 
	    		handlerFilename = logFolder + "//" + logName + ".log";  
	    	else  {
	    		new File("./log").mkdir();
	    		handlerFilename = "./log/" + logName + ".log";  
	    	}
	    	fh = new FileHandler(handlerFilename);
    		fh.setFormatter(new SimpleFormatter());  
	        logger.addHandler(fh); 
	    	logger.setUseParentHandlers(false);
	    } catch (SecurityException e) {  
	    	System.err.println("[Logger] Unable to create logger: permission denied");
	        e.printStackTrace();  
	    } catch (IOException e) {
	    	System.err.println("[Logger] Unable to create logger: unavailable file location");
			e.printStackTrace();
		}  
	}
	
	/**
	 * Logs an exception.
	 *
	 * @param source the source
	 * @param ex the exception
	 * @param message the message
	 */
	public static void logException(Class<?> source, Exception ex, String message){
		log("Exception", ex.getClass().getName() + "@" + source.getName(), ex.getMessage(), Level.SEVERE, true);
		if(console)
			ex.printStackTrace();
	}
	
	/**
	 * Logs an error.
	 *
	 * @param source the source
	 * @param error the error
	 * @param message the message
	 */
	public static void logError(Class<?> source, String error, String message){
		log("Error", error + "@" + source.getName(), message, Level.SEVERE, true);
	}
	
	/**
	 * Logs an error.
	 *
	 * @param source the source
	 * @param error the error
	 * @param message the message
	 */
	public static void logWarning(Class<?> source, String error, String message){
		log("Warning", error + "@" + source.getName(), message, Level.WARNING, true);
	}
	
	/**
	 * Logs information without newline.
	 *
	 * @param source the source
	 * @param message the message
	 */
	public static void logOngoingInfo(Class<?> source, String message){
		log("Info", source.getName(), message, Level.INFO, false);
	}
	
	/**
	 * Logs information.
	 *
	 * @param source the source
	 * @param message the message
	 */
	public static void logInfo(Class<?> source, String message){
		log("Info", source.getName(), message, Level.INFO, true);
	}
	
	/**
	 * Logging function.
	 *
	 * @param tag the tag
	 * @param location the location
	 * @param message the message
	 * @param level the log level
	 * @param newLine the new line flag
	 */
	private static void log(String tag, String location, String message, Level level, boolean newLine){
		String aggMessage = "[" + tag + "][" + location + "] " + message;
		if(logger == null)
			new AppLogger("RELOADLogger", null, true);
		logger.log(level, "[" + System.currentTimeMillis() + "]" + aggMessage);
		if(console){
			if(newLine)
				System.out.println(aggMessage);
			else System.out.print(aggMessage);
		}
			
	}
	
	/**
	 * Shows the error panel on a JFrame.
	 *
	 * @param frame the frame
	 * @param message the message
	 */
	public static void showErrorPanel(JFrame frame, String message){
		showMessagePanel(frame, "Error", message, JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Shows warning panel on a JFrame.
	 *
	 * @param frame the frame
	 * @param message the message
	 */
	public static void showWarningPanel(JFrame frame, String message){
		showMessagePanel(frame, "Warning", message, JOptionPane.WARNING_MESSAGE);
	}
	
	/**
	 * Shows info panel on a JFrame.
	 *
	 * @param frame the frame
	 * @param message the message
	 */
	public static void showInfoPanel(JFrame frame, String message){
		showMessagePanel(frame, "Info", message, JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Shows basic message panel.
	 *
	 * @param frame the frame
	 * @param caption the caption
	 * @param message the message
	 * @param option the option
	 */
	private static void showMessagePanel(JFrame frame, String caption, String message, int option){
		JOptionPane.showMessageDialog(frame, message, caption, option);
	}

	public static String getErrorsSince(long startTime) {
		BufferedReader reader;
		String readed;
		String output = "";
		try {
			if(logger.getHandlers().length > 0){	
				reader = new BufferedReader(new FileReader(new File(handlerFilename)));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && readed.startsWith("SEVERE")){
							String strTime = readed.substring(readed.indexOf("[")+1, readed.indexOf("]"));
							if(AppUtility.isNumber(strTime) && Long.parseLong(strTime) > startTime)
								output = output + readed + "\n";
						}
					}
				}
				reader.close();
				if(output.length() == 0)
					output = null;
				return output;
			}
		} catch(Exception ex){
			AppLogger.logException(AppLogger.class, ex, "Unable to read log");
		}
		return null;
	}
}
