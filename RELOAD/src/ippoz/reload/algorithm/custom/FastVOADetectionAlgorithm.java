/**
 * 
 */
package ippoz.reload.algorithm.custom;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;

/**
 * @author Tommy
 *
 */
public class FastVOADetectionAlgorithm extends DataSeriesNonSlidingAlgorithm {

	// t. Should be log(n)
	private int nRandomVectors;
		
	// DataSeries Size
	private int dimensions;
		
	private int s1, s2;
	
	private int n;
	
	// Size nRandomVectors, dimensions
	private double[][] randoms;
	
	private List<FVOAScore> scores;
	
	public FastVOADetectionAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		if(conf.hasItem(TMP_FILE)){
			loadFile(getFilename());
		}
	}

	/**
	 * Load file.
	 *
	 * @param filename the filename
	 */
	public void loadFile(String filename) {
		loadDetailsFile(new File(filename));
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
				scores = new LinkedList<FVOAScore>();
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && readed.split(";").length >= 2)
							scores.add(new FVOAScore(readed.split(";")[0], Double.parseDouble(readed.split(";")[2]), Double.parseDouble(readed.split(";")[1])));
					}
				}
				reader.close();
				n = scores.size();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read FastVOA Scores file");
		} 
	}
	
	/**
	 * Load observer file.
	 *
	 * @param file the file
	 */
	private void loadDetailsFile(File file){
		BufferedReader reader;
		String readed;
		List<List<Double>> rList = new LinkedList<>();
		try {
			if(file.exists()){
				scores = new LinkedList<FVOAScore>();
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.startsWith("*")){
							List<Double> list = new LinkedList<>();
							for(String st : readed.trim().split(",")){
								list.add(Double.parseDouble(st.trim()));
							}
							rList.add(list);
						}
					}
				}
				reader.close();
				nRandomVectors = rList.size();
				randoms = new double[rList.size()][rList.get(0).size()];
				for(int i=0;i<rList.size();i++){
					for(int j=0;j<rList.get(0).size();j++){
						randoms[i][j] = rList.get(i).get(j);
					}
				}
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read FastVOA Scores file");
		} 
	}
	
	@Override
	public List<Double> getTrainScores() {
		List<Double> list = new LinkedList<Double>();
		for(FVOAScore score : scores){
			list.add(score.getFastVOA());
		}
		return list;
	}

	@Override
	public boolean automaticInnerTraining(List<Knowledge> kList, boolean createOutput) {
		double[][] data = Knowledge.convertSnapshotListIntoMatrix(
				Knowledge.toSnapList(kList, getDataSeries()), getDataSeries(), true, false);
		
		n = data.length;
		dimensions = getDataSeries().size();
		s1 = 3;
		s2 = 10;
		nRandomVectors = 10;
		
		double[] doubleScores = train(data);
		
		int i = 0;
		scores = new LinkedList<FVOAScore>();
		for(Snapshot snap : Knowledge.toSnapList(kList, getDataSeries())){
			scores.add(new FVOAScore(Snapshot.snapToString(snap, getDataSeries()), doubleScores[i++]));
		}
		
		conf.addItem(TMP_FILE, getFilename());
		
		if(createOutput) {
			if(!new File(getDefaultTmpFolder()).exists())
	    		new File(getDefaultTmpFolder()).mkdirs();
	    	printFile(new File(getFilename()));
		}
		
		return true;
	}

	@Override
	protected AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		AlgorithmResult ar = null;
		if(randoms != null){
			ar = new AlgorithmResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), calculateFastVOA(getSnapValueArray(sysSnapshot)));
			getDecisionFunction().assignScore(ar, true);
			return ar;
		} else return AlgorithmResult.error(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
	}
	
	
	private double calculateFastVOA(double[] data) {
		Double score = containsScore(data);
		if(score != null){
			return score;
		} else {
			if(scores != null)
				return computeFVOA(data);
			else return Double.NaN;
		}
	}
	
	private double computeFVOA(double[] data){
		double f1, f2;
		Map<Integer, List<Double[]>> lp = new HashMap<>();
		Map<Integer, List<Double[]>> rp = new HashMap<>();
		
		// extracting lp, rp
		for(int i=0;i<nRandomVectors;i++){
			double refValue = AppUtility.calcNorm(data, randoms[i]);
			lp.put(i, new LinkedList<>());
			rp.put(i, new LinkedList<>());
			for(FVOAScore score : scores){
				if(refValue > AppUtility.calcNorm(score.getValues(), randoms[i]))
					lp.get(i).add(score.getValues());
				else rp.get(i).add(score.getValues());
					
			}
		}
		
		// Calculating f1
		f1 = 0.0;
		for(int i=0;i<nRandomVectors;i++){
			f1 = f1 + lp.get(i).size()*rp.get(i).size();
		}
		f1 = 2*Math.PI/(nRandomVectors*(n-1)*(n-2))*f1;
		
		// Calculating AMS sketches
		double frobenius = 0.0;
		for(int i=0;i<nRandomVectors;i++){
			double amsl = computeAMSSketch(median(lp.get(i)));
			double amsr = computeAMSSketch(median(rp.get(i)));
			if(Double.isFinite(amsl) && Double.isFinite(amsr))
				frobenius = frobenius + amsr*amsl;
		}
		//frobenius = Math.pow(frobenius, 2);
		
		// Calculating f2
		f2 = frobenius*(4*Math.pow(Math.PI, 2))/(nRandomVectors*(nRandomVectors-1)*(n-1)*(n-2));
		f2 = f2 + 2*Math.PI*f1/(nRandomVectors-1);
		
		return f2 - Math.pow(f1, 2);
	}
	
	private Double[] median(List<Double[]> list) {
		Double[] median;
		if(list != null && list.size() > 0){
			median = new Double[list.get(0).length];
			for(int i=0;i<median.length;i++){
				Double[] arr = new Double[list.size()];
				for(int j=0;j<list.size();j++){
					arr[j] = list.get(j)[i];
				}
				median[i] = AppUtility.calcMedian(arr);
			}
			return median;
		} else return null;
	}

	private double computeAMSSketch(Double[] arr){
		double sketch = 0.0;
		if(arr != null){
			int[] rand = generateRandomPM1(arr.length);
			for(int i=0;i<arr.length;i++){
				sketch = sketch + rand[i]*arr[i];
			}
			return sketch;
		} else return Double.NaN;
		
	}
	
	private Double containsScore(double[] data){
		if(scores == null)
			return null;
		for(FVOAScore score : scores){
			if(score.getSnapValue().equals(Arrays.toString(data).replace("[", "{").replace("]", "}")))
				return score.getFastVOA();
		}
		return null;
	}

	/**
	 * Prints the file.
	 *
	 * @param file the file
	 */
	private void printFile(File file) {
		printDetails(file);
		printScores(new File(file.getPath() + "scores"));
	}
	
	/**
	 * Prints the histograms.
	 *
	 * @param file the file
	 */
	private void printDetails(File file){
		BufferedWriter writer;
		try {
			if(scores != null && scores.size() > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("*random vectors\n");
				for(int i=0;i<randoms.length;i++){
					for(int j=0;j<randoms[i].length;j++){
						writer.write(randoms[i][j] + ",");
					}
					writer.write("\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write FastVOA scores file");
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
				writer.write("data(enclosed in {});norm;fastvoa\n");
				for(FVOAScore score : scores){
					writer.write(score.getSnapValue() + ";" + score.getNorm() + ";" + score.getFastVOA() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write FastVOA scores file");
		} 
	}

	private double[] train(double[][] data){
		randoms = new RandomGaussian().generateMatrix(nRandomVectors, dimensions);
		List<List<InnerVector>> rpVector = randomProjections(data, randoms);
		
		// Calculate F1
		double[] f1 = firstMomentEstimator(rpVector, nRandomVectors, dimensions, data.length);
		
		// Calculate Y
		double[][] y = new double[s2][data.length]; 
		for(int i=0;i<s2;i++){
			for(int j=0;j<s1;j++){
				double[] fn = frobeniusNorm(rpVector, nRandomVectors, dimensions, data.length);
				for(int k=0;k<data.length;k++){
					y[i][k] = y[i][k] + fn[k]; 
				}
			}
			for(int k=0;k<data.length;k++){
				y[i][k] = y[i][k]/s1; 
			}
		}
		
		// Median of Y, Calculate F2
		double[] f2 = new double[data.length];
		for(int i=0;i<data.length;i++){
			f2[i] = calculateMedian(y, i);
		}
		
		// Calculate VAR Estimation
		double[] var = new double[data.length];
		double constCycle = (4*Math.pow(Math.PI, 2))/(nRandomVectors*(nRandomVectors-1)*(data.length-1)*(data.length-2));
		for(int j=0;j<data.length;j++){
			f2[j] = constCycle*f2[j] - f1[j]*(2*Math.PI)/(nRandomVectors-1);
			var[j] = f2[j] - Math.pow(f1[j], 2);
		}
		
		return var;
	}
	
	private double[] frobeniusNorm(List<List<InnerVector>> rpVector, int nRandomVectors, int dimensions, int n) {
		double[] f2 = new double[n];
		int[] sl = generateRandomPM1(n);
		int[] sr = generateRandomPM1(n);
		for(int i=0;i<nRandomVectors;i++){
			double[] amsl = new double[n];
			double[] amsr = new double[n];
			List<InnerVector> iVector = rpVector.get(i);
			for(int j=1;j<n;j++){
				int id1 = iVector.get(j).getID();
				int id2 = iVector.get(j-1).getID();
				amsl[id1] = amsl[id2] + sl[id2];
			}
			for(int j=n-2;j>0;j--){
				int id1 = iVector.get(j).getID();
				int id2 = iVector.get(j+1).getID();
				amsr[id1] = amsr[id2] + sr[id2];
			}
			for(int j=0;j<n;j++){
				f2[j] = f2[j] + amsl[j]*amsr[j];
			}
		}
		return f2;
	}

	private int[] generateRandomPM1(int n) {
		int[] arr = new int[n];
		for(int i=0;i<n;i++){
			arr[i] = Math.random() >= 0.5 ? 1 : -1;
		}
		return arr;
	}

	private double calculateMedian(double[][] y, int index) {
		Double[] values = new Double[s2];
		for(int i=0;i<s2;i++){
			values[i] = y[i][index];
		}
		return AppUtility.calcMedian(values);
	}

	private double[] firstMomentEstimator(List<List<InnerVector>> rpVector, int nRandomVectors, int dimensions, int n){
		double[] f1 = new double[n];
		double[] ci, cr;
		List<InnerVector> iVector;
		for(int i=0;i<nRandomVectors;i++){
			ci = new double[n];
			cr = new double[n];
			iVector = rpVector.get(i);
			for(int j=0;j<n;j++){
				int id = iVector.get(j).getID();
				ci[id] = j;
				cr[id] = n - ci[id];
			}
			for(int j=0;j<n;j++){
				f1[j] = f1[j] + ci[j]*cr[j];
			}
		}
		for(int j=0;j<n;j++){
			f1[j] = f1[j] * ((2*Math.PI)/(nRandomVectors*(n-1)*(n-2)));
		}
		return f1;
	}
	
	private List<List<InnerVector>> randomProjections(double[][] data, double[][] randoms){
		List<List<InnerVector>> list = new ArrayList<>(nRandomVectors);
		for(int i=0;i<nRandomVectors;i++){	
			List<InnerVector> innerList = new ArrayList<>(data.length);
			for(int j=0;j<data.length;j++){
				innerList.add(new InnerVector(j, innerRandom(data, j, randoms, i)));
			}
			Collections.sort(innerList);
			list.add(innerList);
		}
		return list;
	}
	
	private double innerRandom(double[][] data, int dataIndex, double[][] randoms, int randomIndex) {
		double dInner = 0.0;
		for (int i = 0; i < dimensions;i++){
			dInner += data[dataIndex][i] * randoms[randomIndex][i];
		}
		return dInner;
	}

	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void storeAdditionalPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		defPar.put("s1", new String[]{"2", "5", "10"});
		defPar.put("s2", new String[]{"1", "2"});
		return defPar;
	}

	private class InnerVector implements Comparable<InnerVector> {
		
		private int id;
		private double value;

		public InnerVector(int id, double value) {
			this.id	= id;
			this.value	= value;
		}

		public int getID() {
			return id;
		}

		public double getValue() {
			return value;
		}

		@Override
		public int compareTo(InnerVector o) {
			return Double.compare(value, o.getValue());
		}
	
	}
	
	private final class RandomGaussian {

		private Random fRandom = new Random();

		public double getGaussian(double aMean, double aVariance){
			return aMean + fRandom.nextGaussian() * aVariance;
		}

		public double getGaussian(){
			return fRandom.nextGaussian();
		}

		public double[][] generateMatrix(int nRandomVectors, int dimensions) {
			double[][] matrix = new double[nRandomVectors][dimensions];
			for(int i=0;i<nRandomVectors;i++){
				for(int j=0;j<dimensions;j++){
					matrix[i][j] = getGaussian();
				}
			}
			return matrix;
		}

	}
	
	private class FVOAScore {
		
		/** The hbos. */
		private double fastvoa;
		
		private double norm;
		
		private Double[] values;
		
		/** The snap value. */
		private String snapValue;

		/**
		 * Instantiates a new HBOS score.
		 *
		 * @param snapValue the snap value
		 * @param hbos the hbos
		 */
		public FVOAScore(String snapValue, double fastvoa) {
			this(snapValue, fastvoa, null);
		}

		public FVOAScore(String snapValue, double fastvoa, Double norm) {
			this.fastvoa = fastvoa;
			this.snapValue = snapValue;
			if(norm != null)
				this.norm = norm;
			else this.norm = AppUtility.calcNorm(snapValue, 1.0);
			values = extractValues(snapValue);
		}
		
		private Double[] extractValues(String stringArray){
			if(stringArray != null){
				stringArray = stringArray.replace("[", "").replace("{", "").replace("]", "").replace("}", "");
				stringArray.trim();
				if(!stringArray.contains(",")){
					if(AppUtility.isNumber(stringArray))
						return new Double[]{Double.parseDouble(stringArray)};
					else return null;
				} else {
					List<Double> val = new LinkedList<>();
					for(String s : stringArray.split(",")){
						if(AppUtility.isNumber(s.trim()))
							val.add(Double.parseDouble(s.trim()));
						else val.add(Double.NaN);
					}
					return val.toArray(new Double[val.size()]);
				}
			} else return null;
		}

		public double getNorm() {
			return norm;
		}
		
		public Double[] getValues(){
			return values;
		}

		/**
		 * Gets the hbos.
		 *
		 * @return the hbos
		 */
		public double getFastVOA() {
			return fastvoa;
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
