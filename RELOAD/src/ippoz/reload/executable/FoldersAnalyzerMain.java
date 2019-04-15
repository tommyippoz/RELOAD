/**
 * 
 */
package ippoz.reload.executable;

import ippoz.reload.commons.support.AppUtility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class FoldersAnalyzerMain {
	
	private static String targetFile = "summary.csv";
	
	private static String folderName = "../output_sint/";
	
	private static int refIndex = 16;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File(folderName);
		String headerNested = null;
		Map<String, String> bestMap = new HashMap<String, String>();
		for(File current : file.listFiles()){
			if(current.isDirectory()){
				for(File currentInto : current.listFiles()){
					if(currentInto.getName().equals(targetFile)){
						try {
							BufferedReader reader = new BufferedReader(new FileReader(currentInto));
							if(headerNested == null){
								headerNested = reader.readLine();
							}
							List<String> readedStrings = new LinkedList<String>();
							int okIndex = Integer.MIN_VALUE;
							double okValue = Double.NEGATIVE_INFINITY;
							while(reader.ready()){
								String readed = reader.readLine();
								if(readed != null && !readed.startsWith("*")){
									readed = readed.trim();
									if(readed.length() > 0){
										readedStrings.add(readed);
										if(AppUtility.isNumber(readed.split(",")[refIndex]) && Double.parseDouble(readed.split(",")[refIndex]) > okValue){
											okValue = Double.parseDouble(readed.split(",")[refIndex]);
											okIndex = readedStrings.size();
										}
									}
								}
							}
							if(readedStrings.size() >= 0 && okIndex >= 0 && okIndex < readedStrings.size())
								bestMap.put(current.getName(), readedStrings.get(okIndex));
							reader.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
				}
			}
			
		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(folderName + "outputSummary.csv")));
			String header = "dataset;algorithm;paramaters;;" + headerNested.replace(",", ";");
			writer.write(header + "\n");
			for(String key : bestMap.keySet()){
				writer.write(key.substring(0, key.indexOf("[")) + ";");
				writer.write(key.substring(key.indexOf("[")+1, key.indexOf("]")) + ";");
				if(key.contains("(")) {
					writer.write(key.substring(key.indexOf("(")+1, key.indexOf(")")).split("-")[0].trim() + ";");
					writer.write(key.substring(key.indexOf("(")+1, key.indexOf(")")).split("-")[1].trim() + ";");
				} else writer.write(";;");
				writer.write(bestMap.get(key).replace(",", ";"));
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
