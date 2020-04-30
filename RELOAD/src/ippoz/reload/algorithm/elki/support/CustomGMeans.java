/**
 * 
 */
package ippoz.reload.algorithm.elki.support;

import ippoz.reload.algorithm.elki.ELKIAlgorithm;
import ippoz.reload.algorithm.elki.support.CustomKMeans.KMeansScore;
import ippoz.reload.algorithm.support.GenericCluster;
import ippoz.reload.commons.support.AppLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.AbstractKMeans;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.KMeansInitialization;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.WritableIntegerDataStore;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.ids.ModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.bundle.SingleObjectBundle;
import de.lmu.ifi.dbs.elki.distance.distancefunction.NumberVectorDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * 
 * This file is part of RELOAD but it was partially inherited by ELKI, and updated under AGPLv3 License.
 * 
 * Changes regard its new inheritance to ELKIAlgorithm<V>, which is used by RELOAD to provide 
 * a common layer of functionalities that are shared among algorithms inherited by ELKI.
 * 
 * 
 * Added on: Fall 2019
 * 
 */

public class CustomGMeans<V extends NumberVector> extends AbstractKMeans<V, KMeansModel> implements ELKIAlgorithm<V> {
	  /**
	   * The logger for this class.
	   */
	  private static final Logging LOG = Logging.getLogger(KMeansLloyd.class);
	  
	  private List<GenericCluster> finalClusters;
	  
	  private List<KMeansScore> scoresList;
	  
	  private double max_k;
	  
	  /**
	   * Constructor.
	   *
	   * @param distanceFunction distance function
	   * @param k k parameter
	   * @param maxiter Maxiter parameter
	   * @param initializer Initialization method
	   */
	  public CustomGMeans(NumberVectorDistanceFunction<? super V> distanceFunction, int max_k, int maxiter, KMeansInitialization<? super V> initializer, List<GenericCluster> finalClusters) {
	    super(distanceFunction, 1, maxiter, initializer);
	    this.finalClusters = finalClusters;
	    this.max_k = max_k;
	  }
	  
	  public boolean hasClusters(){
		  return finalClusters != null && finalClusters.size() > 0;
	  }
	  
	  public GenericCluster getCluster(int index){
		  return finalClusters.get(index);
	  }
	  
	  @Override
	  public Clustering<KMeansModel> run(Database database, Relation<V> relation) {
	    boolean processContinue = true;
	    List<GenericCluster> clusterList = null;
		  
		if(relation.size() <= 0) {
	      return new Clustering<>("k-Means Clustering", "kmeans-clustering");
	    }
		
	    k = 1;
	    while(processContinue && k <= max_k){
	    	clusterList = runKMeans(database, relation, k);
	    	if(clusterList.size() < k)
	    		break;
	    	processContinue = false;
	    	for(GenericCluster cluster : clusterList){
	    		if(!isDistributedEvenly(cluster)){
	    			k++;
	    			processContinue = true;
	    		}
	    	}
	    }
	    
	    finalClusters = runKMeans(database, relation, k);
	    
	    // Calculating Distances
	    scoresList = new LinkedList<KMeansScore>();
	    DBIDs ids = relation.getDBIDs();
	    for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
	    	scoresList.add(getMinimumClustersDistance(extractVector(database.getBundle(iter))));
	    }
		Collections.sort(scoresList);
	    
	    return new Clustering<>("k-Means Clustering", "kmeans-clustering");
	  }

	  private boolean isDistributedEvenly(GenericCluster cluster) {
		double cv = Math.sqrt(cluster.getVar()) / cluster.getAvgDistanceFromCenter();
		if(cluster.getVar() == 0)
			return true;
		return cv <= 2;
	}

	
	  public List<GenericCluster> runKMeans(Database database, Relation<V> relation, int kValue) {
		List<GenericCluster> clusterList;
	    List<Vector> means = initializer.chooseInitialMeans(database, relation, k, getDistanceFunction(), Vector.FACTORY);
	    // Setup cluster assignment store
	    List<ModifiableDBIDs> clusters = new ArrayList<>();
	    for(int i = 0; i < kValue; i++) {
	      clusters.add(DBIDUtil.newHashSet((int) (relation.size() * 2. / k)));
	    }
	    WritableIntegerDataStore assignment = DataStoreUtil.makeIntegerStorage(relation.getDBIDs(), DataStoreFactory.HINT_TEMP | DataStoreFactory.HINT_HOT, -1);
	    double[] varsum = new double[kValue];

	    //IndefiniteProgress prog = LOG.isVerbose() ? new IndefiniteProgress("K-Means iteration", LOG) : null;
	    //DoubleStatistic varstat = LOG.isStatistics() ? new DoubleStatistic(this.getClass().getName() + ".variance-sum") : null;
	    int iteration = 0;
	    for(; maxiter <= 0 || iteration < maxiter; iteration++) {
	      //LOG.incrementProcessed(prog);
	      boolean changed = assignToNearestCluster(relation, means, clusters, assignment, varsum);
	      //logVarstat(varstat, varsum);
	      // Stop if no cluster assignment changed.
	      if(!changed) {
	        break;
	      }
	      // Recompute means.
	      means = means(clusters, means, relation);
	    }

	    // Wrap result
	    Clustering<KMeansModel> result = new Clustering<>("k-Means Clustering", "kmeans-clustering");
	    clusterList = new ArrayList<GenericCluster>(clusters.size());
	    for(int i = 0; i < clusters.size(); i++) {
	      DBIDs ids = clusters.get(i);
	      if(ids.size() == 0) {
	        continue;
	      }
	      KMeansModel model = new KMeansModel(means.get(i), varsum[i]);
	      result.addToplevelCluster(new Cluster<>(ids, model));
	      List<double[]> points = new LinkedList<double[]>();
	      for(DBIDIter it = ids.iter(); it.valid(); it.advance()) {
	    	  	points.add(((DoubleVector)(database.getBundle(it).data(1))).getValues());
	      }
	      double[] avg = new double[means.get(i).getDimensionality()];
	      for(int j=0;j<means.get(i).getDimensionality();j++){
	    	  avg[j] =means.get(i).doubleValue(j);
	      }
	      clusterList.add(new GenericCluster(avg, varsum[i], points));
	    }
	    
	    return clusterList;
	  }
	  
	  public Vector extractVector(SingleObjectBundle bundle){
			double[] bValues = ((DoubleVector)bundle.data(1)).getValues();
			Vector data = new Vector(bValues.length);
			for(int i=0;i<data.getDimensionality();i++){
				((Vector)data).set(i, bValues[i]);
			}
			return data;
	  }
	  
	  public KMeansScore getMinimumClustersDistance(Vector newData){
		  double partial;
		  double minValue = Double.MAX_VALUE;
		  KMeansScore kms = new KMeansScore(newData);
		  KMeansModel minKmm = null;
		  if(finalClusters != null && finalClusters.size() > 0){
			for(GenericCluster km : finalClusters){
				KMeansModel kmm = km.generateKMeansModel();
				partial = Math.abs(SquaredEuclideanDistanceFunction.STATIC.minDist(newData, kmm.getMean()));
				if(partial < minValue){
					minValue = partial;
					minKmm = kmm;
				}
			}
			kms.addClusterDistance(minValue, minKmm);
		  }
		  
		  return kms;
	  }

	  @Override
	  protected Logging getLogger() {
		  return LOG;
	  }
	  
	  public String clustersToString() {
		  String outString = "";
		  for(GenericCluster km : finalClusters){
			  KMeansModel kmm = km.generateKMeansModel();
			  outString = outString + kmm.getVarianceContribution() + "#";
			  for(int i=0;i<kmm.getMean().getDimensionality();i++){
				  outString = outString + kmm.getMean().get(i) + ",";
			  }
			  outString = outString.substring(0, outString.length()-1) + "@";
		  }
		  outString = outString.substring(0, outString.length()-1);
		  return outString;
	  }
	  
	  public static List<KMeansModel> loadClustersOLD(String cString) {
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

	public List<GenericCluster> getClusters() {
		return finalClusters;
	}

	@Override
	public void loadFile(String filename) {
		loadClustersFile(new File(filename));
		loadScoresFile(new File(filename + "scores"));		
	}
	
	private void loadScoresFile(File file) {
		BufferedReader reader;
		String readed;
		try {
			if(file.exists()){
				scoresList = new LinkedList<KMeansScore>();
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && readed.split(";").length >= 3)
							scoresList.add(new KMeansScore(readed.split(";")[0], readed.split(";")[2], Double.parseDouble(readed.split(";")[3])));
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read KMeans Scores file");
		} 
	}
	
	private void loadClustersFile(File file){
		BufferedReader reader;
		String readed;
		double[] vec;
		finalClusters = new LinkedList<GenericCluster>();
		try {
			if(file.exists()){
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						String[] sSplitted = readed.split("#")[1].split(",");
						vec = new double[sSplitted.length];
						for(int i=0; i<sSplitted.length;i++){
							vec[i] = Double.parseDouble(sSplitted[i].trim());
						}
						finalClusters.add(new GenericCluster(vec, Double.parseDouble(readed.split("#")[0]), Double.parseDouble(readed.split("#")[2]), Integer.parseInt(readed.split("#")[3]), Boolean.getBoolean(readed.split("#")[4])));
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read LOF file");
		} 
	}

	@Override
	public List<Double> getScoresList() {
		List<Double> scores = new ArrayList<Double>(scoresList.size());
		for(KMeansScore score : scoresList){
			scores.add(score.getDistance());
		}
		Collections.sort(scores);
		return scores;
	}

	@Override
	public String getAlgorithmName() {
		return "kmeans";
	}

	@Override
	public void printFile(File file) {
		printClusters(file);
		printScores(new File(file.getPath() + "scores"));
	}
	
	private void printClusters(File file){
		BufferedWriter writer;
		try {
			if(finalClusters != null && finalClusters.size() > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("variance_contribution#cluster#avg_distance_from_center#n_data_points#is_Large\n");
				for(GenericCluster km : finalClusters){
					KMeansModel kmm = km.generateKMeansModel();
					String outString = kmm.getVarianceContribution() + "#";
					for(int i=0;i<kmm.getMean().getDimensionality();i++){
						outString = outString + kmm.getMean().get(i) + ",";
					}
					outString = outString + "#" + km.getAvgDistanceFromCenter() 
							+ "#" + km.getSize() + "#" + km.isLarge();
					writer.write(outString + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write KMEANS clusters file");
		} 
	}
	
	private void printScores(File file){
		BufferedWriter writer;
		String clusterString;
		try {
			if(finalClusters != null && finalClusters.size() > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("vector, k, nearest_cluster(variance#means), kmeans_score\n");
				for(KMeansScore score : scoresList){
					String vectorString = "{";
					for(int i=0;i<score.getVector().getDimensionality();i++){
						vectorString = vectorString + score.getVector().get(i) + ",";
					}
					vectorString = vectorString.substring(0, vectorString.length()-1) + "}";
					if(score.getCluster() != null){
						clusterString = score.getCluster().getVarianceContribution() + "#";
						for(int i=0;i<score.getCluster().getMean().getDimensionality();i++){
							clusterString = clusterString + score.getCluster().getMean().get(i) + ",";
						}
						clusterString = clusterString.substring(0, clusterString.length()-1);
					} else clusterString = null;
					writer.write(vectorString + ";" + k + ";" + clusterString + ";" + score.getDistance() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write KMEANS scores file");
		} 
	}
	
}