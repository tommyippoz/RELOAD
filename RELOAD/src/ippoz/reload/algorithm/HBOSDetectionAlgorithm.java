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
 * @author Tommy
 *
 */
public class HBOSDetectionAlgorithm extends DataSeriesDetectionAlgorithm implements AutomaticTrainingAlgorithm {

	public static final String HISTOGRAMS = "histograms";
	
	public static final String HISTOGRAM_FACTORY = "hist_type";

	public static final String K = "k";
	
	public static final String THRESHOLD = "threshold";
	
	public static final double DEFAULT_THRESHOLD = 0.8;
	
	private static final String TMP_FILE = "tmp_file";
	
	public static final int DEFAULT_K = 10;
	
	private Map<String, Histograms> histograms;
	
	private List<HBOSScore> scores;
	
	public HBOSDetectionAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		if(conf.hasItem(HISTOGRAMS)){
			histograms = loadFromConfiguration();
			loadFile(getFilename());
			clearLoggedScores();
			logScores(filterScores());
		}
	}
	
	private String getFilename(){
		return getDefaultTmpFolder() + File.separatorChar + getDataSeries().getCompactString().replace("\\", "_").replace("/", "-").replace("*", "_") + ".hbos";
	}
	
	private String getDefaultTmpFolder(){
		return "HBOS_tmp_RELOAD";
	}
	
	@Override
	protected DecisionFunction buildClassifier() {
		double perc = 0.0;
		if(DecisionFunction.checkClassifier(conf.getItem(THRESHOLD))){
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
	
	/*private double loadThreshold() {
		double perc = 0.0;
		if(conf != null && conf.hasItem(THRESHOLD)){
			if(AppUtility.isNumber(conf.getItem(THRESHOLD)))
				perc = Double.parseDouble(conf.getItem(THRESHOLD));
			else perc = DEFAULT_THRESHOLD;
		} else perc = DEFAULT_THRESHOLD;
		return histograms.size()*Math.log(1.0/(perc));
	}*/

	private Map<String, Histograms> loadFromConfiguration(){
		Map<String, Histograms> loadedHist = new HashMap<String, Histograms>();
		for(String histString : conf.getItem(HISTOGRAMS).trim().split("ç")){
			loadedHist.put(histString.trim().split("@")[0].trim(), new Histograms(histString.trim().split("@")[1].trim()));
		}
		return loadedHist;
	}
	
	private String histogramsToConfiguration(){
		String toReturn = "";
		for(String dsName : histograms.keySet()){
			toReturn = toReturn + dsName + "@" + histograms.get(dsName).toConfString() + "ç";
		}
		return toReturn.substring(0, toReturn.length()-1);
	}

	@Override
	public void automaticTraining(List<Knowledge> kList, boolean createOutput) {
		if(conf.hasItem(HISTOGRAM_FACTORY) && conf.getItem(HISTOGRAM_FACTORY).equalsIgnoreCase("DYNAMIC"))
			generateDynamicHistograms(Knowledge.toSnapList(kList, getDataSeries()));
		else generateStaticHistograms(Knowledge.toSnapList(kList, getDataSeries()), getK());
		
		scores = new LinkedList<HBOSScore>();
		for(Snapshot snap : Knowledge.toSnapList(kList, getDataSeries())){
			scores.add(new HBOSScore(snapToString(snap), calculateHBOS(snap)));
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
	}
	
	private String snapToString(Snapshot snap){
		String snapValue = "{";
		if(getDataSeries().size() == 1){
			snapValue = snapValue + ((DataSeriesSnapshot)snap).getSnapValue().getFirst();
		} else if(getDataSeries().size() > 1){
			for(int j=0;j<getDataSeries().size();j++){
				snapValue = snapValue + ((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst() + ", ";
			}
			snapValue = snapValue.substring(0,  snapValue.length()-2);
		}
		return snapValue + "}";
	}

	private List<Double> filterScores() {
		List<Double> list = new LinkedList<Double>();
		for(HBOSScore score : scores){
			list.add(score.getHbos());
		}
		return list;	
	}

	private int getK() {
		return conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K;
	}

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

	private void generateDynamicHistograms(List<Snapshot> snapList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		AlgorithmResult ar;
		if(histograms != null){
			ar = new AlgorithmResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), calculateHBOS(sysSnapshot));
			getDecisionFunction().classifyScore(ar);
			return ar;
		
		} else return AlgorithmResult.error(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
	}
	
	private double calculateHBOS(Snapshot snap){
		double snapValue;
		double hbos;
		if(getDataSeries().size() == 1){
			snapValue = ((DataSeriesSnapshot)snap).getSnapValue().getFirst();
			if(histograms == null || histograms.get(getDataSeries().getName()) == null)
				hbos = Math.log(1.0/histograms.get(histograms.keySet().iterator().next()).getScore(snapValue));
			else hbos = Math.log(1.0/histograms.get(getDataSeries().getName()).getScore(snapValue));
		} else {
			hbos = 0;
			for(int j=0;j<getDataSeries().size();j++){
				snapValue = ((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst();
				hbos = hbos + Math.log(1.0/histograms.get(((MultipleDataSeries)getDataSeries()).getSeries(j).getName()).getScore(snapValue));
			}
		}
		return hbos;
	}

	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}
	
	public void loadFile(String filename) {
		loadHistogramsFile(new File(filename));
		loadScoresFile(new File(filename + "scores"));		
	}
	
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
	
	private void printFile(File file) {
		printHistograms(file);
		printScores(new File(file.getPath() + "scores"));
	}
	
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
	
	private class Histograms {
		
		private int maxItems;
		
		private List<Histogram> histList;
		
		public Histograms(double min, double max, int k) {
			double step = (max-min)/k;
			maxItems = 0;
			histList = new LinkedList<Histogram>();
			for(int i=0;i<k;i++){
				histList.add(new Histogram(min+step*i, min + step*(i+1)));
			}
		}

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
		
		public String toConfString() {
			String toReturn = "";
			for(Histogram hist : histList){
				toReturn = toReturn + hist.toConfString() + "#";
			}
			return toReturn.substring(0, toReturn.length()-1);
		}

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

		public double getMaximumScore() {
			return maxItems;
		}

		public double getScore(double value) {
			for(Histogram hist : histList){
				if(hist.containsValue(value)){
					return 1.0*hist.getScore() / getMaximumScore();
				}
			}
			return 0.0;
		}

		@Override
		public String toString() {
			return "Histograms [maxItems=" + maxItems + ", histList="
					+ histList.size() + "]";
		}
		
	}
	
	private class Histogram {
		
		private double from;
		
		private double to;
		
		private int items;

		public Histogram(double from, double to) {
			this.from = from;
			this.to = to;
			items = 0;
		}
		
		public int getItems() {
			return items;
		}

		public Histogram(String confString) {
			if(confString.trim().length() > 0){
				from = Double.valueOf(confString.split(",")[0]);
				to = Double.valueOf(confString.split(",")[1]);
				items = Integer.valueOf(confString.split(",")[2]);
			}
		}

		public String toConfString() {
			return from + "," + to + "," + items;
		}

		public int addValue(double value) {
			items++;
			return items;
		}

		public double getScore() {
			return items;
		}

		public boolean containsValue(double value) {
			return from <= value && value <= to;
		}

		@Override
		public String toString() {
			return "Histogram [from=" + from + ", to=" + to + ", items="
					+ items + "]";
		}
		
	}
	
	private class HBOSScore {
		
		private double hbos;
		
		private String snapValue;

		public HBOSScore(String snapValue, double hbos) {
			this.hbos = hbos;
			this.snapValue = snapValue;
		}

		public double getHbos() {
			return hbos;
		}

		public String getSnapValue() {
			return snapValue;
		}
		
	}

}
