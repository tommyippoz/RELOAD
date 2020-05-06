/**
 * 
 */
package ippoz.reload.externalutils;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.ValueSeries;

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
	
	private static final double MIN_VARIANCE_SMALL_BINS = 0.99;
	
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
			data = checkForSmallBins(data);
			wInst = new Instances(getTrainARFFReader(data, label, ds));
			wInst.setClassIndex(ds.size());
			return wInst;
		} catch (IOException ex) {
			AppLogger.logException(WEKAUtils.class, ex, "Unable to create WEKA instances");
			return null;
		}
	}
	
	/**
	 * Checks for feature values that may result in smaller bins to be processed by WEKA Discretize.
	 * In this case, it raises up values of feature values to increase variance.
	 * 
	 * @param data
	 * @return data
	 */
	private static double[][] checkForSmallBins(double[][] data) {
		double smallBin[] = null;
		if(data != null && data.length > 0 && data[0] != null){
			smallBin = new double[data[0].length];
			for(int j=0;j<smallBin.length;j++){
				ValueSeries vs = new ValueSeries();
				for(int i=0;i<data.length;i++){
					if(Double.isFinite(data[i][j])){
						vs.addValue(Math.abs(data[i][j]));
					}
				}
				double std = vs.getStd();
				double minnz = vs.getMinimumNonZero();
				if(minnz < MIN_VARIANCE_SMALL_BINS){
					smallBin[j] = MIN_VARIANCE_SMALL_BINS / minnz;
				} else if(std < MIN_VARIANCE_SMALL_BINS){
					smallBin[j] = MIN_VARIANCE_SMALL_BINS / std;
				} else smallBin[j] = 0;
			}
			for(int i=0;i<data.length;i++){
				for(int j=0;j<smallBin.length;j++){
					if(Double.isFinite(smallBin[j]) && smallBin[j] > 1){
						data[i][j] = data[i][j]*smallBin[j];
					}
				}
			}
		}
		return data;
	}

	private static int STRING_BATCHES = 100;
	
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
		String str = "";
		String inner = "";
		for(int i=0;i<label.length;i++){
			str = "";
			for(int j=0;j<data[i].length;j++){
				str = str + data[i][j] + ",";
			}
			inner = inner + str + label[i] + "\n";
			if(i % STRING_BATCHES == 0){
				arff = arff + inner;
				inner = "";
			}
		}
		return new StringReader(arff + inner);
	}
	
	/**
	 * Gets the stream header.
	 *
	 * @param ds the ds
	 * @param training the training
	 * @return the stream header
	 */
	public static String getStreamHeader(DataSeries ds, boolean training){
		String header = "@relation " + ds.getCompactString().replace(" ", "_").replace("\\", "") + "\n\n";
		if(ds.size() == 1){
			if(ds.getName() != null)
				header = header + "@attribute " + ds.getName().trim().replace(" ", "_").replace("\\", "").replace("/", "") + " numeric\n";
			else header = header + "@attribute nullAttr numeric\n";
		} else {
			for(DataSeries sds : ((MultipleDataSeries)ds).getSeriesList()){
				header = header + "@attribute " + sds.toString().replace(" ", "_").replace("\\", "").replace("/", "") + " numeric\n";
			}
		}
		if(training)
			header = header + "@attribute class {no, yes}\n";
		header = header + "\n@data\n";
		return header;
	}

}
