/**
 * 
 */
package ippoz.madness.detector.algorithm.elki.support;

import ippoz.madness.detector.commons.support.AppLogger;

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

import de.lmu.ifi.dbs.elki.algorithm.AbstractDistanceBasedAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.outlier.OutlierAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.outlier.distance.ODIN;
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
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.relation.DoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.MaterializedDoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.bundle.SingleObjectBundle;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.result.outlier.InvertedOutlierScoreMeta;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierScoreMeta;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;

/**
 * @author Tommy
 *
 */
public class CustomODIN extends AbstractDistanceBasedAlgorithm<NumberVector, OutlierResult> implements OutlierAlgorithm {
	/**
	 * Class logger.
	 */
	private static final Logging LOG = Logging.getLogger(ODIN.class);

	/**
	 * Number of neighbors for kNN graph.
	 */
	int k;

	private List<ODINScore> resList;

	/**
	 * Constructor.
	 *
	 * @param distanceFunction Distance function
	 * @param k k parameter
	 */
	public CustomODIN(DistanceFunction<? super NumberVector> distanceFunction, int k) {
		super(distanceFunction);
		this.k = k + 1; // + query point
	}

	/**
	 * Run the ODIN algorithm
	 *
	 * @param database Database to run on.
	 * @param relation Relation to process.
	 * @return ODIN outlier result.
	 */
	public OutlierResult run(Database database, Relation<NumberVector> relation) {
		// Get the query functions:
		DistanceQuery<NumberVector> dq = database.getDistanceQuery(relation, getDistanceFunction());
		KNNQuery<NumberVector> knnq = database.getKNNQuery(dq, k);

		// Get the objects to process, and a data storage for counting and output:
		DBIDs ids = relation.getDBIDs();
		WritableDoubleDataStore scores = DataStoreUtil.makeDoubleStorage(ids, DataStoreFactory.HINT_DB, 0.);

		double inc = 1. / (k - 1);
		double min = Double.POSITIVE_INFINITY, max = 0.0;
		// Process all objects
		for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
			// Find the nearest neighbors (using an index, if available!)
			DBIDs neighbors = knnq.getKNNForDBID(iter, k);
			// For each neighbor, except ourselves, increase the in-degree:
			for(DBIDIter nei = neighbors.iter(); nei.valid(); nei.advance()) {
				if(DBIDUtil.equal(iter, nei)) {
					continue;
				}
				final double value = scores.doubleValue(nei) + inc;
				if(value < min) {
					min = value;
				}
				if(value > max) {
					max = value;
				}
				scores.put(nei, value);
			}
		}

		resList = new ArrayList<ODINScore>(ids.size());
		ids = relation.getDBIDs();
		for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
			resList.add(new ODINScore(database.getBundle(iter), scores.doubleValue(iter)));
		}
		Collections.sort(resList);

		// Wrap the result and add metadata.
		OutlierScoreMeta meta = new InvertedOutlierScoreMeta(min, max, 0., inc * (ids.size() - 1), 1);
		DoubleRelation rel = new MaterializedDoubleRelation("ODIN In-Degree", "odin", scores, ids);
		return new OutlierResult(meta, rel);
	}

	public double calculateSingleODIN(NumberVector newInstance) {
		double partialResult;
		if(resList == null || resList.size() == 0) 
			return Double.MAX_VALUE;
		else if(!Double.isNaN(partialResult = hasResult(newInstance)))
			return partialResult;
		else {
			double odin = 0;
			double inc = 1. / (k - 1);
			List<ODINScore> extList = new ArrayList<ODINScore>(resList.size()+1);
			for(ODINScore os : resList){
				extList.add(os);
			}
			extList.add(new ODINScore(newInstance, 0.0));
			for(ODINScore os : resList){
				if(!os.getVector().equals(newInstance) && isNeighbour(newInstance, extList, os))
					odin = odin + inc;
			}
			return odin;			
		}
	}

	private boolean isNeighbour(NumberVector toCheck, List<ODINScore> extList, ODINScore os){
		List<KNNValue> nn = getKNNs(os.getVector(), extList, true);
		for(int i=0;i<k;i++){
			if(extList.get(nn.get(i).getIndex()).getVector().equals(toCheck))
				return true;
		}
		return false;
	}  

	private List<KNNValue> getKNNs(NumberVector newInstance, List<ODINScore> scoreList, boolean flag){
		DistanceQuery<NumberVector> sq = getDistanceFunction().instantiate(null);
		List<KNNValue> nn = new LinkedList<KNNValue>();
		Map<String, Integer> nOccurrences = new HashMap<String, Integer>();

		for(int i=0;i<scoreList.size();i++) {
			double dist = getSimilarity(sq, newInstance, scoreList.get(i).getVector());
			if(flag){
				if(!nOccurrences.containsKey(scoreList.get(i).getVector().toString())){
					nn.add(new KNNValue(dist, i));
					nOccurrences.put(scoreList.get(i).getVector().toString(), 1);
				} else if(nOccurrences.get(scoreList.get(i).getVector().toString()) < k - 1){	
					nn.add(new KNNValue(dist, i));
					nOccurrences.put(scoreList.get(i).getVector().toString(), nOccurrences.get(scoreList.get(i).getVector().toString()) + 1);
				}
			} else nn.add(new KNNValue(dist, i));
		}

		Collections.sort(nn);
		return nn;
	}

	private double getSimilarity(DistanceQuery<NumberVector> sq, NumberVector arg0, NumberVector arg1) {
		return sq.distance(arg0, arg1);
	}

	private double hasResult(NumberVector newInstance){
		for(ODINScore ar : resList){
			if(ar.getVector().equals(newInstance))
				return ar.getODIN();
		}
		return Double.NaN;
	}

	@Override
	public TypeInformation[] getInputTypeRestriction() {
		return TypeUtil.array(getDistanceFunction().getInputTypeRestriction());
	}

	@Override
	protected Logging getLogger() {
		return LOG;
	}

	private class ODINScore implements Comparable<ODINScore> {

		private NumberVector data;

		private double odin;

		public ODINScore(SingleObjectBundle bundle, double odin) {
			this.odin = odin;
			double[] bValues = ((DoubleVector)bundle.data(1)).getValues();
			data = new Vector(bValues.length);
			for(int i=0;i<data.getDimensionality();i++){
				((Vector)data).set(i, bValues[i]);
			}
		}

		public ODINScore(String vString, String odin) {
			this.odin = Double.parseDouble(odin);
			String[] splitted = vString.split(",");
			data = new Vector(splitted.length);
			for(int i=0;i<data.getDimensionality();i++){
				((Vector)data).set(i, Double.parseDouble(splitted[i].trim()));
			}
		}

		public ODINScore(NumberVector data, double odin) {
			this.data = data;
			this.odin = odin;
		}

		public double getODIN() {
			return odin;
		}

		public NumberVector getVector(){
			return data;
		}

		@Override
		public int compareTo(ODINScore o) {
			return Double.compare(odin, o.getODIN());
		}

		@Override
		public String toString() {
			return "ODINScore [data=" + data.toString() + ", odin=" + odin + "]";
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
			return Double.compare(score, o.getScore());
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

	/**
	 * Parameterization class.
	 *
	 * @author Erich Schubert
	 *
	 * @apiviz.exclude
	 *
	 * @param <O> Object type
	 */
	public static class Parameterizer<O> extends AbstractDistanceBasedAlgorithm.Parameterizer<O> {
		/**
		 * Parameter for the number of nearest neighbors:
		 *
		 * <pre>
		 * -odin.k &lt;int&gt;
		 * </pre>
		 */
		public static final OptionID K_ID = new OptionID("odin.k", "Number of neighbors to use for kNN graph.");

		/**
		 * Number of nearest neighbors to use.
		 */
		int k;

		@Override
		protected void makeOptions(Parameterization config) {
			super.makeOptions(config);

			IntParameter param = new IntParameter(K_ID) //
			.addConstraint(CommonConstraints.GREATER_EQUAL_ONE_INT);
			if(config.grab(param)) {
				k = param.intValue();
			}
		}

		@Override
		protected ODIN<O> makeInstance() {
			return new ODIN<>(distanceFunction, k);
		}
	}

	public void loadFile(String item) {
		BufferedReader reader;
		String readed;
		try {
			resList = new LinkedList<ODINScore>();
			if(new File(item).exists()){
				reader = new BufferedReader(new FileReader(new File(item)));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						resList.add(new ODINScore(readed.split(";")[0], readed.split(";")[1]));
					}
				}
				reader.close();
				Collections.sort(resList);
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read LOF file");
		} 
	}

	public void printFile(File file) {
		BufferedWriter writer;
		try {
			if(resList != null && resList.size() > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("data;odin\n");
				for(ODINScore ar : resList){
					writer.write(ar.getVector().toString() + ";" + ar.getODIN() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write LOF file");
		} 
	}

	public int size() {
		return resList.size();
	}

	public double getScore(int ratio) {
		if(ratio >= 1 && ratio <= size()){
			return resList.get(ratio-1).getODIN();
		} else return 1.0;
	}
}
