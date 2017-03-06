/**
 * 
 */
package ippoz.multilayer.commons.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Tommy
 *
 */
public class AppUtility {

	public static boolean isWindows(){
		return System.getProperty("os.name").toUpperCase().contains("WIN");
	}
	
	public static boolean isUNIX(){
		return System.getProperty("os.name").toUpperCase().contains("UNIX");
	}
	
	public static Process runScript(String path, String args, boolean setOnFolder, boolean viewOutput) throws IOException{
		Process p;
		BufferedReader reader = null;
		String script = buildScript(path);
		if(setOnFolder)
			p = Runtime.getRuntime().exec(script + " " + args, null, new File((new File(path)).getAbsolutePath().replaceAll((new File(path)).getName(), "")));
		else p = Runtime.getRuntime().exec(script + " " + args);
		if(viewOutput){
			reader = new BufferedReader(new InputStreamReader(
	        p.getInputStream()));
	        while (reader.ready()) {
	            System.out.println(reader.readLine());
	        }
	        reader.close();
        }
		//AppLogger.logInfo(Probe.class, "Executed \"" + script + "\"");
		return p;
	}
	
	private static String buildScript(String path){
		String script = path;
		if(path.endsWith(".jar"))
			script = "java -jar " + path;
		return script;
	}
	
	public static HashMap<String, String> loadPreferences(File prefFile, String[] tags) throws IOException {
		String readed, tag, value;
		BufferedReader reader;
		HashMap<String, String> map = new HashMap<String, String>();
		if(prefFile.exists()){
			reader = new BufferedReader(new FileReader(prefFile));
			while(reader.ready()){
				readed = reader.readLine();
				if(readed.length() > 0) {
					if(readed.contains("=") && readed.split("=").length == 2){
						tag = readed.split("=")[0];
						value = readed.split("=")[1];
						if(tags != null && tags.length > 0){
							for(String current : tags){
								if(current.toUpperCase().equals(tag.toUpperCase())){
									map.put(tag.trim(), value.trim());
									break;
								}
							}
						} else map.put(tag.trim(), value.trim());
					}
				}
			}
			reader.close();
		} else {
			AppLogger.logInfo(AppUtility.class, "Unexisting preference file: " + prefFile.getAbsolutePath());
		}
		return map;
	}
	
	public static String formatMillis(long dateMillis){
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(new Date(dateMillis));
	}
	
	public static Date getDateFromString(String dateString){
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return formatter.parse(dateString);
		} catch (ParseException e) {
			AppLogger.logException(AppUtility.class, e, "Unable to parse date '" + dateString + "'");
			return null;
		}
	}
	
	public static Double calcAvg(Double[] values){
		double mean = 0;
		for(Double d : values){
			mean = mean + d;
		}
		return mean / values.length;
	}
	
	public static Double calcMedian(Double[] values){
		Arrays.sort(values);
		return values[(int)(values.length/2)];
	}
	
	public static Double calcMode(Double[] values){
		int freq = 0, modeFreq = 0;
		double mode = 0;
		Arrays.sort(values);
		for(int i=0;i<values.length;i++){
			if(i > 0){
				if(values[i] == values[i-1])
					freq++;
				else {
					if(freq >= modeFreq){
						mode = values[i-1];
						modeFreq = freq;
						freq = 1;
					}
				}
			} else freq++;
		}
		return mode;
	}
	
	public static Double calcStd(Double[] values, Double mean){
		double std = 0;
		for(Double d : values){
			std = std + Math.pow(d-mean, 2);
		}
		return std / values.length;
	}
	
}
