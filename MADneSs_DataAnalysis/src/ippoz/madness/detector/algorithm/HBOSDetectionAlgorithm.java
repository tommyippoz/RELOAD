/**
 * 
 */
package ippoz.madness.detector.algorithm;

import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.dataseries.MultipleDataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.madness.detector.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.commons.support.AppUtility;

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
	
	public static final int DEFAULT_K = 10;
	
	private Map<String, Histograms> histograms;
	
	private double threshold;
	
	public HBOSDetectionAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		if(conf.hasItem(HISTOGRAMS)){
			histograms = loadFromConfiguration();
			threshold = loadThreshold();
		}
	}
	
	private double loadThreshold() {
		double perc = 0.0;
		if(conf != null && conf.hasItem(THRESHOLD)){
			if(AppUtility.isNumber(conf.getItem(THRESHOLD)))
				perc = Double.parseDouble(conf.getItem(THRESHOLD));
			else perc = DEFAULT_THRESHOLD;
		} else perc = DEFAULT_THRESHOLD;
		return histograms.size()*Math.log(1.0/(perc));
	}

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
			generateDynamicHistograms(toSnapList(kList));
		else generateStaticHistograms(toSnapList(kList), getK());
		threshold = loadThreshold();
		if(createOutput)
			conf.addItem(HISTOGRAMS, histogramsToConfiguration());
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
	protected double evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		double hbos;
		if(histograms != null){
			hbos = calculateHBOS(sysSnapshot);
			if(hbos > threshold)
				return 1.0;
			else return 0.0;
		
		} else return 0.0;
	}
	
	private double calculateHBOS(Snapshot snap){
		double snapValue;
		double hbos;
		if(getDataSeries().size() == 1){
			snapValue = ((DataSeriesSnapshot)snap).getSnapValue().getFirst();
			hbos = Math.log(1.0/histograms.get(getDataSeries().getName()).getScore(snapValue));
		} else {
			hbos = 0;
			for(int j=0;j<getDataSeries().size();j++){
				snapValue = ((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst();
				//System.out.println(((MultipleDataSeries)getDataSeries()).getSeries(j).getName() + " - " + Arrays.toString(histograms.keySet().toArray()));
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

}
