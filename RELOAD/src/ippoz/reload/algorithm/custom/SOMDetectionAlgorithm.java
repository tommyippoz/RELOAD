/**
 * 
 */
package ippoz.reload.algorithm.custom;

import ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;

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

import javafx.util.Pair;

/**
 * @author Tommy
 *
 */
public class SOMDetectionAlgorithm extends DataSeriesNonSlidingAlgorithm {
	
	/** The Constant K. */
	public static final String DECAY = "decay";
	
	/** The Constant DEFAULT_K. */
	public static final double DEFAULT_DECAY = 0.9;

	/** The Constant K. */
	public static final String BASE_ALPHA = "base_alpha";
	
	/** The Constant DEFAULT_K. */
	public static final double DEFAULT_BASE_ALPHA = 0.6;
	
	/** The Constant K. */
	public static final String MIN_ALPHA = "x";
	
	/** The Constant DEFAULT_K. */
	public static final double DEFAULT_MIN_ALPHA = 0.1;

	private Double[][] weights;
	
	private List<SOMScore> scores;
	
	private int trainIterations;
	
	public SOMDetectionAlgorithm(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf);
		if(conf.hasItem(TMP_FILE)){
			loadFile(getFilename());
		}
	}
	
	private double getDecayRate() {
		return conf.hasItem(DECAY) ? Double.parseDouble(conf.getItem(DECAY)) : DEFAULT_DECAY;
	}

	private double getMinAlpha() {
		return conf.hasItem(MIN_ALPHA) ? Double.parseDouble(conf.getItem(MIN_ALPHA)) : DEFAULT_MIN_ALPHA;
	}

	private double getBaseAlpha() {
		return conf.hasItem(BASE_ALPHA) ? Double.parseDouble(conf.getItem(BASE_ALPHA)) : DEFAULT_BASE_ALPHA;
	}

	@Override
	protected void storeAdditionalPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Double> getTrainScores() {
		List<Double> outScores = new LinkedList<Double>();
		for(SOMScore score : scores){
			outScores.add(score.getSOM());
		}
		return outScores;
	}

	@Override
	public boolean automaticInnerTraining(List<Knowledge> kList, boolean createOutput) {
		List<Snapshot> snapList = Knowledge.toSnapList(kList, getDataSeries());
		
		weights = trainSOM(snapList);
		
		scores = new LinkedList<SOMScore>();
		for(Snapshot snap : snapList){
			scores.add(new SOMScore(Snapshot.snapToString(snap, getDataSeries()), calculateSOM(getSnapValueArray(snap))));
		}
		
		conf.addItem(TMP_FILE, getFilename());
		
		if(createOutput) {
	    	printFile(new File(getFilename()));
		}
		
		return true;
	}
	
	private double calculateSOM(Double[][] w, double[] row) {		
	    double[] D = ComputeInput(row, w);
	    return D[1] / D[0];
	}
	
	private double calculateSOM(double row[]){
		return calculateSOM(weights, row);
	}
	
	private double[] ComputeInput(double row[], Double[][] wMatrix) {
		int i, j;
		double D[] = {0.0, 0.0};
		
	    for(i = 0; i < 2; i++)
	    {
	        for(j = 0; j < row.length; j++)
	        {
	            D[i] += Math.pow((wMatrix[i][j] - row[j]), 2);
	            //System.out.println("D= " + D[i]);
	        } // j
	    } // i
	    
	    return D;
	}

	private Double[][] trainSOM(List<Snapshot> snapList) {
		int DMin;
		
		double baseAlpha = getBaseAlpha();
		double minAlpha = getMinAlpha();
		double decayRate = getDecayRate();
		
		Double w[][] = new Double[2][getDataSeries().size()];
		for(int i=0;i<getDataSeries().size();i++){
			w[0][i] = 0.5;
			w[1][i] = 0.5;
		}
		
		trainIterations = 0;

		while(baseAlpha > minAlpha)
	    {
	        
	        for(int p=0;p<snapList.size();p++) {

	        	double[] snapValues = getSnapValueArray(snapList.get(p));
		        DMin = calculateSOM(w, snapValues) < 1.0 ? 1 : 0;

	            // Update the weights on the winning unit.
	            for(int i = 0; i < getDataSeries().size(); i++)
	            {
	                w[DMin][i] = w[DMin][i] + (baseAlpha * (snapValues[i] - w[DMin][i]));
	                //System.out.println(" w(" + i + ")= " + w[DMin][i]);
	            }
	        }

	        // Reduce the learning rate.
	        baseAlpha = decayRate * baseAlpha;
	        trainIterations++;

	    }
	    	    
	    return w;
	}
	
	@Override
	public Pair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		return new Pair<Double, Object>(calculateSOM(snapArray), null);
	}

	@Override
	protected boolean checkCalculationCondition(double[] snapArray) {
		return weights != null;
	}

	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Load file.
	 *
	 * @param filename the filename
	 */
	public void loadFile(String filename) {
		loadWeightsFile(new File(filename));
		loadScoresFile(new File(filename + "scores"));		
	}
	
	/**
	 * Load scores file.
	 *
	 * @param file the file
	 */
	private void loadScoresFile(File file) {
		BufferedReader reader;
		String readed;
		try {
			if(file.exists()){
				scores = new LinkedList<SOMScore>();
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && readed.split(";").length >= 2)
							scores.add(new SOMScore(readed.split(";")[0], Double.parseDouble(readed.split(";")[1])));
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read SOM Scores file");
		} 
	}
	
	/**
	 * Load observer file.
	 *
	 * @param file the file
	 */
	private void loadWeightsFile(File file){
		BufferedReader reader;
		String readed;
		int i = 0;
		try {
			if(file.exists()){
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				weights = new Double[2][];
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null && i < 2){
						readed = readed.trim();
						List<Double> list = new LinkedList<Double>();
						for(String str : readed.split(",")){
							list.add(Double.parseDouble(str));
						}
						weights[i++] = list.toArray(new Double[list.size()]);
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read Weights (SOM) file");
		} 
	}
	
	/**
	 * Prints the file.
	 *
	 * @param file the file
	 */
	private void printFile(File file) {
		printWeigths(file);
		printScores(new File(file.getPath() + "scores"));
	}
	
	/**
	 * Prints the histograms.
	 *
	 * @param file the file
	 */
	private void printWeigths(File file){
		BufferedWriter writer;
		try {
			if(weights != null && weights.length > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("* weights for SOM (" + trainIterations + " iterations)\n");
				for(int i=0;i<getDataSeries().size();i++){
					
				}
				for(int j=0;j<2;j++){
					for(int i=0;i<getDataSeries().size();i++){
						writer.write(weights[j][i] + ",");
					}
					writer.write("\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write SDO clusters file");
		} 
	}
	
	/**
	 * Prints the scores.
	 *
	 * @param file the file
	 */
	private void printScores(File file){
		BufferedWriter writer;
		try {
			if(scores != null && scores.size() > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("data(enclosed in {});som\n");
				for(SOMScore score : scores){
					writer.write(score.getSnapValue() + ";" + score.getSOM() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write SOM scores file");
		} 
	}
	
	/**
	 * The Class HBOSScore.
	 */
	private class SOMScore {
		
		/** The sdo. */
		private double som;
		
		/** The snap value. */
		private String snapValue;

		/**
		 * Instantiates a new SDO score.
		 *
		 * @param snapValue the snap value
		 * @param hbos the sdo
		 */
		public SOMScore(String snapValue, double som) {
			this.som = som;
			this.snapValue = snapValue;
		}

		/**
		 * Gets the hbos.
		 *
		 * @return the hbos
		 */
		public double getSOM() {
			return som;
		}

		/**
		 * Gets the snap value.
		 *
		 * @return the snap value
		 */
		public String getSnapValue() {
			return snapValue;
		}
		
	}

	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		defPar.put("decay", new String[]{"0.9", "0.95"});
		defPar.put("min_alpha", new String[]{"0.1", "0.2"});
		defPar.put("base_alpha", new String[]{"0.6", "0.8"});
		return defPar;
	}

}
