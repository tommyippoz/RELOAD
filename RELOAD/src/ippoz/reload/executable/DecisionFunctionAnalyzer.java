/**
 * 
 */
package ippoz.reload.executable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Tommy
 *
 */
public class DecisionFunctionAnalyzer {
	
	private static String targetFile = "thresholdrelevance";
	
	private static String folderName = "../../../Desktop/RELOAD_Data/intermediate/";
	
	private static String[] algs = {"HBOS", "ELKI_COF", "ELKI_LOF", "ELKI_KMEANS", "ELKI_SVM", "ELKI_ODIN", "HBOS, ELKI_KMEANS", "ELKI_FASTABOD"};

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
				Map<String, Integer> bestMap = sortByComparator(algMap.get(alg), false);
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
	
	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order)
    {

        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>()
        {
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

	
	public static LinkedHashMap<String, Integer> sortHashMapByValues(Map<String, Integer> passedMap) {
	    List<String> mapKeys = new ArrayList<>(passedMap.keySet());
	    List<Integer> mapValues = new ArrayList<>(passedMap.values());
	    Collections.sort(mapValues);
	    Collections.sort(mapKeys);

	    LinkedHashMap<String, Integer> sortedMap =
	        new LinkedHashMap<>();

	    Iterator<Integer> valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        Integer val = valueIt.next();
	        Iterator<String> keyIt = mapKeys.iterator();

	        while (keyIt.hasNext()) {
	            String key = keyIt.next();
	            Integer comp1 = passedMap.get(key);
	            Integer comp2 = val;

	            if (comp1.equals(comp2)) {
	                keyIt.remove();
	                sortedMap.put(key, val);
	                break;
	            }
	        }
	    }
	    return sortedMap;
	}

}
