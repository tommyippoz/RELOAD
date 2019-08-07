/**
 * 
 */
package ippoz.reload.executable;

import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.PreferencesManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class MergingCreator {
	
	private static String MERGING_PREFERENCES = "merging.preferences";
	
	private static String MAIN_FOLDER_TAG = "MAIN_FOLDER";
	
	private static String FILE_EXTENSION_TAG = "FILE_EXTENSION";
	
	private static String SKIP_COLUMNS_TAG = "SKIP_COLUMNS";
	
	private static String ROW_SEPARATOR_TAG = "ROW_SEPARATOR";
	
	private static String OUTPUT_FILE_TAG = "OUTPUT_FILE";
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		PreferencesManager prefManager;
		Map<String, List<List<String>>> filesList = new HashMap<>();
		try {
			if(new File(MERGING_PREFERENCES).exists()){
				prefManager = new PreferencesManager(MERGING_PREFERENCES);
				if(prefManager.hasPreference(MAIN_FOLDER_TAG)){
					File rootFolder = new File(prefManager.getPreference(MAIN_FOLDER_TAG));
					if(rootFolder.exists()){
						String extension = prefManager.getPreference(FILE_EXTENSION_TAG);
						int userID = 1;
						for(File containedFile : rootFolder.listFiles()){
							if(!containedFile.isDirectory() && (extension == null || containedFile.getName().endsWith(extension))){
								filesList.put(containedFile.getName(), loadItem(userID++, containedFile, prefManager));
							}
						}
					}
					printMerged(filesList, new File(prefManager.getPreference(OUTPUT_FILE_TAG)));
				}
			}
		} catch(Exception ex){
			AppLogger.logException(MergingCreator.class, ex, "");
		}
	}

	private static void printMerged(Map<String, List<List<String>>> filesList, File outFile) {
		BufferedWriter writer = null;
		try {
			if(outFile != null) {
				writer = new BufferedWriter(new FileWriter(outFile));
				writer.write("* Merged File\n\n");
				for(String key : filesList.keySet()){
					for(List<String> valueList : filesList.get(key)){
						for(String value : valueList){
							writer.write(value + ",");
						}
						writer.write("\n");
					}
				}
			}
		} catch(Exception ex){
			AppLogger.logException(MergingCreator.class, ex, "Unable to write file");
		} finally {
			try {
				writer.close();
			} catch (IOException ex) {
				AppLogger.logException(MergingCreator.class, ex, "Unable to close output file");
			}
		}
	}

	private static List<List<String>> loadItem(int userID, File containedFile, PreferencesManager prefManager) {
		List<List<String>> list = new LinkedList<>();
		BufferedReader reader;
		try {
			if(containedFile.exists()){
				String separatorString = prefManager.getPreference(ROW_SEPARATOR_TAG);
				Integer[] toSkip = parseColumns(prefManager.getPreference(SKIP_COLUMNS_TAG));
				reader = new BufferedReader(new FileReader(containedFile));
				int rowIndex = 0;
				while(reader.ready()){
					String readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*")){
							if(readed.contains(separatorString)){
								List<String> parsedRow = new LinkedList<>();
								parsedRow.add(containedFile.getName());
								parsedRow.add(userID + "");
								parsedRow.add(rowIndex++ + "");
								int i = 0;
								for(String token : readed.split(separatorString)){
									if(Arrays.binarySearch(toSkip, i++) < 0){
										parsedRow.add(token);
									}
								}
								if(parsedRow.size() > 4)
									list.add(parsedRow);
							}
							
						}
					}
				}
				reader.close();
			}
		} catch(IOException ex){
			AppLogger.logException(MergingCreator.class, ex, "");
		}
		return list;
	}
	
	private static Integer[] parseColumns(String colString) {
		LinkedList<Integer> iList = new LinkedList<Integer>();
		if(colString != null && colString.length() > 0){
			for(String str : colString.split(",")){
				iList.add(new Integer(str.trim()));
			}
			return iList.toArray(new Integer[iList.size()]);
		} else return new Integer[]{};
	}

}
