/**
 * 
 */
package ippoz.multilayer.commons.support;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * @author Tommy
 *
 */
public class AppLogger {
	
	private static Logger logger;	
	private static boolean console;
	
	private AppLogger(String logName, String logFolder, boolean console) { 
	    FileHandler fh;  
	    try {  
	    	AppLogger.console = console;
	    	logger = Logger.getLogger("AppLogger"); 
	    	if(logFolder != null) {
		        fh = new FileHandler(logFolder + logName + ".log");  
		        fh.setFormatter(new SimpleFormatter());  
		        logger.addHandler(fh); 
	    	}
	    	logger.setUseParentHandlers(false);
	    } catch (SecurityException e) {  
	    	System.err.println("[Logger] Unable to create logger: permission denied");
	        e.printStackTrace();  
	    } catch (IOException e) {
	    	System.err.println("[Logger] Unable to create logger: unavailable file location");
			e.printStackTrace();
		}  
	}
	
	public static void logException(Class<?> source, Exception ex, String message){
		log("Exception", ex.getClass().getName() + "@" + source.getName(), ex.toString(), Level.SEVERE, true);
		if(console)
			ex.printStackTrace();
	}
	
	public static void logError(Class<?> source, String error, String message){
		log("Error", error + "@" + source.getName(), message, Level.SEVERE, true);
	}
	
	public static void logOngoingInfo(Class<?> source, String message){
		log("Info", source.getName(), message, Level.INFO, false);
	}
	
	public static void logInfo(Class<?> source, String message){
		log("Info", source.getName(), message, Level.INFO, true);
	}
	
	private static void log(String tag, String location, String message, Level level, boolean newLine){
		String aggMessage = "[" + tag + "][" + location + "] " + message;
		if(logger == null)
			new AppLogger("AppLogger", "", true);
		logger.log(level, aggMessage);
		if(console){
			if(newLine)
				System.out.println(aggMessage);
			else System.out.print(aggMessage);
		}
			
	}
	
	public static void showErrorPanel(JFrame frame, String message){
		showMessagePanel(frame, "Error", message, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showWarningPanel(JFrame frame, String message){
		showMessagePanel(frame, "Warning", message, JOptionPane.WARNING_MESSAGE);
	}
	
	public static void showInfoPanel(JFrame frame, String message){
		showMessagePanel(frame, "Info", message, JOptionPane.INFORMATION_MESSAGE);
	}
	
	private static void showMessagePanel(JFrame frame, String caption, String message, int option){
		JOptionPane.showMessageDialog(frame, message, caption, option);
	}
}
