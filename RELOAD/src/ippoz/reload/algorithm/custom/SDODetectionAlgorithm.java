/**
 * 
 */
package ippoz.reload.algorithm.custom;

import ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;

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
import java.util.Random;

/**
 * @author Tommaso Capecchi, Tommaso Zoppi
 *
 */
public class SDODetectionAlgorithm extends DataSeriesNonSlidingAlgorithm {
	
	/** The Constant K. */
	public static final String K = "k";
	
	/** The Constant DEFAULT_K. */
	public static final int DEFAULT_K = 10;

	/** The Constant K. */
	public static final String Q = "q";
	
	/** The Constant DEFAULT_K. */
	public static final double DEFAULT_Q = 0.1;
	
	/** The Constant K. */
	public static final String X = "x";
	
	/** The Constant DEFAULT_K. */
	public static final int DEFAULT_X = 5;

	private List<Point> observers;
	
	private List<SDOScore> scores;
	
	public SDODetectionAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		if(conf.hasItem(TMP_FILE)){
			loadFile(getFilename());
		}
	}
	
	private int getK() {
		return conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K;
	}
	
	private double getQ(int size) {
		return size*(conf.hasItem(Q) ? Double.parseDouble(conf.getItem(Q)) : DEFAULT_Q);
	}
	
	private int getX() {
		return conf.hasItem(X) ? Integer.parseInt(conf.getItem(X)) : DEFAULT_X;
	}

	@Override
	protected void storeAdditionalPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Double> getTrainScores() {
		List<Double> outScores = new LinkedList<Double>();
		for(SDOScore score : scores){
			outScores.add(score.getSDO());
		}
		return outScores;
	}

	@Override
	public boolean automaticInnerTraining(List<Knowledge> kList, boolean createOutput) {
		List<Point> pointDs = new LinkedList<Point>();
		for(Snapshot snap : Knowledge.toSnapList(kList, getDataSeries())){
			pointDs.add(getPoint(snap));
		}
		
		observers = deriveObservers(pointDs);
		
		scores = new LinkedList<SDOScore>();
		for(Point cp : pointDs){
			scores.add(new SDOScore(Arrays.toString(cp.getValues()), calculateSDO(cp)));
		}
		
		conf.addItem(TMP_FILE, getFilename());
		
		if(createOutput) {
	    	printFile(new File(getFilename()));
		}
		
		return true;
	}

	@Override
	protected AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		AlgorithmResult ar;
		double score;
		if(observers != null){
			score = calculateSDO(getPoint(sysSnapshot));
			ar = new AlgorithmResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), score, getConfidence(score));
			getDecisionFunction().assignScore(ar, true);
			return ar;
		} else return AlgorithmResult.error(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
	}
	
	private Point getPoint(Snapshot snap) {
		Double[] values = new Double[getDataSeries().size()];
		if(getDataSeries().size() == 1){
			values[0] = ((DataSeriesSnapshot)snap).getSnapValue().getFirst();
		} else {
			for(int j=0;j<getDataSeries().size();j++){
				values[j] = ((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst();
				
			}
		}
		return new Point(values);
	}

	public List<Point> deriveObservers(List<Point> S){
		List<Point> O = selectRandomObservers(S);
		Map<Point, List<Double>> D = computeDistancesMatrix(S, O);
		Map<Point, List<Integer>> I  = nearestObservers(S, D);
		List<Integer> P = occurrenciesOfObservers(S, O, I);
		List<Point> active = getActiveObserver(O, P, S.size());
		return active;
	}
	
	public double calculateSDO(Point point) {
		double score = 0;
		List<Double> D = computeDistancesFromObservers(point, observers);
		score = avg(D, xNearestObservers(D));
		return score;
	}
	
	private double avg(List<Double> D, List<Integer> I) {
		double score = 0;
		for(Integer i : I) {
			score += D.get(i);
		}
		return score/getX();
	}
	
	private List<Integer> xNearestObservers(List<Double> distances) {
		int count;
		int x = getX();
		List<Integer> closest = new LinkedList<Integer>();
	    for(int i=0;i<distances.size();i++){
	    	count = 0;
	    	for(int j=0;j<distances.size();j++){
	    		if(i != j && distances.get(j) < distances.get(i))
	    			count++;
	    	}
	    	if(count < x)
	    		closest.add(i);
	    }
		return closest;
	}
	
	
	protected LinkedList<Double> clone(LinkedList<Double> D){
		LinkedList<Double> clone = new LinkedList<>();
		for(Double d : D) {
			clone.add(d);
		}
		return clone;
	}

	private List<Double> computeDistancesFromObservers(Point point, List<Point> active) {
		List<Double> D = new LinkedList<>();
		for(Point o : active) {
			D.add(computeDistance(point, o));
		}
		return D;
	}

	
	
	private LinkedList<Point> getActiveObserver(List<Point> O, List<Integer> P, int trainSize){
		LinkedList<Point> active = new LinkedList<>();
		for(int i = 0; i<P.size();i++) {
			if(P.get(i) >= getQ(trainSize)) {
				active.add(O.get(i));
			}
		}
		return active;
	}
	
	private List<Integer> occurrenciesOfObservers(List<Point> S, List<Point> O, Map<Point, List<Integer>> I){
		List<Integer> P = new LinkedList<>();
		for(int i = 0; i< O.size(); i++) {
			int occurrencies = 0;
			for(Point p : S) {
				occurrencies += countOccurrencies(I.get(p),i);
			}
			P.add(occurrencies);
		}
		return P;
	}
	
	private int countOccurrencies(List<Integer> obsIndex, int index) {
		int occurrence = 0;
		for(Integer i : obsIndex) {
			if(i == index) {
				occurrence++;
			}
		}
		return occurrence;
	}
	
	private Map<Point, List<Integer>> nearestObservers(List<Point> S, Map<Point, List<Double>> D){
		Map<Point,List<Integer>> I = new HashMap<>();
		for(Point p : S) {
			I.put(p, xNearestObservers(D.get(p)));
		}
		return I;
	}
	
	/*
	 * D is represented as an HasMap where the Integer represents the index of the current point
	 * and the LinkedList<Double> is an array which contains the distances of that point from the
	 * k observers. The distances are calculated as the Euclidean distance.
	 */
	private Map<Point, List<Double>> computeDistancesMatrix(List<Point> S, List<Point> O){
		Map<Point, List<Double>> D = new HashMap<>();
		for(Point p : S) {
			List<Double> distances = new LinkedList<>();
			for(Point o : O) {
				distances.add(computeDistance(p,o));
			}
			D.put(p, distances);
		}
		return D;
	}
	
	private double computeDistance(Point point, Point observer) {
		return AppUtility.euclideanDistance(point.getValues(), observer.getValues());
	}
	
	private List<Point> selectRandomObservers(List<Point> S){
		List<Point> randObs = new LinkedList<>();
		Random random = new Random();
		for(int i = 0; i<getK(); i++) {
			int randomI = random.nextInt(S.size());
			randObs.add(S.get(randomI));
		}
		return randObs;
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
		loadObserversFile(new File(filename));
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
				scores = new LinkedList<SDOScore>();
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && readed.split(";").length >= 2)
							scores.add(new SDOScore(readed.split(";")[0], Double.parseDouble(readed.split(";")[1])));
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read SDO Scores file");
		} 
	}
	
	/**
	 * Load observer file.
	 *
	 * @param file the file
	 */
	private void loadObserversFile(File file){
		BufferedReader reader;
		String readed;
		try {
			if(file.exists()){
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				observers = new LinkedList<Point>();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						observers.add(new Point(readed.trim()));
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read Observers (SDO) file");
		} 
	}
	
	/**
	 * Prints the file.
	 *
	 * @param file the file
	 */
	private void printFile(File file) {
		printObservers(file);
		printScores(new File(file.getPath() + "scores"));
	}
	
	/**
	 * Prints the histograms.
	 *
	 * @param file the file
	 */
	private void printObservers(File file){
		BufferedWriter writer;
		try {
			if(observers != null && observers.size() > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("observer\n");
				for(Point ob : observers){
					writer.write(ob.toConfString() + "\n");
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
				writer.write("data(enclosed in {});hbos\n");
				for(SDOScore score : scores){
					writer.write(score.getSnapValue() + ";" + score.getSDO() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write SDO scores file");
		} 
	}
	
	private class Point {

		private Double[] values;
		
		private double score;
		
		public Point(Double[] values){
			this.values = values;
		}

		public Point(String scoreString) {
			String[] splitted = scoreString.split(",");
			score = Double.valueOf(splitted[1].trim());
			List<Double> valList = new LinkedList<Double>();
			if(splitted[0] != null && splitted[0].length() > 0){
				splitted[0] = splitted[0].replace("{", "").replace("}", "").trim();
				if(splitted[0].contains(";")){
					for(String s : splitted[0].split(";")){
						valList.add(Double.parseDouble(s.trim()));
					}
				} else valList.add(Double.parseDouble(splitted[0]));
			}
			values = valList.toArray(new Double[valList.size()]);
		}
		
		public Double[] getValues() {
			return values;
		}

		public String toConfString() {
			String outString = "{";
			if(values != null && values.length > 0){
				for(double d : values){
					outString = outString + d + ";";
				}
				outString = outString.substring(0, outString.length()-1) + "}, " + getScore();
			} else outString = "{}, " + getScore();
			return outString;
		}

		public double getScore() {
			return this.score;
		}

		@Override
		public String toString() {
			return Arrays.toString(values) + ":" + score;
		}
		
		

		/*public double getValue(int i) {
			if(values != null && i<values.length)
				return values[i];
			else return Double.NaN;
		}*/
		
	}
	
	/**
	 * The Class HBOSScore.
	 */
	private class SDOScore {
		
		/** The sdo. */
		private double sdo;
		
		/** The snap value. */
		private String snapValue;

		/**
		 * Instantiates a new SDO score.
		 *
		 * @param snapValue the snap value
		 * @param hbos the sdo
		 */
		public SDOScore(String snapValue, double sdo) {
			this.sdo = sdo;
			this.snapValue = snapValue;
		}

		/**
		 * Gets the hbos.
		 *
		 * @return the hbos
		 */
		public double getSDO() {
			return sdo;
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
		defPar.put("k", new String[]{"20", "50", "100"});
		defPar.put("q", new String[]{"0.1", "0.2"});
		defPar.put("x", new String[]{"3", "5", "10"});
		return defPar;
	}

}
