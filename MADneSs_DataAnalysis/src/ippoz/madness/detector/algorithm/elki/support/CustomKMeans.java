/**
 * 
 */
package ippoz.madness.detector.algorithm.elki.support;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.AbstractKMeans;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.KMeansInitialization;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.WritableIntegerDataStore;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.ids.ModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.NumberVectorDistanceFunction;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.logging.progress.IndefiniteProgress;
import de.lmu.ifi.dbs.elki.logging.statistics.DoubleStatistic;
import de.lmu.ifi.dbs.elki.logging.statistics.LongStatistic;
import de.lmu.ifi.dbs.elki.logging.statistics.StringStatistic;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public class CustomKMeans<V extends NumberVector> extends AbstractKMeans<V, KMeansModel> {
	  /**
	   * The logger for this class.
	   */
	  private static final Logging LOG = Logging.getLogger(KMeansLloyd.class);

	  /**
	   * Key for statistics logging.
	   */
	  private static final String KEY = KMeansLloyd.class.getName();
	  
	  private List<KMeansModel> finalClusters;
	  
	  /**
	   * Constructor.
	   *
	   * @param distanceFunction distance function
	   * @param k k parameter
	   * @param maxiter Maxiter parameter
	   * @param initializer Initialization method
	   */
	  public CustomKMeans(NumberVectorDistanceFunction<? super V> distanceFunction, int k, int maxiter, KMeansInitialization<? super V> initializer, List<KMeansModel> finalClusters) {
	    super(distanceFunction, k, maxiter, initializer);
	    this.finalClusters = finalClusters;
	  }
	  
	  public boolean hasClusters(){
		  return finalClusters != null && finalClusters.size() > 0;
	  }
	  
	  public KMeansModel getCluster(int index){
		  return finalClusters.get(index);
	  }

	  @Override
	  public Clustering<KMeansModel> run(Database database, Relation<V> relation) {
	    if(relation.size() <= 0) {
	      return new Clustering<>("k-Means Clustering", "kmeans-clustering");
	    }
	    // Choose initial means
	    if(LOG.isStatistics()) {
	      LOG.statistics(new StringStatistic(KEY + ".initialization", initializer.toString()));
	    }
	    List<Vector> means = initializer.chooseInitialMeans(database, relation, k, getDistanceFunction(), Vector.FACTORY);
	    // Setup cluster assignment store
	    List<ModifiableDBIDs> clusters = new ArrayList<>();
	    for(int i = 0; i < k; i++) {
	      clusters.add(DBIDUtil.newHashSet((int) (relation.size() * 2. / k)));
	    }
	    WritableIntegerDataStore assignment = DataStoreUtil.makeIntegerStorage(relation.getDBIDs(), DataStoreFactory.HINT_TEMP | DataStoreFactory.HINT_HOT, -1);
	    double[] varsum = new double[k];

	    IndefiniteProgress prog = LOG.isVerbose() ? new IndefiniteProgress("K-Means iteration", LOG) : null;
	    DoubleStatistic varstat = LOG.isStatistics() ? new DoubleStatistic(this.getClass().getName() + ".variance-sum") : null;
	    int iteration = 0;
	    for(; maxiter <= 0 || iteration < maxiter; iteration++) {
	      LOG.incrementProcessed(prog);
	      boolean changed = assignToNearestCluster(relation, means, clusters, assignment, varsum);
	      logVarstat(varstat, varsum);
	      // Stop if no cluster assignment changed.
	      if(!changed) {
	        break;
	      }
	      // Recompute means.
	      means = means(clusters, means, relation);
	    }
	    LOG.setCompleted(prog);
	    if(LOG.isStatistics()) {
	      LOG.statistics(new LongStatistic(KEY + ".iterations", iteration));
	    }

	    // Wrap result
	    Clustering<KMeansModel> result = new Clustering<>("k-Means Clustering", "kmeans-clustering");
	    finalClusters = new ArrayList<KMeansModel>(clusters.size());
	    for(int i = 0; i < clusters.size(); i++) {
	      DBIDs ids = clusters.get(i);
	      if(ids.size() == 0) {
	        continue;
	      }
	      KMeansModel model = new KMeansModel(means.get(i), varsum[i]);
	      result.addToplevelCluster(new Cluster<>(ids, model));
	      finalClusters.add(model);
	    }
	    return result;
	  }

	  @Override
	  protected Logging getLogger() {
		  return LOG;
	  }
	  
	  public String clustersToString() {
		  String outString = "";
		  for(KMeansModel kmm : finalClusters){
			  outString = outString + kmm.getVarianceContribution() + "#";
			  for(int i=0;i<kmm.getMean().getDimensionality();i++){
				  outString = outString + kmm.getMean().get(i) + ",";
			  }
			  outString = outString.substring(0, outString.length()-1) + "@";
		  }
		  outString = outString.substring(0, outString.length()-1);
		  return outString;
	  }
	  
	  public static List<KMeansModel> loadClusters(String cString) {
		  Vector vec;
		  String[] splitted;
		  String[] sSplitted;
		  List<KMeansModel> list = null;
		  if(cString != null && cString.trim().length() > 0){
			  splitted = cString.split("@");
			  list = new ArrayList<KMeansModel>(splitted.length);
			  for(String sString : splitted){
				  sSplitted = sString.split("#")[1].split(",");
				  vec = new Vector(sSplitted.length);
				  for(int i=0; i<sSplitted.length;i++){
					  vec.set(i, Double.parseDouble(sSplitted[i].trim()));
				  }
				  list.add(new KMeansModel(vec, Double.parseDouble(sString.split("#")[0])));
			  }
		  }
		  return list;
	  }

	  /**
	   * Parameterization class.
	   *
	   * @author Erich Schubert
	   *
	   * @apiviz.exclude
	   */
	  public static class Parameterizer<V extends NumberVector> extends AbstractKMeans.Parameterizer<V> {
	    @Override
	    protected Logging getLogger() {
	      return LOG;
	    }

	    @Override
	    protected KMeansLloyd<V> makeInstance() {
	      return new KMeansLloyd<>(distanceFunction, k, maxiter, initializer);
	    }
	  }

	public List<KMeansModel> getClusters() {
		return finalClusters;
	}
}