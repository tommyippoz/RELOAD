/**
 * 
 */
package ippoz.madness.detector.algorithm.elki.support;

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
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;


/**
 * @author Tommy
 *
 */
public class CustomKNN extends AbstractDistanceBasedAlgorithm<NumberVector, OutlierResult> implements OutlierAlgorithm {
	  /**
	   * The logger for this class.
	   */
	  private static final Logging LOG = Logging.getLogger(KNNOutlier.class);

	  /**
	   * The parameter k (including query point!)
	   */
	  private int k;

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
	  public List<Double> run(Relation<NumberVector> relation) {
	    final DistanceQuery<NumberVector> distanceQuery = relation.getDistanceQuery(getDistanceFunction());
	    final KNNQuery<NumberVector> knnQuery = relation.getKNNQuery(distanceQuery, k);
	    List<Double> nnScores = new LinkedList<Double>();
	    for(DBIDIter it = relation.iterDBIDs(); it.valid(); it.advance()) {
	      // distance to the kth nearest neighbor
	      // (assuming the query point is always included, with distance 0)
	      final double dkn = knnQuery.getKNNForDBID(it, k).getKNNDistance();
	      nnScores.add(dkn);
	    }
	    Collections.sort(nnScores);
	    return nnScores;
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
	  
}

