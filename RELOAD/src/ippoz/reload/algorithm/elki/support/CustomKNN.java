/**
 * 
 */
package ippoz.reload.algorithm.elki.support;

import ippoz.reload.algorithm.elki.ELKIAlgorithm;
import ippoz.reload.algorithm.utils.KdTree;
import ippoz.reload.algorithm.utils.KdTree.ELKIEuclid;
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

import de.lmu.ifi.dbs.elki.algorithm.AbstractDistanceBasedAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.outlier.OutlierAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.outlier.distance.KNNOutlier;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.utilities.Alias;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;

/**
 * 
 * This file is part of RELOAD but it was inherited by ELKI, and updated under AGPLv3 License.
 * 
 * Changes regard its new inheritance to ELKIAlgorithm<V>, which is used by RELOAD to provide 
 * a common layer of functionalities that are shared among algorithms inherited by ELKI.
 * 
 * Methods to be overridden include:
 * loadFile(String filename);
 * public List<Double> getScoresList();
 * public String getAlgorithmName();
 * public void printFile(File file);
 * public Object run(Database db, Relation<V> relation);
 * 
 * Other functions may be added to support the functionalities above.
 * 
 * Added on: Spring 2019
 * 
 */

/**
 * Outlier Detection based on the distance of an object to its k nearest
 * neighbor.
 *
 * This implementation differs from the original pseudocode: the k nearest
 * neighbors do not exclude the point that is currently evaluated. I.e. for k=1
 * the resulting score is the distance to the 1-nearest neighbor that is not the
 * query point and therefore should match k=2 in the exact pseudocode - a value
 * of k=1 in the original code does not make sense, as the 1NN distance will be
 * 0 for every point in the database. If you for any reason want to use the
 * original algorithm, subtract 1 from the k parameter.
 *
 * Reference:
 * <p>
 * S. Ramaswamy, and R. Rastogi, and K. Shim<br />
 * Efficient Algorithms for Mining Outliers from Large Data Sets.<br />
 * In: Proc. Int. Conf. on Management of Data, 2000.
 * </p>
 *
 * @author Lisa Reichert
 * @since 0.3
 *
 * @apiviz.has KNNQuery
 *
 * @param <O> the type of DatabaseObjects handled by this Algorithm
 */
@Title("KNN outlier: Efficient Algorithms for Mining Outliers from Large Data Sets")
@Description("Outlier Detection based on the distance of an object to its k nearest neighbor.")
@Reference(authors = "S. Ramaswamy, and R. Rastogi, and K. Shim", //
title = "Efficient Algorithms for Mining Outliers from Large Data Sets", //
booktitle = "Proc. Int. Conf. on Management of Data, 2000", //
url = "http://dx.doi.org/10.1145/342009.335437")
@Alias({ "de.lmu.ifi.dbs.elki.algorithm.outlier.KNNOutlier", "knno" })


public class CustomKNN extends AbstractDistanceBasedAlgorithm<NumberVector, OutlierResult> implements OutlierAlgorithm, ELKIAlgorithm<NumberVector> {
	  /**
	   * The logger for this class.
	   */
	  private static final Logging LOG = Logging.getLogger(KNNOutlier.class);

	  /**
	   * The parameter k (including query point!)
	   */
	  private int k;
	  
	//  private List<KNNScore> resList;
	  
	  private ELKIEuclid<KNNScore> treeList;

	  /**
	   * Constructor for a single kNN query.
	   *
	   * @param distanceFunction distance function to use
	   * @param k Value of k (excluding query point!)
	   */
	  public CustomKNN(DistanceFunction<? super NumberVector> distanceFunction, int k) {
	    super(distanceFunction);
	    this.k = k + 1; // INCLUDE the query point now
	  }

	  /**
	   * Runs the algorithm in the timed evaluation part.
	   *
	   * @param relation Data relation
	   */
	  public Object run(Relation<NumberVector> relation) {
		  if(isApplicable(relation, relation.getDBIDs())){
		    final DistanceQuery<NumberVector> distanceQuery = relation.getDistanceQuery(getDistanceFunction());
		    final KNNQuery<NumberVector> knnQuery = relation.getKNNQuery(distanceQuery, k);
		    treeList = new ELKIEuclid<KNNScore>();
		    for(DBIDIter it = relation.iterDBIDs(); it.valid(); it.advance()) {
		      // distance to the kth nearest neighbor
		      // (assuming the query point is always included, with distance 0)
		      final double dkn = knnQuery.getKNNForDBID(it, k).getKNNDistance();
		      KNNScore knn = new KNNScore(relation.get(it), dkn);
		      treeList.addPoint(knn.getPoint(), knn);
		    }
		    return treeList;
		  } else return null;
	  }
	  
	  private boolean isApplicable(Relation<NumberVector> relation, DBIDs ids){
			List<Double> differentValues = new LinkedList<Double>();
			for(DBIDIter pA = ids.iter(); pA.valid(); pA.advance()) {
				NumberVector newValue = relation.get(pA);
				double newDouble = 0;
				if(newValue.getDimensionality() == 1)
					newDouble = newValue.doubleValue(0);
				else {
					newDouble = 0;
					for(int i=0;i<newValue.getDimensionality();i++){
						newDouble = newDouble + Math.pow(-1, i)*newValue.doubleValue(i)*i;
					}
				}
				if(!Double.isFinite(newDouble))
					return false;
				if(Double.isFinite(newDouble) && !differentValues.contains(newDouble)){
					differentValues.add(newDouble);
					if(differentValues.size() > k+1){
						return true;
					}
				}
			}
			return false;
			
		}
	  
	  public double calculateSingleKNN(Vector newInstance) {
		  double partialResult;
			if(treeList == null || treeList.size() == 0) 
				return Double.MAX_VALUE;
			else if(Double.isFinite(partialResult = hasResult(newInstance)))
				return partialResult;
			else {
				double[] point = new double[newInstance.getDimensionality()];
				for(int i=0;i<point.length;i++){
					point[i] = newInstance.doubleValue(i);
				}
				List<KdTree.Entry<KNNScore>> kNN = treeList.nearestNeighbor(point, k-1, false);
				Collections.sort(kNN);
				return kNN.get(k-2).distance;
				/*DistanceQuery<NumberVector> sq = getDistanceFunction().instantiate(null);
				List<Double> distances = new ArrayList<Double>(resList.size());
				for(KNNScore ks : resList){
					if(ks != null)
						distances.add(getSimilarity(sq, newInstance, ks.getVector()));
				}
				Collections.sort(distances);
				return distances.get(k-2);		*/	
			}
		}
	  
	  private double hasResult(NumberVector newInstance){
			for(KNNScore ar : treeList.listItems()){
				if(ar.getVector().equals(newInstance))
					return ar.getDistanceToKthNeighbour();
			}
			return Double.NaN;
		}
	  
	  public Double calculateKNN(Vector vector, Database db) {
		  DistanceQuery<NumberVector> sq = getDistanceFunction().instantiate(null);
		  Relation<NumberVector> relation = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
		  List<Double> nnScores = new LinkedList<Double>();
		  for(DBIDIter it = relation.iterDBIDs(); it.valid(); it.advance()) {
			  double[] bValues = ((DoubleVector)db.getBundle(it).data(1)).getValues();
			  Vector data = new Vector(bValues.length);
			  for(int i=0;i<data.getDimensionality();i++){
				  ((Vector)data).set(i, bValues[i]);
			  }
			  nnScores.add(getSimilarity(sq, vector, data));
		  }
		  Collections.sort(nnScores);
		  if(nnScores == null || nnScores.size() == 0){
			  return Double.MAX_VALUE;
		  } else if(nnScores.size() > k)
			  return nnScores.get(k);
		  else {
			  return nnScores.get(nnScores.size()-1);
		  }
	  }
	  
	  private double getSimilarity(DistanceQuery<NumberVector> sq, NumberVector arg0, NumberVector arg1) {
		  return sq.distance(arg0, arg1);
	  }

	  @Override
	  public TypeInformation[] getInputTypeRestriction() {
	    return TypeUtil.array(getDistanceFunction().getInputTypeRestriction());
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
	  public static class Parameterizer<O> extends AbstractDistanceBasedAlgorithm.Parameterizer<O> {
	    /**
	     * Parameter to specify the k nearest neighbor
	     */
	    public static final OptionID K_ID = new OptionID("knno.k", //
	    "The k nearest neighbor, excluding the query point "//
	    + "(i.e. query point is the 0-nearest-neighbor)");

	    /**
	     * k parameter
	     */
	    protected int k = 0;

	    @Override
	    protected void makeOptions(Parameterization config) {
	      super.makeOptions(config);
	      final IntParameter kP = new IntParameter(K_ID)//
	      .addConstraint(CommonConstraints.GREATER_EQUAL_ONE_INT);
	      if(config.grab(kP)) {
	        k = kP.getValue();
	      }
	    }

	    @Override
	    protected KNNOutlier<O> makeInstance() {
	      return new KNNOutlier<>(distanceFunction, k);
	    }
	  }

	  public void loadFile(String item) {
			BufferedReader reader;
			String readed;
			try {
				treeList = new ELKIEuclid<CustomKNN.KNNScore>();
				if(new File(item).exists()){
					reader = new BufferedReader(new FileReader(new File(item)));
					reader.readLine();
					while(reader.ready()){
						readed = reader.readLine();
						if(readed != null){
							readed = readed.trim();
							KNNScore knn = new KNNScore(readed.split(";")[0].replace("{", "").replace("}",  ""), readed.split(";")[2]);
							treeList.addPoint(knn.getPoint(), knn);
						}
					}
					reader.close();
				}
			} catch (IOException ex) {
				AppLogger.logException(getClass(), ex, "Unable to read LOF file");
			} 
		}

		public void printFile(File file) {
			BufferedWriter writer;
			try {
				if(treeList != null && treeList.size() > 0){
					if(file.exists())
						file.delete();
					writer = new BufferedWriter(new FileWriter(file));
					writer.write("data (enclosed in {});k;distanceToKthNeighbour\n");
					for(KNNScore ar : treeList.listItems()){
						writer.write("{" + ar.getVector().toString().replace(" ", ",") + "};" + (k-1) + ";" + ar.getDistanceToKthNeighbour() + "\n");
					}
					writer.close();
				}
			} catch (IOException ex) {
				AppLogger.logException(getClass(), ex, "Unable to write KNN file");
			} 
		}

		public int size() {
			return treeList.size();
		}

		@Override
		public List<Double> getScoresList() {
			ArrayList<Double> list = new ArrayList<Double>(size());
			for(KNNScore os : treeList.listItems()){
				list.add(os.getDistanceToKthNeighbour());
			}
			Collections.sort(list);
			return list;
		}

	@Override
	public String getAlgorithmName() {
		return "knn";
	}

	@Override
	public Object run(Database db, Relation<NumberVector> relation) {
		return run(relation);
	}
	
	private class KNNScore implements Comparable<KNNScore> {

		private NumberVector data;
		
		private double distanceToKthNeighbour;

		public KNNScore(String vString, String distK) {
			this.distanceToKthNeighbour = Double.parseDouble(distK);
			String[] splitted = vString.split(",");
			data = new Vector(splitted.length);
			for(int i=0;i<data.getDimensionality();i++){
				((Vector)data).set(i, Double.parseDouble(splitted[i].trim()));
			}
		}

		public double[] getPoint(){
			double[] point = new double[data.getDimensionality()];
			for(int i=0;i<point.length;i++){
				point[i] = data.doubleValue(i);
			}
			return point;
		}

		public KNNScore(NumberVector data, double distance) {
			this.data = data;
			this.distanceToKthNeighbour = distance;
		}
		
		public double getDistanceToKthNeighbour(){
			return distanceToKthNeighbour;
		}

		public NumberVector getVector(){
			return data;
		}

		@Override
		public int compareTo(KNNScore o) {
			return Double.compare(distanceToKthNeighbour, o.getDistanceToKthNeighbour());
		}

		@Override
		public String toString() {
			return "KNNScore [data=" + data.toString() + ", kndistance=" + distanceToKthNeighbour + "]";
		}

	}

	public double getDbSize() {
		if(treeList != null)
			return treeList.size();
		else return 0;
	}
	  
}

