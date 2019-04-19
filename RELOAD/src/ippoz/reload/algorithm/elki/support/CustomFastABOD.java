/**
 * 
 */
package ippoz.reload.algorithm.elki.support;

import ippoz.reload.algorithm.elki.ELKIAlgorithm;
import ippoz.reload.commons.support.AppLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.lmu.ifi.dbs.elki.algorithm.outlier.anglebased.ABOD;
import de.lmu.ifi.dbs.elki.algorithm.outlier.anglebased.FastABOD;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDoubleDataStore;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDListIter;
import de.lmu.ifi.dbs.elki.database.ids.KNNHeap;
import de.lmu.ifi.dbs.elki.database.ids.KNNList;
import de.lmu.ifi.dbs.elki.database.query.similarity.SimilarityQuery;
import de.lmu.ifi.dbs.elki.database.relation.DoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.MaterializedDoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.bundle.SingleObjectBundle;
import de.lmu.ifi.dbs.elki.distance.similarityfunction.SimilarityFunction;
import de.lmu.ifi.dbs.elki.distance.similarityfunction.kernel.KernelMatrix;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.math.DoubleMinMax;
import de.lmu.ifi.dbs.elki.math.MeanVariance;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.result.outlier.InvertedOutlierScoreMeta;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierScoreMeta;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.GreaterEqualConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;

/**
 * @author Tommy
 *
 */
public class CustomFastABOD<V extends NumberVector> extends ABOD<V> implements ELKIAlgorithm<V> {
	/**
	 * The logger for this class.
	 */
	private static final Logging LOG = Logging.getLogger(FastABOD.class);

	/**
	 * Number of nearest neighbors.
	 */
	protected int k;
	
	private List<ABODResult> resList;
	
	private KernelMatrix kernelMatrix;

	/**
	 * Constructor for Angle-Based Outlier Detection (ABOD).
	 *
	 * @param kernelFunction kernel function to use
	 * @param k Number of nearest neighbors
	 */
	public CustomFastABOD(SimilarityFunction<? super V> kernelFunction, int k) {
		super(kernelFunction);
		this.k = k;
	}
	
	/**
	 * Run Fast-ABOD on the data set.
	 *
	 * @param relation Relation to process
	 * @return Outlier detection result
	 */
	@Override
	public OutlierResult run(Database db, Relation<V> relation) {
		DBIDs ids = relation.getDBIDs();
		// Build a kernel matrix, to make O(n^3) slightly less bad.
		SimilarityQuery<V> sq = db.getSimilarityQuery(relation, kernelFunction);
		kernelMatrix = new KernelMatrix(sq, relation, ids);

		WritableDoubleDataStore abodvalues = DataStoreUtil.makeDoubleStorage(ids, DataStoreFactory.HINT_STATIC);
		DoubleMinMax minmaxabod = new DoubleMinMax();

		resList = new ArrayList<ABODResult>(ids.size());
		
		for(DBIDIter pA = ids.iter(); pA.valid(); pA.advance()) {
			//System.out.println(relation.get(pA));
			double[] abof = calculateABOF(pA, relation.iterDBIDs(), relation);
			//System.out.println(abof[0]);
			resList.add(new ABODResult(db.getBundle(pA), abof[0]));
			minmaxabod.put(abof[0]);
			abodvalues.putDouble(pA, abof[0]);
		}
		
		Collections.sort(resList);

		// Build result representation.
		DoubleRelation scoreResult = new MaterializedDoubleRelation("Angle-Based Outlier Degree", "abod-outlier", abodvalues, relation.getDBIDs());
		OutlierScoreMeta scoreMeta = new InvertedOutlierScoreMeta(minmaxabod.getMin(), minmaxabod.getMax(), 0.0, Double.POSITIVE_INFINITY);
		return new OutlierResult(scoreMeta, scoreResult);

	}
	
	private double[] calculateABOF(DBIDIter instanceIndex, DBIDIter startIndex, Relation<V> relation) {
		MeanVariance s = new MeanVariance();
		KNNHeap nn = DBIDUtil.newHeap(k);
		Map<String, Integer> nOccurrences = new HashMap<String, Integer>();
		final double simAA = kernelMatrix.getSimilarity(instanceIndex, instanceIndex);
		for(DBIDIter nB = startIndex; nB.valid(); nB.advance()) {
			if(DBIDUtil.equal(nB, instanceIndex)) {
				continue;
			}
			double simBB = kernelMatrix.getSimilarity(nB, nB);
			double simAB = kernelMatrix.getSimilarity(instanceIndex, nB);
			double sqdAB = simAA + simBB - simAB - simAB;
			if(!(sqdAB > 0.)) {
				continue;
			}
			//System.out.println("'" + relation.get(nB) + "'");
			if(!nOccurrences.containsKey(relation.get(nB).toString())){
				nn.insert(sqdAB, nB);
				nOccurrences.put(relation.get(nB).toString(), 1);
			} else if(nOccurrences.get(relation.get(nB).toString()) < k - 1){	
				nn.insert(sqdAB, nB);
				nOccurrences.put(relation.get(nB).toString(), nOccurrences.get(relation.get(nB).toString()) + 1);
			}
		}
		
		KNNList nl = nn.toKNNList();
		
		DoubleDBIDListIter iB = nl.iter(), iC = nl.iter();
		for(; iB.valid(); iB.advance()) {
			//System.out.print(relation.get(iB) + " _ ");
			double sqdAB = iB.doubleValue();
			double simAB = kernelMatrix.getSimilarity(instanceIndex, iB);
			if(!(sqdAB > 0.)) {
				continue;
			}
			for(iC.seek(iB.getOffset() + 1); iC.valid(); iC.advance()) {
				double sqdAC = iC.doubleValue();
				double simAC = kernelMatrix.getSimilarity(instanceIndex, iC);
				if(!(sqdAC > 0.)) {
					continue;
				}
				// Exploit bilinearity of scalar product:
				// <B-A, C-A> = <B, C-A> - <A,C-A>
				// = <B,C> - <B,A> - <A,C> + <A,A>
				double simBC = kernelMatrix.getSimilarity(iB, iC);
				double numerator = simBC - simAB - simAC + simAA;
				double div = 1. / (sqdAB * sqdAC);
				if(!Double.isFinite(div) || !Double.isFinite(numerator))
					div = 0;
				s.put(numerator * div, Math.sqrt(div));
			}
		}
		//System.out.println();
		// Sample variance probably would probably be better, but the ABOD
		// publication uses the naive variance.
		double var = s.getNaiveVariance();
		if(var == 0.0)
			return new double[]{0.0, };
		else return new double[]{s.getNaiveVariance(), Double.NaN};
		
	}

	private double getSimilarity(SimilarityQuery<V> sq, V o1, V o2){
		return sq.similarity(o1, o2);
	}
	
	public int rankSingleABOF(V newInstance) {
		if(resList != null && resList.size() > 0) {
			double abof = calculateSingleABOF(newInstance);
			for(int i=resList.size()-1; i>=0;i--){
				if(resList.get(i).getABOF() <= abof){
					return i+1;
				}
			}
		}
		return Integer.MAX_VALUE;
	}
	
	private double hasResult(V newInstance){
		for(ABODResult ar : resList){
			if(ar.getVector().equals(newInstance))
				return ar.getABOF();
		}
		return Double.NaN;
	}
	
	public double calculateSingleABOF(V newInstance) {
		double partialResult;
		if(resList == null || resList.size() == 0) 
			return Double.MAX_VALUE;
		else if(Double.isFinite(partialResult = hasResult(newInstance)))
			return partialResult;
		else {
			SimilarityQuery<V> sq = kernelFunction.instantiate(null);
			MeanVariance s = new MeanVariance();
			List<KNNValue> nn = new LinkedList<KNNValue>();
			Map<String, Integer> nOccurrences = new HashMap<String, Integer>();
			final double simAA = getSimilarity(sq, newInstance, newInstance);
			
			for(int i=0;i<resList.size();i++) {
				double simBB = getSimilarity(sq, resList.get(i).getVector(), resList.get(i).getVector());
				double simAB = getSimilarity(sq, newInstance, resList.get(i).getVector());
				double sqdAB = simAA + simBB - simAB - simAB;
				if(!(sqdAB > 0.)) {
					continue;
				}
				if(!nOccurrences.containsKey(resList.get(i).getVector().toString())){
					nn.add(new KNNValue(sqdAB, i));
					nOccurrences.put(resList.get(i).getVector().toString(), 1);
				} else if(nOccurrences.get(resList.get(i).getVector().toString()) < k - 1){	
					nn.add(new KNNValue(sqdAB, i));
					nOccurrences.put(resList.get(i).getVector().toString(), nOccurrences.get(resList.get(i).getVector().toString()) + 1);
				}
			}
	
			Collections.sort(nn);
			
			for(int j=0;j<(nn.size()<k ? nn.size() : k);j++) {
				
				double sqdAB = nn.get(j).getScore();
				double simAB = getSimilarity(sq, newInstance, resList.get(nn.get(j).getIndex()).getVector());
				
				if(!(sqdAB > 0.)) {
					continue;
				}
				
				for(int x=j+1;x<(nn.size()<k ? nn.size() : k);x++) {
					
					double sqdAC = nn.get(x).getScore();
					double simAC = getSimilarity(sq, newInstance, resList.get(nn.get(x).getIndex()).getVector());
					
					if(!(sqdAC > 0.)) {
						continue;
					
					}
					
					double simBC = getSimilarity(sq, resList.get(nn.get(j).getIndex()).getVector(), resList.get(nn.get(x).getIndex()).getVector());
					double numerator = simBC - simAB - simAC + simAA;
					double div = 1. / (sqdAB * sqdAC);
					
					s.put(numerator * div, Math.sqrt(div));
					
				}
				
			}
			return s.getNaiveVariance();
		}
	}

	@Override
	public TypeInformation[] getInputTypeRestriction() {
		return TypeUtil.array(TypeUtil.NUMBER_VECTOR_FIELD);
	}

	@Override
	protected Logging getLogger() {
		return LOG;
	}

	/**
	 * Parameterization class.
	 *
	 * @author Erich Schubert
	 *
	 * @apiviz.exclude
	 */
	public static class Parameterizer<V extends NumberVector> extends ABOD.Parameterizer<V> {
		/**
		 * Parameter for the nearest neighbors.
		 */
		public static final OptionID K_ID = new OptionID("fastabod.k", "Number of nearest neighbors to use for ABOD.");

		/**
		 * Number of neighbors.
		 */
		protected int k;

		@Override
		protected void makeOptions(Parameterization config) {
			super.makeOptions(config);
			final IntParameter kP = new IntParameter(K_ID) //
			.addConstraint(new GreaterEqualConstraint(3));
			if(config.grab(kP)) {
				k = kP.intValue();
			}
		}

		@Override
		protected FastABOD<V> makeInstance() {
			return new FastABOD<>(kernelFunction, k);
		}
	}

	private class ABODResult implements Comparable<ABODResult> {

		private Vector data;

		private double abof;

		public ABODResult(SingleObjectBundle bundle, double abof) {
			this.abof = abof;
			double[] bValues = ((DoubleVector)bundle.data(1)).getValues();
			data = new Vector(bValues.length);
			for(int i=0;i<data.getDimensionality();i++){
				data.set(i, bValues[i]);
			}
		}
		
		public ABODResult(String vString, String abof) {
			this.abof = Double.parseDouble(abof);
			String[] splitted = vString.split(",");
			data = new Vector(splitted.length);
			for(int i=0;i<data.getDimensionality();i++){
				data.set(i, Double.parseDouble(splitted[i].trim()));
			}
		}
		
		public double getABOF() {
			return abof;
		}
		
		@SuppressWarnings("unchecked")
		public V getVector(){
			return (V) data;
		}

		@Override
		public int compareTo(ABODResult o) {
			return Double.compare(abof, o.getABOF());
		}

		@Override
		public String toString() {
			return "ABODResult [data=" + data.toString() + ", abof=" + abof + "]";
		}

	}
	
	private class KNNValue implements Comparable<KNNValue>{
		
		private double score;
		
		private int index;

		public KNNValue(double score, int index) {
			this.score = score;
			this.index = index;
		}

		public double getScore() {
			return score;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public int compareTo(KNNValue o) {
			return score < o.getScore() ? -1 : score == o.getScore() ? 0 : 1;
		}

		@Override
		public String toString() {
			return "KNNValue [score=" + score + ", index=" + index + "]";
		}
		
	}
	
	public double getDbSize() {
		if(resList != null)
			return resList.size();
		else return 0;
	}

	public void loadFile(String item) {
		BufferedReader reader;
		String readed;
		try {
			resList = new LinkedList<ABODResult>();
			if(new File(item).exists()){
				reader = new BufferedReader(new FileReader(new File(item)));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						resList.add(new ABODResult(readed.split(";")[0], readed.split(";")[1]));
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read ABOD file");
		} 
	}

	public void printFile(File file) {
		BufferedWriter writer;
		try {
			if(resList != null && resList.size() > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("data;abof\n");
				for(ABODResult ar : resList){
					writer.write(ar.getVector().toString() + ";" + ar.getABOF() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write ABOD file");
		} 
	}

	@Override
	public List<Double> getScoresList() {
		ArrayList<Double> list = new ArrayList<Double>(resList.size());
		for(ABODResult abof : resList){
			list.add(abof.getABOF());
		}
		Collections.sort(list);
		return list;
	}

	@Override
	public String getAlgorithmName() {
		return "fabod";
	}
	
}
