/**
 * 
 */
package ippoz.madness.detector.loader;

import ippoz.madness.commons.indicator.Indicator;
import ippoz.madness.commons.layers.LayerType;
import ippoz.reload.commons.support.AppLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class CSVLoader extends SimpleLoader {

	protected File csvFile;
	protected int labelCol;
	protected int experimentRows;
	protected List<Indicator> header;

	protected CSVLoader(List<Integer> runs, File csvFile, Integer[] skip, int labelCol, int experimentRows) {
		super(runs);
		this.csvFile = csvFile;
		this.labelCol = labelCol;
		this.experimentRows = experimentRows;
		loadHeader(skip);
	}
	
	private void loadHeader(Integer[] skip){
		BufferedReader reader = null;
		String readLine = null;
		try {
			header = new LinkedList<Indicator>();
			if(csvFile != null && csvFile.exists()){
				reader = new BufferedReader(new FileReader(csvFile));
				while(reader.ready() && readLine == null){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() == 0 || readLine.startsWith("*"))
							readLine = null;
					}
				}
				readLine = filterInnerCommas(readLine);
				for(String splitted : readLine.split(",")){
					if(!occursIn(skip, header.size()))
						header.add(new Indicator(splitted.replace("\"", ""), LayerType.NO_LAYER, Double.class));
					else header.add(null);
				}
				reader.close();
			}
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
		
	}	

	@Override
	public boolean canRead(int index) {
		return super.canRead(getRun(index));
	}

	protected int getRun(int rowIndex){
		return rowIndex / experimentRows;
	}
	
	private static boolean occursIn(Integer[] skip, int item){
		for(int i=0;i<skip.length;i++){
			if(skip[i] == item)
				return true;
		}
		return false;
	}
	
	protected static String filterInnerCommas(String readLine) {
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

	@Override
	public String getName() {
		return "CSV - " + csvFile.getName().split(".")[0];
	}

}
