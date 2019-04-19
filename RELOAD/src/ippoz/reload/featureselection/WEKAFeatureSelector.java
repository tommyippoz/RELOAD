/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.utils.logging.AppLogger;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeEvaluator;
import weka.core.Instances;

/**
 * @author Tommy
 *
 */
public abstract class WEKAFeatureSelector extends FeatureSelector {

	public WEKAFeatureSelector(FeatureSelectorType fsType, double selectorThreshold) {
		super(fsType, selectorThreshold);
	}

	@Override
	protected Map<DataSeries, Double> executeSelector(List<DataSeries> seriesList, List<Knowledge> kList) {
		if(seriesList != null && seriesList.size() > 0){
			return executeWEKASelector(seriesList, translateKnowledge(kList, seriesList));
		} else return null;
	}
	
	private Instances translateKnowledge(List<Knowledge> kList, List<DataSeries> seriesList) {
		DataSeries targetSeries = new MultipleDataSeries(seriesList);
		double[][] dataMatrix = Knowledge.convertKnowledgeIntoMatrix(kList, targetSeries, true, false);
		String[] label = Knowledge.extractLabels(kList, targetSeries, true);
		if(dataMatrix.length > 0)
			return createWEKADatabase(dataMatrix, label, targetSeries);
		else return null;
	}

	private Instances createWEKADatabase(double[][] data, String[] label, DataSeries ds){ 
		Instances wInst;
		try {
			wInst = new Instances(getTrainARFFReader(data, label, ds));
			wInst.setClassIndex(ds.size());
			return wInst;
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to create WEKA instances");
			return null;
		}
	}
	
	private Reader getTrainARFFReader(double[][] data, String[] label, DataSeries ds) {
		String arff = getStreamHeader(ds, true);
		for(int i=0;i<label.length;i++){
			for(int j=0;j<data[i].length;j++){
				arff = arff + data[i][j] + ",";
			}
			arff = arff + label[i] + "\n";
		}
		return new StringReader(arff);
	}
	
	protected String getStreamHeader(DataSeries ds, boolean training){
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
	
	private Map<DataSeries, Double> executeWEKASelector(List<DataSeries> seriesList, Instances data){
		ASEvaluation attEval;
		Map<DataSeries, Double> scores = new HashMap<DataSeries, Double>();
		try {
			attEval = instantiateWEKASelector();
			if(attEval instanceof AttributeEvaluator){
				attEval.buildEvaluator(data);
				for(int i=0;i<seriesList.size();i++){
					scores.put(seriesList.get(i), ((AttributeEvaluator)attEval).evaluateAttribute(i));
				}
			} else AppLogger.logError(getClass(), "AttributeSelectorError", "Unable to instantiate correct attribute evaluator");
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to Select Attributes through " + getSelectorName());
		}
		return scores;
	}	
	
	protected abstract ASEvaluation instantiateWEKASelector();

}
