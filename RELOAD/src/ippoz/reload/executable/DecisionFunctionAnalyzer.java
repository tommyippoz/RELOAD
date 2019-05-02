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
public class DecisionFunctionAnalyzer {
	
	private static String targetFile = "thresholdrelevance";
	
	private static String folderName = "../../../Desktop/RELOAD_Data/intermediate/";
	
	private static String[] algs = {"HBOS", "ELKI_COF", "ELKI_LOF", "ELKI_KMEANS", "ELKI_SVM", "ELKI_ODIN", "HBOS, ELKI_KMEANS", "ELKI_FastABOD"};

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File(folderName);
		Map<String, Map<String, Integer>> algMap = new HashMap<String, Map<String, Integer>>();
		for(String alg : algs){
			algMap.put(alg, new HashMap<String, Integer>());
			for(File current : file.listFiles()){
				if(current.isDirectory()){
					for(File currentInto : current.listFiles()){
						if(currentInto.getName().contains(targetFile) && currentInto.getName().contains(alg + "_")){
							try {
								BufferedReader reader = new BufferedReader(new FileReader(currentInto));
								reader.readLine();
								while(reader.ready()){
									String readed = reader.readLine();
									if(readed != null && !readed.startsWith("*")){
										readed = readed.trim();
										if(readed.length() > 0 && readed.contains(",")){
											String key = readed.split(",")[0].trim();
											Integer val = Integer.parseInt(readed.split(",")[1].trim());
											if(!algMap.get(alg).containsKey(key))
												algMap.get(alg).put(key, 0);
											algMap.get(alg).put(key, algMap.get(alg).get(key) + val);	
										}
									}
								}
								reader.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			
		}
		
		try {
			for(String alg : algs){
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File(folderName + alg.replace(" ", "_") + "_DecisionFunctionRelevance.csv")));
				writer.write("decision_function, score_avg\n");
				Map<String, Integer> bestMap = algMap.get(alg);
				for(String key : bestMap.keySet()){
					writer.write(key + "," + bestMap.get(key) + "\n");
				}
				writer.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
