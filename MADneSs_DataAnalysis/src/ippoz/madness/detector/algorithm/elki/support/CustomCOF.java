/**
 * 
 */
package ippoz.madness.detector.algorithm.elki.support;

import ippoz.madness.detector.algorithm.elki.ELKIAlgorithm;

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
import de.lmu.ifi.dbs.elki.algorithm.outlier.lof.COF;
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
public class CustomCOF extends AbstractDistanceBasedAlgorithm<NumberVector, OutlierResult> implements OutlierAlgorithm, ELKIAlgorithm<NumberVector> {
	
		  /**
		   * The logger for this class.
		   */
		  private static final Logging LOG = Logging.getLogger(COF.class);

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
		  public CustomCOF(int k, DistanceFunction<? super NumberVector> distanceFunction) {
		    super(distanceFunction);
		    this.k = k + 1; // + query point
		  }

		  /**
		   * Runs the COF algorithm on the given database.
		   *
		   * @param database Database to query
		   * @param relation Data to process
		   * @return COF outlier result
		   */
		  public OutlierResult run(Database database, Relation<NumberVector> relation) {
		    //StepProgress stepprog = LOG.isVerbose() ? new StepProgress("COF", 3) : null;
		    DistanceQuery<NumberVector> dq = database.getDistanceQuery(relation, getDistanceFunction());
		    //LOG.beginStep(stepprog, 1, "Materializing COF neighborhoods.");
		    KNNQuery<NumberVector> knnq = DatabaseUtil.precomputedKNNQuery(database, relation, dq, k);
		    DBIDs ids = relation.getDBIDs();

		    //LOG.beginStep(stepprog, 2, "Computing Average Chaining Distances.");
		    WritableDoubleDataStore acds = DataStoreUtil.makeDoubleStorage(ids, DataStoreFactory.HINT_HOT | DataStoreFactory.HINT_TEMP);
		    computeAverageChainingDistances(knnq, dq, ids, acds);

		    // compute COF_SCORE of each db object
		    //LOG.beginStep(stepprog, 3, "Computing Connectivity-based Outlier Factors.");
		    WritableDoubleDataStore cofs = DataStoreUtil.makeDoubleStorage(ids, DataStoreFactory.HINT_HOT | DataStoreFactory.HINT_DB);
		    // track the maximum value for normalization.
		    DoubleMinMax cofminmax = new DoubleMinMax();
		    computeCOFScores(database, knnq, ids, acds, cofs, cofminmax);

		    //LOG.setCompleted(stepprog);

		    // Build result representation.
		    DoubleRelation scoreResult = new MaterializedDoubleRelation("Connectivity-Based Outlier Factor", "cof-outlier", cofs, ids);
		    OutlierScoreMeta scoreMeta = new QuotientOutlierScoreMeta(cofminmax.getMin(), cofminmax.getMax(), 0.0, Double.POSITIVE_INFINITY, 1.0);
		    return new OutlierResult(scoreMeta, scoreResult);
		  }

		  /**
		   * Computes the average chaining distance, the average length of a path
		   * through the given set of points to each target. The authors of COF decided
		   * to approximate this value using a weighted mean that assumes every object
		   * is reached from the previous point (but actually every point could be best
		   * reachable from the first, in which case this does not make much sense.)
		   *
		   * TODO: can we accelerate this by using the kNN of the neighbors?
		   *
		   * @param knnq KNN query
		   * @param dq Distance query
		   * @param ids IDs to process
		   * @param acds Storage for average chaining distances
		   */
		  protected void computeAverageChainingDistances(KNNQuery<NumberVector> knnq, DistanceQuery<NumberVector> dq, DBIDs ids, WritableDoubleDataStore acds) {
		    //FiniteProgress lrdsProgress = LOG.isVerbose() ? new FiniteProgress("Computing average chaining distances", ids.size(), LOG) : null;

		    // Compute the chaining distances.
		    // We do <i>not</i> bother to materialize the chaining order.
		    for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
		      final KNNList neighbors = knnq.getKNNForDBID(iter, k);
		      final int r = neighbors.size();
		      DoubleDBIDListIter it1 = neighbors.iter(), it2 = neighbors.iter();
		      // Store the current lowest reachability.
		      final double[] mindists = new double[r];
		      for(int i = 0; it1.valid(); it1.advance(), ++i) {
		        mindists[i] = DBIDUtil.equal(it1, iter) ? Double.NaN : it1.doubleValue();
		      }

		      double acsum = 0.;
		      for(int j = ((r < k) ? r : k) - 1; j > 0; --j) {
		        // Find the minimum:
		        int minpos = -1;
		        double mindist = Double.NaN;
		        for(int i = 0; i < mindists.length; ++i) {
		          double curdist = mindists[i];
		          // Both values could be NaN, deliberately.
		          if(curdist == curdist && !(curdist > mindist)) {
		            minpos = i;
		            mindist = curdist;
		          }
		        }
		        acsum += mindist * j; // Weighted sum, decreasing weights
		        mindists[minpos] = Double.NaN;
		        it1.seek(minpos);
		        // Update distances
		        it2.seek(0);
		        for(int i = 0; it2.valid(); it2.advance(), ++i) {
		          final double curdist = mindists[i];
		          if(curdist != curdist) {
		            continue; // NaN = processed!
		          }
		          double newdist = dq.distance(it1, it2);
		          if(newdist < curdist) {
		            mindists[i] = newdist;
		          }
		        }
		      }
		      acds.putDouble(iter, acsum / (r * 0.5 * (r - 1.)));
		      //LOG.incrementProcessed(lrdsProgress);
		    }
		    //LOG.ensureCompleted(lrdsProgress);
		  }
		  
		  private double singleACDS(List<KNNValue> neighbors, NumberVector newInstance) {
			  	  DistanceQuery<NumberVector> sq = getDistanceFunction().instantiate(null);
			      final int r = neighbors.size();
			      // DoubleDBIDListIter it1 = neighbors.iter(), it2 = neighbors.iter();
			      
			      // Store the current lowest reachability.
			      // OFScore ofs;
			      final double[] mindists = new double[r];
			      boolean allNAN = true;
			      for(int i = 0; i < neighbors.size(); ++i) {
			    	 mindists[i] = newInstance.equals(resList.get(neighbors.get(i).getIndex()).getVector()) ? Double.NaN : neighbors.get(i).getScore();
			    	 if(Double.isFinite(mindists[i]))
			    		 allNAN = false;
			      }
			      if(allNAN)
			    	  return Double.NaN;

			      double acsum = 0.;
			      for(int j = ((r < k) ? r : k) - 1; j > 0; --j) {
			        // Find the minimum:
			        int minpos = -1;
			        double mindist = Double.NaN;
			        for(int i = 0; i < mindists.length; ++i) {
			          double curdist = mindists[i];
			          // Both values could be NaN, deliberately.
			          if(curdist == curdist && !(curdist > mindist)) {
			            minpos = i;
			            mindist = curdist;
			          }
			        }
			        acsum += mindist * j; // Weighted sum, decreasing weights
			        if(minpos >= 0)
			        	mindists[minpos] = Double.NaN;
			        else {
			        	return Double.NaN;
			        }

			        for(int i = 0; i < neighbors.size(); ++i) {
			          final double curdist = mindists[i];
			          if(curdist != curdist) {
			            continue; // NaN = processed!
			          }
			          double newdist = getSimilarity(sq, resList.get(neighbors.get(minpos).getIndex()).getVector(), resList.get(neighbors.get(i).getIndex()).getVector());
			          if(newdist < curdist) {
			            mindists[i] = newdist;
			          }
			        }
			      }
			      return acsum / (r * 0.5 * (r - 1.));
			}

		  /**
		   * Compute Connectivity outlier factors.
		   *
		   * @param knnq KNN query
		   * @param ids IDs to process
		   * @param acds Average chaining distances
		   * @param cofs Connectivity outlier factor storage
		   * @param cofminmax Score minimum/maximum tracker
		   */
		  private void computeCOFScores(Database database, KNNQuery<NumberVector> knnq, DBIDs ids, DoubleDataStore acds, WritableDoubleDataStore cofs, DoubleMinMax cofminmax) {
		    //FiniteProgress progressCOFs = LOG.isVerbose() ? new FiniteProgress("COF for objects", ids.size(), LOG) : null;
		    resList = new ArrayList<OFScore>(ids.size());
		    for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
		      double cof = computeCOFScore(knnq, iter, acds);
		      resList.add(new OFScore(database.getBundle(iter), cof, acds.doubleValue(iter)));
		      cofs.putDouble(iter, cof);
		      // update minimum and maximum
		      cofminmax.put(cof);

		      //LOG.incrementProcessed(progressCOFs);
		    }
		    //LOG.ensureCompleted(progressCOFs);
		  }
		  
		  private double computeCOFScore(KNNQuery<NumberVector> knnq, DBIDIter iter, DoubleDataStore acds) {
			  final KNNList neighbors = knnq.getKNNForDBID(iter, k);
		      // Aggregate the average chaining distances of all neighbors:
		      double sum = 0.;
		      for(DBIDIter neighbor = neighbors.iter(); neighbor.valid(); neighbor.advance()) {
		        // skip the point itself
		        if(DBIDUtil.equal(neighbor, iter)) {
		          continue;
		        }
		        sum += acds.doubleValue(neighbor);
		      }
		      return (sum > 0.) ? (acds.doubleValue(iter) * k / sum) : (acds.doubleValue(iter) > 0. ? Double.POSITIVE_INFINITY : 1.); 
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
				    OFScore ofs;
					for(int i=0;i<k;i++){
						if(i < nn.size()){
							ofs = resList.get(nn.get(i).getIndex()); 
							if(ofs.getVector() != newInstance) {
				  				sum += ofs.getACDS();
			      			}
						}
				    }
					double acds = singleACDS(nn, newInstance);
					if(sum > 0){
						return acds * k / sum;
					} else {
						if(acds > 0)
							return Double.POSITIVE_INFINITY;
						else return 1.0;
					}
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
		  
		  private class OFScore implements Comparable<OFScore> {

				private NumberVector data;

				private double of;
				
				private double acds;

				public OFScore(SingleObjectBundle bundle, double of, double acds) {
					this.of = of;
					this.acds = acds;
					double[] bValues = ((DoubleVector)bundle.data(1)).getValues();
					data = new Vector(bValues.length);
					for(int i=0;i<data.getDimensionality();i++){
						((Vector)data).set(i, bValues[i]);
					}
				}
				
				public OFScore(String vString, String acds, String of) {
					this.of = Double.parseDouble(of);
					this.acds = Double.parseDouble(acds);
					String[] splitted = vString.split(",");
					data = new Vector(splitted.length);
					for(int i=0;i<data.getDimensionality();i++){
						((Vector)data).set(i, Double.parseDouble(splitted[i].trim()));
					}
				}
				
				public double getOF() {
					return of;
				}
				
				public double getACDS() {
					return acds;
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
								resList.add(new OFScore(readed.split(";")[0], readed.split(";")[1], readed.split(";")[2]));
							}
						}
						reader.close();
					}
				} catch (IOException ex) {
					//AppLogger.logException(getClass(), ex, "Unable to read ABOD file");
				} 
			}

			public void printFile(File file) {
				BufferedWriter writer;
				try {
					if(resList != null && resList.size() > 0){
						if(file.exists())
							file.delete();
						writer = new BufferedWriter(new FileWriter(file));
						writer.write("data;acds;cof\n");
						for(OFScore ar : resList){
							writer.write(ar.getVector().toString() + ";" + ar.getACDS() + ";" + ar.getOF()+ "\n");
						}
						writer.close();
					}
				} catch (IOException ex) {
					//AppLogger.logException(getClass(), ex, "Unable to write ABOD file");
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
			public List<Double> getScoresList() {
				ArrayList<Double> list = new ArrayList<Double>(size());
				for(OFScore of : resList){
					list.add(of.getOF());
				}
				Collections.sort(list);
				return list;
			}

			@Override
			public String getAlgorithmName() {
				return "cof";
			}

}

