/**
 * 
 */
package ippoz.madness.detector.algorithm.elki.support;

import ippoz.madness.detector.algorithm.elki.ELKIAlgorithm;
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
import de.lmu.ifi.dbs.elki.algorithm.outlier.lof.LOF;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.DoubleDataStore;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDoubleDataStore;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDListIter;
import de.lmu.ifi.dbs.elki.database.ids.KNNList;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.relation.DoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.MaterializedDoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.bundle.SingleObjectBundle;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.math.DoubleMinMax;
import de.lmu.ifi.dbs.elki.math.MathUtil;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierScoreMeta;
import de.lmu.ifi.dbs.elki.result.outlier.QuotientOutlierScoreMeta;
import de.lmu.ifi.dbs.elki.utilities.DatabaseUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;

/**
 * @author Tommy
 *
 */
public class CustomLOF extends AbstractDistanceBasedAlgorithm<NumberVector, OutlierResult> implements OutlierAlgorithm, ELKIAlgorithm<NumberVector> {
	  /**
	   * The logger for this class.
	   */
	  private static final Logging LOG = Logging.getLogger(LOF.class);

	  /**
	   * The number of neighbors to query (including the query point!)
	   */
	  protected int k;
	  
	  private List<OFScore> resList;

	  /**
	   * Constructor.
	   * 
	   * @param k the number of neighbors to use for comparison (excluding the query
	   *        point)
	   * @param distanceFunction the neighborhood distance function
	   */
	  public CustomLOF(int k, DistanceFunction<? super NumberVector> distanceFunction) {
	    super(distanceFunction);
	    this.k = k + 1; // + query point
	  }
	  
	  /**
	   * Runs the LOF algorithm on the given database.
	   * 
	   * @param database Database to query
	   * @param relation Data to process
	   * @return LOF outlier result
	   */
	  @Override
	  public OutlierResult run(Database database, Relation<NumberVector> relation) {
	    //StepProgress stepprog = LOG.isVerbose() ? new StepProgress("LOF", 3) : null;
	    DBIDs ids = relation.getDBIDs();

	    //LOG.beginStep(stepprog, 1, "Materializing nearest-neighbor sets.");
	    KNNQuery<NumberVector> knnq = DatabaseUtil.precomputedKNNQuery(database, relation, getDistanceFunction(), k);

	    // Compute LRDs
	    //LOG.beginStep(stepprog, 2, "Computing Local Reachability Densities (LRD).");
	    WritableDoubleDataStore lrds = DataStoreUtil.makeDoubleStorage(ids, DataStoreFactory.HINT_HOT | DataStoreFactory.HINT_TEMP);
	    computeLRDs(database, knnq, ids, lrds);

	    // compute LOF_SCORE of each db object
	    //LOG.beginStep(stepprog, 3, "Computing Local Outlier Factors (LOF).");
	    WritableDoubleDataStore lofs = DataStoreUtil.makeDoubleStorage(ids, DataStoreFactory.HINT_HOT | DataStoreFactory.HINT_DB);
	    // track the maximum value for normalization.
	    DoubleMinMax lofminmax = new DoubleMinMax();
	    computeLOFScores(database, knnq, ids, lrds, lofs, lofminmax);

	    //LOG.setCompleted(stepprog);

	    // Build result representation.
	    DoubleRelation scoreResult = new MaterializedDoubleRelation("Local Outlier Factor", "lof-outlier", lofs, ids);
	    OutlierScoreMeta scoreMeta = new QuotientOutlierScoreMeta(lofminmax.getMin(), lofminmax.getMax(), 0.0, Double.POSITIVE_INFINITY, 1.0);
	    return new OutlierResult(scoreMeta, scoreResult);
	  }

	  /**
	   * Compute local reachability distances.
	   * 
	   * @param knnq KNN query
	   * @param ids IDs to process
	   * @param lrds Reachability storage
	   */
	  private void computeLRDs(Database db, KNNQuery<NumberVector> knnq, DBIDs ids, WritableDoubleDataStore lrds) {
	    //FiniteProgress lrdsProgress = LOG.isVerbose() ? new FiniteProgress("Local Reachability Densities (LRD)", ids.size(), LOG) : null;
	    double lrd;
	    for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
	      lrd = computeLRD(db, knnq, iter);
	      lrds.putDouble(iter, lrd);
	      //LOG.incrementProcessed(lrdsProgress);
	    }
	    //LOG.ensureCompleted(lrdsProgress);
	  }

	  /**
	   * Compute a single local reachability distance.
	   * 
	   * @param knnq kNN Query
	   * @param curr Current object
	   * @return Local Reachability Density
	   */
	  protected double computeLRD(Database db, KNNQuery<NumberVector> knnq, DBIDIter curr) {
	    final KNNList neighbors = knnq.getKNNForDBID(curr, k);
	    double sum = 0.0;
	    int count = 0;
	    for(DoubleDBIDListIter neighbor = neighbors.iter(); neighbor.valid(); neighbor.advance()) {
	      if(DBIDUtil.equal(curr, neighbor)) {
	        continue;
	      }
	      //System.out.println(neighbor.doubleValue());
	      KNNList neighborsNeighbors = knnq.getKNNForDBID(neighbor, k);
	      sum += MathUtil.max(neighbor.doubleValue(), neighborsNeighbors.getKNNDistance());
	      count++;
	    }
	    // Avoid division by 0
	    return (sum > 0) ? (count / sum) : Double.POSITIVE_INFINITY;
	  }
			  
	  
	  protected double singleLRD(List<KNNValue> nn, NumberVector newInstance) {
	    double sum = 0.0;
	    int count = 0;
	    OFScore ofs;
	    for(int i=0;i<k-1;i++) {
	    	ofs = resList.get(nn.get(i).getIndex());
	    	if(!newInstance.equals(ofs.getVector())) {
	    	  List<KNNValue> nnList = getKNNs(ofs.getVector(), false);
	    	  double nd = nn.get(i).getScore();
	    	  double nnd = (nnList.size() >= k-1) ? nnList.get(k -2).getScore() : Double.POSITIVE_INFINITY;
	    	  sum += MathUtil.max(nd, nnd);
		      count++;
	      }
	    }
	    return (sum > 0) ? (count / sum) : Double.POSITIVE_INFINITY;
	  }

	  /**
	   * Compute local outlier factors.
	 * @param database 
	   * 
	   * @param knnq KNN query
	   * @param ids IDs to process
	   * @param lrds Local reachability distances
	   * @param lofs Local outlier factor storage
	   * @param lofminmax Score minimum/maximum tracker
	   */
	  private void computeLOFScores(Database database, KNNQuery<NumberVector> knnq, DBIDs ids, DoubleDataStore lrds, WritableDoubleDataStore lofs, DoubleMinMax lofminmax) {
	    //FiniteProgress progressLOFs = LOG.isVerbose() ? new FiniteProgress("Local Outlier Factor (LOF) scores", ids.size(), LOG) : null;
	    double lof;
	    resList = new ArrayList<OFScore>(ids.size());
	    for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
	      lof = computeLOFScore(knnq, iter, lrds);
	      resList.add(new OFScore(database.getBundle(iter), lof, lrds.doubleValue(iter)));
	      lofs.putDouble(iter, lof);
	      // update minimum and maximum
	      lofminmax.put(lof);
	      //LOG.incrementProcessed(progressLOFs);
	    }
	    Collections.sort(resList);
	    //LOG.ensureCompleted(progressLOFs);
	  }

	  /**
	   * Compute a single LOF score.
	   * 
	   * @param knnq kNN query
	   * @param cur Current object
	   * @param lrds Stored reachability densities
	   * @return LOF score.
	   */
	  protected double computeLOFScore(KNNQuery<NumberVector> knnq, DBIDRef cur, DoubleDataStore lrds) {
	    final double lrdp = lrds.doubleValue(cur);
	    if(Double.isInfinite(lrdp)) {
	      return 1.0;
	    }
	    double sum = 0.;
	    int count = 0;
	    final KNNList neighbors = knnq.getKNNForDBID(cur, k);
	    for(DBIDIter neighbor = neighbors.iter(); neighbor.valid(); neighbor.advance()) {
	      // skip the point itself
	      if(DBIDUtil.equal(cur, neighbor)) {
	        continue;
	      }
	      sum += lrds.doubleValue(neighbor);
	      ++count;
	    }
	    return sum / (lrdp * count);
	  }
	  
	  public double calculateSingleOF(NumberVector newInstance) {
		double partialResult;
		if(resList == null || resList.size() == 0) 
			return Double.MAX_VALUE;
		else if(!Double.isNaN(partialResult = hasResult(newInstance)))
			return partialResult;
		else {
			List<KNNValue> nn = getKNNs(newInstance, true);
			double sum = 0.;
		    int count = 0;
		    OFScore ofs;
			for(int i=0;i<k;i++){
				if(i < nn.size() && nn.get(i).getIndex() < resList.size()){
					ofs = resList.get(nn.get(i).getIndex()); 
					if(ofs.getVector() != newInstance) {
		  				sum += ofs.getLRD();
	    				++count;
	    			}
				}
		    }
		    return sum / (singleLRD(nn, newInstance)*count);
		
		}
	  }
	  
	  private List<KNNValue> getKNNs(NumberVector newInstance, boolean flag){
		  DistanceQuery<NumberVector> sq = getDistanceFunction().instantiate(null);
			List<KNNValue> nn = new LinkedList<KNNValue>();
			Map<String, Integer> nOccurrences = new HashMap<String, Integer>();
			
			for(int i=0;i<resList.size();i++) {
				double dist = getSimilarity(sq, newInstance, resList.get(i).getVector());
				if(flag){
					if(!nOccurrences.containsKey(resList.get(i).getVector().toString())){
						nn.add(new KNNValue(dist, i));
						nOccurrences.put(resList.get(i).getVector().toString(), 1);
					} else if(nOccurrences.get(resList.get(i).getVector().toString()) < k - 1){	
						nn.add(new KNNValue(dist, i));
						nOccurrences.put(resList.get(i).getVector().toString(), nOccurrences.get(resList.get(i).getVector().toString()) + 1);
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
			for(OFScore ar : resList){
				if(ar.getVector().equals(newInstance))
					return ar.getOF();
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
	  
	  public List<Double> getScoresList(){
			ArrayList<Double> list = new ArrayList<Double>(size());
			for(OFScore of : resList){
				list.add(of.getOF());
			}
			Collections.sort(list);
			return list;
		}
	  
	  private class OFScore implements Comparable<OFScore> {

			private NumberVector data;

			private double of;
			
			private double lrd;

			public OFScore(SingleObjectBundle bundle, double of, double lrd) {
				this.of = of;
				this.lrd = lrd;
				double[] bValues = ((DoubleVector)bundle.data(1)).getValues();
				data = new Vector(bValues.length);
				for(int i=0;i<data.getDimensionality();i++){
					((Vector)data).set(i, bValues[i]);
				}
			}
			
			public OFScore(String vString, String lrd, String of) {
				this.of = Double.parseDouble(of);
				this.lrd = Double.parseDouble(lrd);
				String[] splitted = vString.split(",");
				data = new Vector(splitted.length);
				for(int i=0;i<data.getDimensionality();i++){
					((Vector)data).set(i, Double.parseDouble(splitted[i].trim()));
				}
			}
			
			public double getOF() {
				return of;
			}
			
			public double getLRD() {
				return lrd;
			}
			
			public NumberVector getVector(){
				return data;
			}

			@Override
			public int compareTo(OFScore o) {
				return Double.compare(of, o.getOF());
			}

			@Override
			public String toString() {
				return "OFResult [data=" + data.toString() + ", of=" + of + "]";
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
	     * Parameter to specify the number of nearest neighbors of an object to be
	     * considered for computing its LOF score, must be an integer greater than
	     * or equal to 1.
	     */
	    public static final OptionID K_ID = new OptionID("lof.k", "The number of nearest neighbors (not including the query point) of an object to be considered for computing its LOF score.");

	    /**
	     * The neighborhood size to use.
	     */
	    protected int k = 2;

	    @Override
	    protected void makeOptions(Parameterization config) {
	      super.makeOptions(config);

	      final IntParameter pK = new IntParameter(K_ID) //
	      .addConstraint(CommonConstraints.GREATER_EQUAL_ONE_INT);
	      if(config.grab(pK)) {
	        k = pK.intValue();
	      }
	    }

	    @Override
	    protected LOF<O> makeInstance() {
	      return new LOF<>(k, distanceFunction);
	    }
	  }

	  public void loadFile(String item) {
			BufferedReader reader;
			String readed;
			try {
				resList = new LinkedList<OFScore>();
				if(new File(item).exists()){
					reader = new BufferedReader(new FileReader(new File(item)));
					reader.readLine();
					while(reader.ready()){
						readed = reader.readLine();
						if(readed != null){
							readed = readed.trim();
							resList.add(new OFScore(readed.split(";")[0].trim().substring(1, readed.split(";")[0].trim().length()-1), readed.split(";")[2].trim(), readed.split(";")[3].trim()));
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
					writer.write("data(vector enclosed in {});k;Local Reachability Distance (lrd);Local Outrlier Factor (lof)\n");
					for(OFScore ar : resList){
						writer.write("{" + ar.getVector().toString() + "};" + (k-1) + ";" + ar.getLRD() + ";" + ar.getOF()+ "\n");
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
				return resList.get(ratio-1).getOF();
			} else return 1.0;
		}

		@Override
		public String getAlgorithmName() {
			return "lof";
		}
}