/**
 * 
 */
package ippoz.reload.externalutils;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.utils.logging.AppLogger;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import weka.core.Instances;

/**
 * @author Tommy
 *
 */
public class WEKAUtils {
	
	/**
	 * Translates knowledge list into an Instances object to be processed by WEKA.
	 *
	 * @param kList the k list
	 * @param seriesList the series list
	 * @return the instances
	 */
	public static Instances translateKnowledge(List<Knowledge> kList, List<DataSeries> seriesList) {
		DataSeries targetSeries;
		if(seriesList.size() == 1)
			targetSeries = seriesList.get(0);
		else targetSeries = new MultipleDataSeries(seriesList);
		double[][] dataMatrix = Knowledge.convertKnowledgeIntoMatrix(kList, targetSeries, true, false);
		String[] label = Knowledge.extractLabels(kList, targetSeries, true);
		if(dataMatrix.length > 0)
			return createWEKADatabase(dataMatrix, label, targetSeries);
		else return null;
	}

	/**
	 * Creates the WEKA database, an Instances object.
	 *
	 * @param data the data
	 * @param label the label
	 * @param ds the ds
	 * @return the instances
	 */
	public static Instances createWEKADatabase(double[][] data, String[] label, DataSeries ds){ 
		Instances wInst;
		try {
			wInst = new Instances(getTrainARFFReader(data, label, ds));
			wInst.setClassIndex(ds.size());
			return wInst;
		} catch (IOException ex) {
			AppLogger.logException(WEKAUtils.class, ex, "Unable to create WEKA instances");
			return null;
		}
	}
	
	/**
	 * Gets an ARFF stream reader to create Instances object.
	 *
	 * @param data the data
	 * @param label the label
	 * @param ds the ds
	 * @return the train arff reader
	 */
	private static Reader getTrainARFFReader(double[][] data, String[] label, DataSeries ds) {
		String arff = getStreamHeader(ds, true);
		for(int i=0;i<label.length;i++){
			for(int j=0;j<data[i].length;j++){
				arff = arff + data[i][j] + ",";
			}
			arff = arff + label[i] + "\n";
		}
		return new StringReader(arff);
	}
	
	/**
	 * Gets the stream header.
	 *
	 * @param ds the ds
	 * @param training the training
	 * @return the stream header
	 */
	public static String getStreamHeader(DataSeries ds, boolean training){
		String header = "@relation " + ds.getCompactString() + "\n\n";
		if(ds.size() == 1){
			header = header + "@attribute " + ds.getName() + " numeric\n";
		} else {
			for(DataSeries sds : ((MultipleDataSeries)ds).getSeriesList()){
				header = header + "@attribute " + sds.toString() + " numeric\n";
			}
		}
		if(training)
			header = header + "@attribute class {no, yes}\n";
		header = header + "\n@data\n";
		return header;
	}

}
