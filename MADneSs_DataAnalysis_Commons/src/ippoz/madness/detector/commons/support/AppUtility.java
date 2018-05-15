/**
 * 
 */
package ippoz.madness.detector.commons.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Tommy
 *
 */
public class AppUtility {
	
	public static HashMap<String, String> loadPreferences(File prefFile, String[] tags) throws IOException {
		String readed, tag, value; 
		BufferedReader reader;
		HashMap<String, String> map = new HashMap<String, String>();
		if(prefFile.exists()){ 
			reader = new BufferedReader(new FileReader(prefFile));
			while(reader.ready()){
				readed = reader.readLine();
				if(readed.length() > 0 && !readed.trim().startsWith("*")) {
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
	
	public static boolean isServerUp(int port) {
	    return isServerUp("127.0.0.1", port);
	}
	
	public static boolean isServerUp(String address, int port) {
	    boolean isUp = false;
	    try {
	        Socket socket = new Socket(address, port);
	        isUp = true;
	        socket.close();
	    }
	    catch (IOException e) {}
	    return isUp;
	}
	
	public static String formatMillis(long dateMillis){
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(new Date(dateMillis));
	}
	
	public static Date convertStringToDate(String dateString){
		DateFormat formatter;
		try {
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return formatter.parse(dateString);
		} catch (ParseException ex) {
			AppLogger.logException(AppUtility.class, ex, "Unable to parse date: '" + dateString + "'");
		}
		return null;
	}

	public static boolean isNumber(String value) {
		try {
			Double.parseDouble(value);
			return true;
		} catch(Exception ex){
			return false;
		}
	}
	
	public static boolean isInteger(String item) {
		try {
			Integer.parseInt(item);
			return true;
		} catch(Exception ex){
			return false;
		}
	}
	
	public static double readMillis(){
		return (double)(1.0*System.nanoTime()/1000000.0);
	}
	
	public static Double calcAvg(Collection<? extends Number> values){
		double mean = 0;
		if(values != null && values.size() > 0) {
			for(Number d : values){
				if(d instanceof Long)
					mean = mean + d.longValue();
				else mean = mean + d.doubleValue();
			}
			return mean / values.size();
		} else return 0.0;
	}
	
	public static Double calcAvg(Double[] values){
		int count = 0;
		double mean = 0;
		for(Double d : values){
			if(d != null){
				mean = mean + d;
				count++;
			}
		}
		return mean / count;
	}
	
	public static Double calcStd(Double[] values, Double mean){
		int count = 0;
		double std = 0;
		for(Double d : values){
			if(d != null){
				std = std + Math.pow(d-mean, 2);
				count++;
			}
		}
		return std / count;
	}
	
	public static double calcStd(Collection<Double> values, double mean) {
		return calcStd(values.toArray(new Double[values.size()]), mean);
	}
	
	public static Double calcStd(List<Integer> values, Double mean){
		double std = 0;
		for(Integer d : values){
			std = std + Math.pow(d-mean, 2);
		}
		return std / values.size();
	}
	
	public static double getSecondsBetween(Date current, Date ref){
		if(current.after(ref))
			return (current.getTime() - ref.getTime())/1000;
		else if(current.compareTo(ref) == 0)
			return 0.0;
		else return Double.MAX_VALUE;
	}
	
	public static int getIntSecondsBetween(Date current, Date ref){
		if(current.after(ref))
			return (int)((current.getTime() - ref.getTime())/1000);
		else if(current.compareTo(ref) == 0)
			return 0;
		else return Integer.MAX_VALUE;
	}
	
	public static Map<Double, Double> convertMapTimestamps(Date firstTimestamp, List<TimedValue> observations){
		Map<Double, Double> convertedMap = new TreeMap<Double, Double>();
		if(observations.size() > 0) {
			for(TimedValue tv : observations){
				convertedMap.put(AppUtility.getSecondsBetween(tv.getDate(), firstTimestamp), tv.getValue());
			}
		}
		return convertedMap;
	}

	public static Double calcMedian(Double[] timeSingle) {
		Arrays.sort(timeSingle);
		return timeSingle[(int)(timeSingle.length/2)];
	}
	
	public static double[] toPrimitiveArray(List<Double> toPrim){
		double[] target = new double[toPrim.size()];
		for (int i = 0; i < target.length; i++) {
		   target[i] = toPrim.get(i);                
		}
		return target;
	}
	
}
