/**
 * 
 */
package ippoz.reload.algorithm;

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
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.decisionfunction.LogThresholdDecision;

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

/**
 * The Class HBOSDetectionAlgorithm. Implements the Histogram-Based Outlier Score algorithm.
 *
 * @author Tommy
 */
public class HBOSDetectionAlgorithm extends DataSeriesDetectionAlgorithm implements AutomaticTrainingAlgorithm {

	/** The Constant HISTOGRAMS. */
	public static final String HISTOGRAMS = "histograms";
	
	/** The Constant HISTOGRAM_FACTORY. */
	public static final String HISTOGRAM_FACTORY = "hist_type";

	/** The Constant K. */
	public static final String K = "k";
	
	/** The Constant THRESHOLD. */
	public static final String THRESHOLD = "threshold";
	
	/** The Constant DEFAULT_THRESHOLD. */
	public static final double DEFAULT_THRESHOLD = 0.8;
	
	/** The Constant TMP_FILE. */
	private static final String TMP_FILE = "tmp_file";
	
	/** The Constant DEFAULT_K. */
	public static final int DEFAULT_K = 10;
	
	/** The Constant HBOS_DEFAULT_MAX. */
	private static final double HBOS_DEFAULT_MAX = 10;
	
	/** The map containing the histograms. */
	private Map<String, Histograms> histograms;
	
	/** The HBOS scores used to build histograms. */
	private List<HBOSScore> scores;
	
	/**
	 * Instantiates a new HBOS detection algorithm.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public HBOSDetectionAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		if(conf.hasItem(HISTOGRAMS)){
			histograms = loadFromConfiguration();
			loadFile(getFilename());
			clearLoggedScores();
			logScores(filterScores());
		}
	}
	
	/**
	 * Gets the filename used to store data about scores and histograms.
	 *
	 * @return the filename
	 */
	private String getFilename(){
		return getDefaultTmpFolder() + File.separatorChar + getDataSeries().getCompactString().replace("\\", "_").replace("/", "-").replace("*", "_") + ".hbos";
	}
	
	/**
	 * Gets the default folder used to store temporary data.
	 *
	 * @return the default temporary folder
	 */
	private String getDefaultTmpFolder(){
		return File.separatorChar + "tmp" + File.separatorChar + "hbos_tmp_RELOAD";
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DetectionAlgorithm#buildClassifier()
	 */
	@Override
	protected DecisionFunction buildClassifier() {
		double perc = 0.0;
		if(DecisionFunction.checkDecisionFunction(conf.getItem(THRESHOLD))){
			return super.buildClassifier();
		} else {
			if(conf != null && conf.hasItem(THRESHOLD)){
				if(AppUtility.isNumber(conf.getItem(THRESHOLD)))
					perc = Double.parseDouble(conf.getItem(THRESHOLD));
				else if(conf.getItem(THRESHOLD).contains("LOG(") && conf.getItem(THRESHOLD).contains(")")){
					perc = Double.parseDouble(conf.getItem(THRESHOLD).replace("LOG(", "").replace(")",""));
				} else perc = DEFAULT_THRESHOLD;
			} else perc = DEFAULT_THRESHOLD;
			return new LogThresholdDecision(perc, histograms.size());
		}
	}

	/**
	 * Loads histograms from configuration.
	 *
	 * @return the map of histograms
	 */
	private Map<String, Histograms> loadFromConfiguration(){
		Map<String, Histograms> loadedHist = new HashMap<String, Histograms>();
		for(String histString : conf.getItem(HISTOGRAMS).trim().split("ç")){
			loadedHist.put(histString.trim().split("@")[0].trim(), new Histograms(histString.trim().split("@")[1].trim()));
		}
		return loadedHist;
	}
	
	/**
	 * Stores Histograms to configuration through string.
	 *
	 * @return the string
	 */
	private String histogramsToConfiguration(){
		String toReturn = "";
		for(String dsName : histograms.keySet()){
			toReturn = toReturn + dsName + "@" + histograms.get(dsName).toConfString() + "ç";
		}
		return toReturn.substring(0, toReturn.length()-1);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.AutomaticTrainingAlgorithm#automaticTraining(java.util.List, boolean)
	 */
	@Override
	public boolean automaticTraining(List<Knowledge> kList, boolean createOutput) {
		if(conf.hasItem(HISTOGRAM_FACTORY) && conf.getItem(HISTOGRAM_FACTORY).equalsIgnoreCase("DYNAMIC"))
			generateDynamicHistograms(Knowledge.toSnapList(kList, getDataSeries()));
		else generateStaticHistograms(Knowledge.toSnapList(kList, getDataSeries()), getK());
		
		scores = new LinkedList<HBOSScore>();
		for(Snapshot snap : Knowledge.toSnapList(kList, getDataSeries())){
			scores.add(new HBOSScore(Snapshot.snapToString(snap, getDataSeries()), calculateHBOS(snap)));
		}
		clearLoggedScores();
		logScores(filterScores());
		
		conf.addItem(TMP_FILE, getFilename());
		
		if(createOutput) {
			conf.addItem(HISTOGRAMS, histogramsToConfiguration());
			if(!new File(getDefaultTmpFolder()).exists())
	    		new File(getDefaultTmpFolder()).mkdirs();
	    	printFile(new File(getFilename()));
		}
		
		return true;
	}

	/**
	 * Filter scores.
	 *
	 * @return the list
	 */
	private List<Double> filterScores() {
		List<Double> list = new LinkedList<Double>();
		for(HBOSScore score : scores){
			list.add(score.getHbos());
		}
		return list;	
	}

	/**
	 * Gets the k.
	 *
	 * @return the k
	 */
	private int getK() {
		return conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K;
	}

	/**
	 * Generate static histograms.
	 *
	 * @param snapList the snap list
	 * @param k the k
	 */
	private void generateStaticHistograms(List<Snapshot> snapList, int k) {
		double min, max;
		double snapValue;
		histograms = new HashMap<String, Histograms>();
		if(getDataSeries().size() == 1){
			min = Double.MAX_VALUE;
			max = Double.MIN_VALUE;
			for(Snapshot snap : snapList){
				snapValue = ((DataSeriesSnapshot)snap).getSnapValue().getFirst();
				if(snapValue > max)
					max = snapValue;
				if(snapValue < min)
					min = snapValue;
			}
			histograms.put(getDataSeries().getName(), new Histograms(min, max, k));
			for(Snapshot snap : snapList){
				snapValue = ((DataSeriesSnapshot)snap).getSnapValue().getFirst();
				histograms.get(getDataSeries().getName()).add(snapValue);
			}
		} else {
			for(int j=0;j<getDataSeries().size();j++){
				min = Double.MAX_VALUE;
				max = Double.MIN_VALUE;
				for(Snapshot snap : snapList){
					snapValue = ((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst();
					if(snapValue > max)
						max = snapValue;
					if(snapValue < min)
						min = snapValue;
				}
				histograms.put(((MultipleDataSeries)getDataSeries()).getSeries(j).getName(), new Histograms(min, max, k));
				for(Snapshot snap : snapList){
					snapValue = ((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst();
					histograms.get(((MultipleDataSeries)getDataSeries()).getSeries(j).getName()).add(snapValue);
				}
			}
		}
	}

	/**
	 * Generate dynamic histograms.
	 *
	 * @param snapList the snap list
	 */
	private void generateDynamicHistograms(List<Snapshot> snapList) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DataSeriesDetectionAlgorithm#evaluateDataSeriesSnapshot(ippoz.reload.commons.knowledge.Knowledge, ippoz.reload.commons.knowledge.snapshot.Snapshot, int)
	 */
	@Override
	protected AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		AlgorithmResult ar;
		if(histograms != null){
			ar = new AlgorithmResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), calculateHBOS(sysSnapshot));
			getDecisionFunction().assignScore(ar, true);
			return ar;
		} else return AlgorithmResult.error(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
	}
	
	/**
	 * Calculate hbos.
	 *
	 * @param snap the snap
	 * @return the double
	 */
	private double calculateHBOS(Snapshot snap){
		double snapValue;
		double hbos;
		double temp;
		if(getDataSeries().size() == 1){
			snapValue = ((DataSeriesSnapshot)snap).getSnapValue().getFirst();
			if(histograms == null || histograms.get(getDataSeries().getName()) == null){
				temp = histograms.get(histograms.keySet().iterator().next()).getScore(snapValue);
				if(temp > 0)
					hbos = Math.log(1.0/temp);
				else hbos = HBOS_DEFAULT_MAX;
			} else {
				temp = histograms.get(getDataSeries().getName()).getScore(snapValue);
				if(temp > 0)
					hbos = Math.log(1.0/temp);
				else hbos = HBOS_DEFAULT_MAX;
			}
		} else {
			hbos = 0;
			for(int j=0;j<getDataSeries().size();j++){
				snapValue = ((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst();
				temp = histograms.get(((MultipleDataSeries)getDataSeries()).getSeries(j).getName()).getScore(snapValue);
				if(temp > 0)
					hbos = hbos + Math.log(1.0/temp);
				else hbos = HBOS_DEFAULT_MAX*getDataSeries().size();
			}
		}
		return hbos;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DetectionAlgorithm#printImageResults(java.lang.String, java.lang.String)
	 */
	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DetectionAlgorithm#printTextResults(java.lang.String, java.lang.String)
	 */
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
		loadHistogramsFile(new File(filename));
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
				scores = new LinkedList<HBOSScore>();
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && readed.split(";").length >= 2)
							scores.add(new HBOSScore(readed.split(";")[0], Double.parseDouble(readed.split(";")[1])));
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read KMeans Scores file");
		} 
	}
	
	/**
	 * Load histograms file.
	 *
	 * @param file the file
	 */
	private void loadHistogramsFile(File file){
		BufferedReader reader;
		String readed;
		try {
			if(file.exists()){
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				histograms = new HashMap<String, Histograms>();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						histograms.put(readed.trim().split("@")[0].trim(), new Histograms(readed.trim().split("@")[1].trim()));
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read Histograms (HBOS) file");
		} 
	}
	
	/**
	 * Prints the file.
	 *
	 * @param file the file
	 */
	private void printFile(File file) {
		printHistograms(file);
		printScores(new File(file.getPath() + "scores"));
	}
	
	/**
	 * Prints the histograms.
	 *
	 * @param file the file
	 */
	private void printHistograms(File file){
		BufferedWriter writer;
		try {
			if(histograms != null && histograms.size() > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("histogram\n");
				for(String dsName : histograms.keySet()){
					writer.write(dsName + "@" + histograms.get(dsName).toConfString() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write KMEANS clusters file");
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
				for(HBOSScore score : scores){
					writer.write(score.getSnapValue() + ";" + score.getHbos() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write KMEANS scores file");
		} 
	}
	
	/**
	 * The Class Histograms.
	 */
	private class Histograms {
		
		/** The max items. */
		private int maxItems;
		
		/** The hist list. */
		private List<Histogram> histList;
		
		/**
		 * Instantiates a new histograms.
		 *
		 * @param min the min
		 * @param max the max
		 * @param k the k
		 */
		public Histograms(double min, double max, int k) {
			double step = (max-min)/k;
			maxItems = 0;
			histList = new LinkedList<Histogram>();
			for(int i=0;i<k;i++){
				histList.add(new Histogram(min+step*i, min + step*(i+1)));
			}
		}

		/**
		 * Instantiates a new histograms.
		 *
		 * @param confString the conf string
		 */
		public Histograms(String confString) {
			maxItems = 0;
			histList = new LinkedList<Histogram>();
			if(confString != null && confString.trim().length() > 0){
				for(String subst : confString.split("#")){
					histList.add(new Histogram(subst.trim()));
					if(histList.get(histList.size()-1).getItems() > maxItems)
						maxItems = histList.get(histList.size()-1).getItems();
				}
			}
		}
		
		/**
		 * To conf string.
		 *
		 * @return the string
		 */
		public String toConfString() {
			String toReturn = "";
			for(Histogram hist : histList){
				toReturn = toReturn + hist.toConfString() + "#";
			}
			return toReturn.substring(0, toReturn.length()-1);
		}

		/**
		 * Adds the.
		 *
		 * @param value the value
		 */
		public void add(double value){
			int newHeight = -1;
			for(Histogram hist : histList){
				if(hist.containsValue(value)){
					newHeight = hist.addValue(value);
					if(newHeight > maxItems)
						maxItems = newHeight;
					break;
				}
			}
		}

		/**
		 * Gets the maximum score.
		 *
		 * @return the maximum score
		 */
		public double getMaximumScore() {
			return maxItems;
		}

		/**
		 * Gets the score.
		 *
		 * @param value the value
		 * @return the score
		 */
		public double getScore(double value) {
			for(Histogram hist : histList){
				if(hist.containsValue(value)){
					return 1.0*hist.getScore() / getMaximumScore();
				}
			}
			return 0.0;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Histograms [maxItems=" + maxItems + ", histList="
					+ histList.size() + "]";
		}
		
	}
	
	/**
	 * The Class Histogram.
	 */
	private class Histogram {
		
		/** The from. */
		private double from;
		
		/** The to. */
		private double to;
		
		/** The items. */
		private int items;

		/**
		 * Instantiates a new histogram.
		 *
		 * @param from the from
		 * @param to the to
		 */
		public Histogram(double from, double to) {
			this.from = from;
			this.to = to;
			items = 0;
		}
		
		/**
		 * Gets the items.
		 *
		 * @return the items
		 */
		public int getItems() {
			return items;
		}

		/**
		 * Instantiates a new histogram.
		 *
		 * @param confString the conf string
		 */
		public Histogram(String confString) {
			if(confString.trim().length() > 0){
				from = Double.valueOf(confString.split(",")[0]);
				to = Double.valueOf(confString.split(",")[1]);
				items = Integer.valueOf(confString.split(",")[2]);
			}
		}

		/**
		 * To conf string.
		 *
		 * @return the string
		 */
		public String toConfString() {
			return from + "," + to + "," + items;
		}

		/**
		 * Adds the value.
		 *
		 * @param value the value
		 * @return the int
		 */
		public int addValue(double value) {
			items++;
			return items;
		}

		/**
		 * Gets the score.
		 *
		 * @return the score
		 */
		public double getScore() {
			return items;
		}

		/**
		 * Contains value.
		 *
		 * @param value the value
		 * @return true, if successful
		 */
		public boolean containsValue(double value) {
			return from <= value && value <= to;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Histogram [from=" + from + ", to=" + to + ", items="
					+ items + "]";
		}
		
	}
	
	/**
	 * The Class HBOSScore.
	 */
	private class HBOSScore {
		
		/** The hbos. */
		private double hbos;
		
		/** The snap value. */
		private String snapValue;

		/**
		 * Instantiates a new HBOS score.
		 *
		 * @param snapValue the snap value
		 * @param hbos the hbos
		 */
		public HBOSScore(String snapValue, double hbos) {
			this.hbos = hbos;
			this.snapValue = snapValue;
		}

		/**
		 * Gets the hbos.
		 *
		 * @return the hbos
		 */
		public double getHbos() {
			return hbos;
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

}
