/**
 * 
 */
package ippoz.multilayer.detector.algorithm;

import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.data.ExperimentData;
import ippoz.multilayer.detector.commons.data.MultipleSnapshot;
import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.dataseries.MultipleDataSeries;
import ippoz.multilayer.detector.commons.support.AppLogger;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

/**
 * @author Tommy
 *
 */
public class InvariantChecker extends DetectionAlgorithm implements AutomaticTrainingAlgorithm {
	
	public static final String INVARIANT_TOLERANCE = "inv_tolerance";

	public static final String INVARIANT_WINDOW = "inv_window";
	
	public static final String INVARIANT_COEFFICIENTS = "inv_coefficients";
	
	public static final String INVARIANT_RSQUARED = "inv_rsquared";
	
	private MultipleDataSeries invDs;

	private int window;
	
	private double tolerance;
	
	private double[] coefficients;
	
	private LinkedList<Double> winX, winY;
	
	public InvariantChecker(MultipleDataSeries dataSeries, AlgorithmConfiguration conf) {
		super(conf);
		invDs = dataSeries;
		winX = new LinkedList<Double>();
		winY = new LinkedList<Double>();
		conf.addItem(AlgorithmConfiguration.DETAIL, dataSeries.getSeriesString());
		window = Integer.parseInt(conf.getItem(INVARIANT_WINDOW));
		tolerance = Double.parseDouble(conf.getItem(INVARIANT_TOLERANCE));
		if(conf.getItem(INVARIANT_COEFFICIENTS, false) != null)
			parseCoefficients(conf.getItem(INVARIANT_COEFFICIENTS));
	}
	
	private boolean hasCoefficients(){
		return coefficients != null && coefficients.length > 0;
	}

	private void parseCoefficients(String coeffStr) {
		String[] splitted;
		if(coeffStr != null && coeffStr.length() > 0){
			splitted = coeffStr.split(",");
			coefficients = new double[splitted.length];
			for(int i=0;i<splitted.length;i++){
				coefficients[i] = Double.valueOf(splitted[i].trim());
			}
		} else coefficients = null;
		
	}

	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DataSeries getDataSeries() {
		return invDs;
	}

	@Override
	protected double evaluateSnapshot(Snapshot sysSnapshot) {
		double xt, yt;
		double snapEval = 0.0;
		if(hasCoefficients() && sysSnapshot instanceof MultipleSnapshot){
			xt = ((MultipleSnapshot)sysSnapshot).getFirstSeriesValue().getFirst();
			yt = ((MultipleSnapshot)sysSnapshot).getLastSeriesValue().getFirst();
			if(winX.size() >= window){
				snapEval = Math.abs(coeffScore(xt) - yt) > tolerance ? 1.0 : 0.0;
				winX.removeFirst();
				winY.removeFirst();
			} 
			winX.add(xt);
			winY.add(yt);
		}
		return snapEval;
	}
	
	private double coeffScore(double xt) {
		double cScore = 0.0;
		for(int i=0;i<window;i++){
			cScore = cScore - coefficients[i]*winY.get(i);
		}
		cScore = cScore + xt*coefficients[window];
		for(int i=0;i<window;i++){
			cScore = cScore + coefficients[i+window+1]*winX.get(i);
		}
		return cScore;
	}

	private double[][] buildMatrix(List<ExperimentData> expList){
		int count = 0;
		for(ExperimentData ed : expList){
			if(ed.getSnapshotNumber() > window)
				count = count + ed.getSnapshotNumber() - window;
		}
		return new double[count][];
	}

	@Override
	public void automaticTraining(List<ExperimentData> expList) {
		int rowIndex = 0;
		double[][] x = buildMatrix(expList);
		double[] y = new double[x.length];
		double[] current;
		LinkedList<Double> slidingx = new LinkedList<Double>();
		LinkedList<Double> slidingy = new LinkedList<Double>();
		MultipleSnapshot ms;
		OLSMultipleLinearRegression sr = new OLSMultipleLinearRegression();
		try {
			if(!hasCoefficients()){
				
				for(ExperimentData ed : expList){
					for(int i=0;i<ed.getSnapshotNumber();i++){
						ms = ed.getMultipleSnapshot(i, invDs);
						if(i >= window){
							current = buildArray(slidingy.toArray(new Double[slidingy.size()]), slidingx.toArray(new Double[slidingx.size()]), ms.getLastSeriesValue().getFirst());
							if(!sumsZero(current)){
								x[rowIndex] = current;
								y[rowIndex] = ms.getLastSeriesValue().getFirst();
								rowIndex++;
							}
						}
						if(slidingy.size() >= window){
							slidingy.removeFirst();
							slidingx.removeFirst();
						}
						slidingx.add(ms.getFirstSeriesValue().getFirst());
						slidingy.add(ms.getLastSeriesValue().getFirst());
					}
				}
				coefficients = null;
				if(x.length != rowIndex){
					x = formatMatrix(x, rowIndex);
					y = formatArray(y, rowIndex);
				}
				while(rowIndex > 2*window+1 && coefficients == null){
					try {
						sr.newSampleData(y, x);
						coefficients = sr.estimateRegressionParameters();
					} catch(SingularMatrixException ex){
						rowIndex = (int) ((int)rowIndex*0.9);
						sr = new OLSMultipleLinearRegression();
						x = formatMatrix(x, rowIndex);
						y = formatArray(y, rowIndex);
					}
				} 
				if(coefficients == null){
					sr = null;
				}
			} else AppLogger.logInfo(getClass(), "Checker has already been trained");
		} catch(SingularMatrixException ex){
			AppLogger.logError(getClass(), "", "Unable to get coefficients of " + invDs.getName() + ": singular matrix");
			sr = null;
		} finally {
			conf.addItem(INVARIANT_COEFFICIENTS, stringCoefficients());
			conf.addItem(INVARIANT_RSQUARED, String.valueOf(sr != null ? sr.calculateRSquared() : 0.0));
		}
	}
	
	private double[] formatArray(double[] y, int newSize) {
		double[] newY = new double[newSize];
		for(int i=0;i<newSize;i++){
			newY[i] = y[i];
		}
		return newY;
	}

	private double[][] formatMatrix(double[][] x, int newSize) {
		double[][] newX = new double[newSize][];
		for(int i=0;i<newSize;i++){
			newX[i] = x[i];
		}
		return newX;
	}

	private boolean sumsZero(double[] current) {
		for(int i=0;i<current.length;i++){
			if(Math.abs(current[i]) > 0.0)
				return false;
		}
		return true;
	}

	private String stringCoefficients() {
		String sc = "";
		if(coefficients != null && coefficients.length > 0){
			for(int i=0;i<coefficients.length;i++){
				sc = sc + coefficients[i] + ", ";
			}
		}
		return sc.length() > 0 ? sc.substring(0, sc.length()-2) : "";
	}

	private double[] buildArray(Double[] y, Double[] x, double xt){
		double sw[] = new double[2*window+1];
		for(int i=0;i<window;i++){
			sw[i] = y[i];
		}
		sw[window] = xt;
		for(int i=0;i<window;i++){
			sw[i+window+1] = x[i];
		}
		return sw;
	} 

}
