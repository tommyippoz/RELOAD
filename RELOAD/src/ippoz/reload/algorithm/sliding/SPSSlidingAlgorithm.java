/**
 * 
 */
package ippoz.reload.algorithm.sliding;

import ippoz.reload.algorithm.DataSeriesSlidingAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.commons.utils.ObjectPair;
import ippoz.reload.decisionfunction.DecisionFunction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.special.Erf;

/**
 * The Class SPSDetector.
 * Instantiates SPS on a given indicator and executes the prediction for the specific experiment.
 *
 * @author Tommy
 */
public class SPSSlidingAlgorithm extends DataSeriesSlidingAlgorithm {
	
	/** The Constant SPS_PDV. */
	public static final String SPS_PDV = "pdv";
	
	/** The Constant SPS_POV. */
	public static final String SPS_POV = "pov";
	
	/** The Constant SPS_PDS. */
	public static final String SPS_PDS = "pds";
	
	/** The Constant SPS_POS. */
	public static final String SPS_POS = "pos";
	
	/** The Constant SPS_POS. */
	public static final String SPS_P = "p";
	
	/** The Constant SPS_DYN_WEIGHT. */
	public static final String SPS_DYN_WEIGHT = "dweight";
	
	/** The pdv. */
	private double pdv;
	
	/** The pov. */
	private double pov;
	
	/** The pds. */
	private double pds;
	
	/** The pos. */
	private double pos;
		
	/** The dynamic weights. */
	private boolean dynamicWeights;

	/**
	 * Instantiates a new SPS detector.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public SPSSlidingAlgorithm(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf);
		if(conf.hasItem(SPS_P)){
			pdv = Double.parseDouble(conf.getItem(SPS_P));
			pov = Double.parseDouble(conf.getItem(SPS_P));
			pds = Double.parseDouble(conf.getItem(SPS_P));
			pos = Double.parseDouble(conf.getItem(SPS_P));
		} else {
			pdv = Double.parseDouble(conf.getItem(SPS_PDV));
			pov = Double.parseDouble(conf.getItem(SPS_POV));
			pds = Double.parseDouble(conf.getItem(SPS_PDS));
			pos = Double.parseDouble(conf.getItem(SPS_POS));
		}
		dynamicWeights = (Double.parseDouble(conf.getItem(SPS_DYN_WEIGHT)) == 1.0);	
	}
	
	@Override
	protected DecisionFunction buildClassifier(ValueSeries vs, boolean flag) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.madness.detector.algorithm.DataSeriesSlidingAlgorithm#evaluateSlidingSnapshot(ippoz.madness.detector.commons.knowledge.SlidingKnowledge, java.util.List, ippoz.madness.detector.commons.knowledge.snapshot.Snapshot)
	 */
	@Override
	protected ObjectPair<Double, Object> evaluateSlidingSnapshot(SlidingKnowledge sKnowledge, List<Snapshot> snapList, Snapshot dsSnapshot) {
		DataVector[] thresholds = calculateTreshold(parseBlocks(snapList));
		DataVector snapValue = snapToDataVector(dsSnapshot);
		double d1 = snapValue.calculateDistance(thresholds[1]);
		double d0 = snapValue.calculateDistance(thresholds[0]);
		double dt = thresholds[0].calculateDistance(thresholds[1]);
		if(Double.isFinite(dt)){
			return new ObjectPair<Double, Object>(d1 + d0 - dt, null);
		} else return new ObjectPair<Double, Object>(Double.NaN, null);
	}

	/**
	 * Parses the blocks.
	 *
	 * @param array the array
	 * @return the list
	 */
	private List<SPSBlock> parseBlocks(List<Snapshot> array) {
		List<SPSBlock> blockList = new LinkedList<SPSBlock>();
		for(Snapshot snap : array){
			if(blockList.size() == 0)
				blockList.add(new SPSBlock(snapToDataVector(snap), null));
			else blockList.add(new SPSBlock(snapToDataVector(snap), blockList.get(blockList.size()-1)));
		}
		return blockList;
	}
		
	private DataVector snapToDataVector(Snapshot snap) {
		DataVector result = new DataVector();
		for(int j=0;j<getDataSeries().size();j++){
			result.add(snap.getDoubleValueFor(getDataSeries().getIndicators()[j]));
		}
		return result;
	}

	/**
	 * Calculates the new thresholds.
	 *
	 * @param observedValues the observed values
	 * @return the new thresholds
	 */
	public DataVector[] calculateTreshold(List<SPSBlock> observedValues){
		DataVector calcTreshold = null;
		if(observedValues == null || observedValues.size() == 0)
			return new DataVector[]{DataVector.generateSingleDataVector(Double.MIN_VALUE, getDataSeries().size()), DataVector.generateSingleDataVector(Double.MAX_VALUE, getDataSeries().size())};
		else if(observedValues.size() == 1){
			return singleThresholds(observedValues.get(0).getObs());
		} else {
			calcTreshold = computeThreshold(observedValues);
			return new DataVector[]{observedValues.get(observedValues.size()-1).getObs().less(calcTreshold), observedValues.get(observedValues.size()-1).getObs().plus(calcTreshold)};
		}
	}
	
	private DataVector[] singleThresholds(DataVector ref){
		DataVector[] single = new DataVector[2];
		single[0] = new DataVector();
		single[1] = new DataVector();
		for(Double value : ref){
			if(value >= 0){
				single[0].add(0.0);
				single[1].add(2*value);
			} else {
				single[0].add(2*value);
				single[1].add(0.0);
			}
		}
		return single;
	}

	/**
	 * Computes thresholds.
	 *
	 * @param observedValues the observed values
	 * @return the computed threshold
	 */
	private DataVector computeThreshold(List<SPSBlock> observedValues) {
		double bounds[];
		DataVector threshold = new DataVector();
		for(int i=0;i<getDataSeries().size();i++){
			bounds = calculateBounds(observedValues, i);
			double pred = Erf.erf(pdv)*Math.sqrt(2.0*bounds[0])*(2.0/3)*Math.pow(observedValues.get(observedValues.size()-1).getTimeDiff(), (3/2));
			double sm = Erf.erf(pov)*Math.sqrt(2.0*bounds[1]);
			threshold.add(pred + sm);
		}
		return threshold;
	}
	
	/**
	 * Calculate bounds.
	 *
	 * @param observedValues the observed values
	 * @param dataVectorIndex 
	 * @return the double[]
	 */
	private double[] calculateBounds(List<SPSBlock> observedValues, int dataVectorIndex){
		int dof = observedValues.size() - 1;
		ChiSquaredDistribution chiSq = new ChiSquaredDistribution(dof);
		double wdf = 0, wof = 0;
		double weigthSum = getWeightSum(observedValues);
		double nWeightSum = getWeigthQuadraticSum(observedValues) / Math.pow(weigthSum, 2);
		double weigthDMean = 0, weigthOMean = 0;
		
		for(int i=0;i<observedValues.size();i++){
			weigthDMean = weigthDMean + getWeigth(i, observedValues)*observedValues.get(i).getDrift().get(dataVectorIndex);
			weigthOMean = weigthOMean + getWeigth(i, observedValues)*observedValues.get(i).getOffset().get(dataVectorIndex);
		}
		
		weigthDMean = weigthDMean/weigthSum;
		weigthOMean = weigthOMean/weigthSum;
		for(int i=0;i<observedValues.size();i++){
			wdf = wdf + (getWeigth(i, observedValues)/weigthSum)*Math.pow(observedValues.get(i).getDrift().get(dataVectorIndex) - weigthDMean, 2);
			wof = wof + (getWeigth(i, observedValues)/weigthSum)*Math.pow(observedValues.get(i).getOffset().get(dataVectorIndex) - weigthOMean, 2);
		}
		
		double result[] = new double[2];
		if(pds == pos){
			double den = dof/chiSq.inverseCumulativeProbability(pds);
			result[0] = wdf/(1-nWeightSum)*den;
			result[1] = wof/(1-nWeightSum)*den;
		} else {
			result[0] = wdf/(1-nWeightSum)*(dof/chiSq.inverseCumulativeProbability(pds));
			result[1] = wof/(1-nWeightSum)*(dof/chiSq.inverseCumulativeProbability(pos));
		}
		
		return result;
	}
	
	/**
	 * Gets the weight of each observation in the sliding window.
	 *
	 * @param obsIndex the observation index
	 * @param observedValues the observed values
	 * @return the weight
	 */
	private double getWeigth(int obsIndex, List<SPSBlock> observedValues){
		if(dynamicWeights){
			return ((obsIndex+1.0)/observedValues.size());
		} else return 1.0;
	}
	
	/**
	 * Gets the weight of each observation in the sliding window.
	 *
	 * @param observedValues the observed values
	 * @return the weight
	 */
	private double getWeigthQuadraticSum(List<SPSBlock> observedValues){
		int nEl = observedValues.size();
		if(dynamicWeights)
			return (nEl+1)*(2*nEl+1)/(6*nEl);
		else return nEl;
	}
	
	/**
	 * Gets the weight sum.
	 *
	 * @param observedValues the observed values
	 * @return the weight sum
	 */
	private double getWeightSum(List<SPSBlock> observedValues){
		if(dynamicWeights){
			return (observedValues.size() + 1.0)/2.0;
		} else return 1.0*observedValues.size();
	}
	
	/**
	 * The Class SPSBlock.
	 */
	private class SPSBlock {
		
		/** The observation value. */
		private DataVector obs;
		
		/** The drift. */
		private DataVector drift;
		
		/** The offset. */
		private DataVector offset;
		
		/**
		 * Instantiates a new SPS block.
		 *
		 * @param obs the observation
		 * @param timestamp the timestamp
		 * @param last the last
		 */
		public SPSBlock(DataVector obs, SPSBlock last) {
			this.obs = obs;
			if(last != null){
				drift = obs.less(last.getDrift()).fraction(2);
				offset = obs.less(last.getObs());
			} else {
				drift = obs;
				offset = obs;
			}
		}

		/**
		 * Gets the observation value.
		 *
		 * @return the observation
		 */
		public DataVector getObs() {
			return obs;
		}
		
		/**
		 * Gets the drift.
		 *
		 * @return the drift
		 */
		public DataVector getDrift() {
			return drift;
		}
		
		/**
		 * Gets the offset.
		 *
		 * @return the offset
		 */
		public DataVector getOffset() {
			return offset;
		}
		
		/**
		 * Gets the time difference.
		 *
		 * @return the time difference
		 */
		public int getTimeDiff(){
			return 1;
		}
		
	}
	
	private static class DataVector extends LinkedList<Double> implements Comparable<DataVector>{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public DataVector(){
			super();
		}

		public double calculateDistance(DataVector other){
			double dist = 0;
			if(other != null && size() == other.size()){
				for(int i=0;i<size();i++){
					dist = dist + Math.pow(get(i) - other.get(i), 2);
				}
				dist = Math.sqrt(dist);
				return dist;
			} else {
				AppLogger.logError(getClass(), "WrongVectorSize", "Vectors have different sizes");
				return Double.NaN;			
			}
		}
		
		public DataVector less(DataVector other){
			DataVector result = new DataVector();
			if(other != null && size() > 0 && size() == other.size()){
				for(int i=0;i<size();i++){
					if(other.get(i) != null){
						
						result.add(get(i) - other.get(i));
					}
				}
			}
			return result;
		}
		
		public DataVector plus(DataVector other) {
			DataVector result = new DataVector();
			if(other != null && size() > 0 && size() == other.size()){
				for(int i=0;i<size();i++){
					if(other != null){
						result.add(get(i) + other.get(i));
					}
				}
			}
			return result;
		}
		
		public DataVector fraction(double value){
			DataVector result = new DataVector();
			for(int i=0;i<size();i++){
				if(Double.isFinite(value)){
					result.add(get(i)/value);
				}
			}
			return result;
		}
		
		private static DataVector generateSingleDataVector(Double value, int size){
			DataVector dv = new DataVector();
			for(int i=0;i<size;i++){
				dv.add(value);
			}
			return dv;
		}

		@Override
		public int compareTo(DataVector other) {
			boolean equal = true;
			boolean biggerThan = true;
			if(other != null && size() == other.size()){
				for(int i=0;i<size();i++){
					if(get(i) != other.get(i))
						equal = false;
					if(get(i) < other.get(i))
						biggerThan = false;
				}
				if(equal)
					return 0;
				else if(biggerThan)
					return 1;
				else return -1;
			} else {
				AppLogger.logError(getClass(), "WrongVectorSize", "Vectors have different sizes");
				return -1;			
			}
		}
		
	}

	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		defPar.put("p", new String[]{"0.9999", "0.999", "0.99", "0.9"});
		defPar.put("dweight", new String[]{"0", "1"});
		return defPar;
	}

	@Override
	protected boolean checkCalculationCondition(double[] snapArray) {
		// TODO Auto-generated method stub
		return false;
	}

}
