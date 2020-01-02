/**
 * 
 */
package ippoz.reload.commons.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.DecimalFormat;
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
	
	public static String formatDouble(double value, int digits){
		DecimalFormat formatter;
		String format = "#.";
		if(Double.isFinite(value)){
			if(Math.abs(value) > 100000 || (Math.abs(value) < 0.00001 && value != 0)){
				formatter = new DecimalFormat("#.###E0");
			} else {
				for(int i=0;i<digits;i++){
					format = format + "#";
				}
				formatter = new DecimalFormat(format);
				while(isNumber(formatter.format(value)) && Double.valueOf(formatter.format(value)) == 0 && value != 0){
					format = format + "#";
					formatter = new DecimalFormat(format);
				}
			}
			
			return formatter.format(value).replace(",", "."); 
		} else return "-";
	}
	
	public static String formatDouble(double value){
		return formatDouble(value, 2);
	}
	
	/**
	 * Filters inner commas.
	 *
	 * @param readLine the read line
	 * @return the string
	 */
	public static String filterInnerCommas(String readLine) {
		int tDelCount = 0;
		for(int i=0;i<readLine.length();i++){
			if(readLine.charAt(i) == '"' && (i == 0 || readLine.charAt(i-1) != '\\')){
				tDelCount++;
			} else if(readLine.charAt(i) == ',' && tDelCount%2 == 1){
				readLine = readLine.substring(0, i) + ';' + readLine.substring(i+1);
			}
		}
		return readLine;
	}
	
	public static double euclideanDistance(Double[] d1, Double[] d2){
		double res = 0;
		if(d1 == null || d2 == null)
			return Double.MAX_VALUE;
		if(d1.length == d2.length){
			for(int i=0;i<d1.length;i++){
				res = res + Math.pow(d1[i] - d2[i], 2);
			}
		} else return Double.NaN;				
		return Math.sqrt(res);
	}
	
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
		int uncountable = 0;
		if(values != null && values.size() > 0) {
			for(Number d : values){
				if(d instanceof Long && d.longValue() != Long.MAX_VALUE && d.longValue() != Long.MIN_VALUE)
					mean = mean + d.longValue();
				else if(Double.isFinite(d.doubleValue()))
					mean = mean + d.doubleValue();
				else uncountable++;
			}
			return mean / (values.size() - uncountable);
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
			if(d != null && Double.isFinite(d)){
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
			if(Double.isFinite(toPrim.get(i)))
				target[i] = toPrim.get(i);
			else target[i] = 0.0;
		}
		return target;
	}

	public static String[] splitAndPurify(String readed, String sep) {
		String[] splitted = readed.split(sep);
		for(int i=0;i<splitted.length;i++){
			while(splitted[i].length() > 0 && (splitted[i].charAt(splitted[i].length()-1) < ' ' || splitted[i].charAt(splitted[i].length()-1) > 'z')){
				splitted[i] = splitted[i].substring(0, splitted[i].length()-1);
			}
		}
		return splitted;
	}

	public static double calcNorm(String stringArray, double value) {
		if(stringArray != null){
			stringArray.replace("[", "").replace("{", "").replace("]", "").replace("}", "");
			stringArray.trim();
			if(!stringArray.contains(",")){
				if(AppUtility.isNumber(stringArray))
					return Double.parseDouble(stringArray)*value;
				else return Double.NaN;
			} else {
				double count = 0;
				for(String s : stringArray.split(",")){
					if(AppUtility.isNumber(s))
						count = count + Math.pow(Double.parseDouble(s)*value, 2);
				}
				return Math.sqrt(count);
			}
		} else return Double.NaN;
	}
	
	public static double calcNorm(Double[] data, double[] ds) {
		if(data != null){
			if(ds != null || data.length != ds.length){
				double[] d = new double[data.length];
				for(int i=0;i<data.length;i++){
					d[i] = data[i]*ds[i];
				}
				return calcNorm(d);
			} else return calcNorm(data);
		} else return Double.NaN;
	}

	public static double calcNorm(double[] data, double[] ds) {
		if(data != null){
			if(ds != null || data.length != ds.length){
				double[] d = new double[data.length];
				for(int i=0;i<data.length;i++){
					d[i] = data[i]*ds[i];
				}
				return calcNorm(d);
			} else return calcNorm(data);
		} else return Double.NaN;
	}
	
	private static double calcNorm(Double[] data) {
		double count = 0;
		if(data == null)
			return Double.NaN;
		for(double d : data){
			count = count + Math.pow(d, 2);
		}
		return Math.sqrt(count);
	}

	private static double calcNorm(double[] data) {
		double count = 0;
		if(data == null)
			return Double.NaN;
		for(double d : data){
			count = count + Math.pow(d, 2);
		}
		return Math.sqrt(count);
	}
	
}
