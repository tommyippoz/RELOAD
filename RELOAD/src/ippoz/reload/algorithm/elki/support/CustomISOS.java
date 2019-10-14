package ippoz.reload.algorithm.elki.support;

/*
 * This file is part of ELKI:
 * Environment for Developing KDD-Applications Supported by Index-Structures
 *
 * Copyright (C) 2019
 * ELKI Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
import java.util.LinkedList;
import java.util.List;

import de.lmu.ifi.dbs.elki.algorithm.AbstractDistanceBasedAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.outlier.OutlierAlgorithm;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDoubleDataStore;
import de.lmu.ifi.dbs.elki.database.ids.DBIDArrayIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDListIter;
import de.lmu.ifi.dbs.elki.database.ids.KNNList;
import de.lmu.ifi.dbs.elki.database.ids.ModifiableDoubleDBIDList;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.relation.DoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.MaterializedDoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.bundle.SingleObjectBundle;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.logging.progress.FiniteProgress;
import de.lmu.ifi.dbs.elki.math.DoubleMinMax;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.math.statistics.intrinsicdimensionality.AggregatedHillEstimator;
import de.lmu.ifi.dbs.elki.math.statistics.intrinsicdimensionality.IntrinsicDimensionalityEstimator;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierScoreMeta;
import de.lmu.ifi.dbs.elki.result.outlier.ProbabilisticOutlierScore;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.GreaterEqualConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.ObjectParameter;
/**
 * Intrinsic Stochastic Outlier Selection.
 * <p>
 * Reference:
 * <p>
 * Erich Schubert, Michael Gertz<br>
 * Intrinsic t-Stochastic Neighbor Embedding for Visualization and Outlier
 * Detection: A Remedy Against the Curse of Dimensionality?<br>
 * Proc. Int. Conf. Similarity Search and Applications, SISAP'2017
 * 
 * @author Erich Schubert
 * @since 0.7.5
 *
 * @param <O> Object type.
 */
/*@Title("ISOS: Intrinsic Stochastic Outlier Selection")
@Reference(authors = "Erich Schubert, Michael Gertz", //
    title = "Intrinsic t-Stochastic Neighbor Embedding for Visualization and Outlier Detection: A Remedy Against the Curse of Dimensionality?", //
    booktitle = "Proc. Int. Conf. Similarity Search and Applications, SISAP'2017", //
    url = "https://doi.org/10.1007/978-3-319-68474-1_13", //
    bibkey = "DBLP:conf/sisap/SchubertG17")*/
public class CustomISOS extends AbstractDistanceBasedAlgorithm<NumberVector, OutlierResult> implements OutlierAlgorithm, ELKIAlgorithm<NumberVector> {
  /**
   * Class logger.
   */
  private static final Logging LOG = Logging.getLogger(CustomISOS.class);

  /**
   * Number of neighbors (not including query point).
   */
  protected int k;
  
  private List<ISOSScore> scoresList;
  
  private double perplexity;

  /**
   * Estimator of intrinsic dimensionality.
   */
  IntrinsicDimensionalityEstimator estimator;

  /**
   * Expected outlier rate.
   */
  protected double phi;

  /**
   * Constructor.
   *
   * @param distanceFunction Distance function
   * @param k Number of neighbors to consider
   * @param estimator Estimator of intrinsic dimensionality.
   */
  public CustomISOS(DistanceFunction<? super NumberVector> distanceFunction, int k, double phi, IntrinsicDimensionalityEstimator estimator) {
    super(distanceFunction);
    this.k = k;
    this.phi = phi;
    this.estimator = estimator;
    perplexity = k / 3.;
  }
  
  public void loadFile(String item) {
		BufferedReader reader;
		String readed;
		try {
			scoresList = new LinkedList<ISOSScore>();
			if(new File(item).exists()){
				reader = new BufferedReader(new FileReader(new File(item)));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						scoresList.add(new ISOSScore(readed.split(";")[0].replace("{", "").replace("}",  ""), readed.split(";")[5], readed.split(";")[4]));
					}
				}
				reader.close();
				Collections.sort(scoresList);
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read LOF file");
		} 
	}

	public void printFile(File file) {
		BufferedWriter writer;
		try {
			if(scoresList != null && scoresList.size() > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("data (enclosed in {});k;perplexity;estimator;s;sos\n");
				for(ISOSScore ar : scoresList){
					writer.write("{" + ar.getVector().toString() + "};" + k + ";" + perplexity + ";" + estimator.toString() + ";" + ar.getS() + ";" + ar.getSOS() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write ISOS file");
		} 
	}

	public int size() {
		return scoresList.size();
	}

	public double getScore(int ratio) {
		if(ratio >= 0 && ratio < size()){
			return scoresList.get(ratio).getSOS();
		} else return Double.NaN;
	}

	@Override
	public List<Double> getScoresList() {
		ArrayList<Double> list = new ArrayList<Double>(size());
		for(ISOSScore os : scoresList){
			list.add(os.getSOS());
		}
		Collections.sort(list);
		return list;
	}
	
	@Override
	public String getAlgorithmName() {
		return "ISOS";
	}

  @Override
  public TypeInformation[] getInputTypeRestriction() {
    return TypeUtil.array(getDistanceFunction().getInputTypeRestriction());
  }
  
  /**
   * Run the algorithm.
   * 
   * @param relation data relation.
   * @return outlier detection result
   */
  @Override
  public OutlierResult run(Database db, Relation<NumberVector> relation) {
    final int k1 = k + 1; // Query size
    KNNQuery<NumberVector> knnq = relation.getKNNQuery(getDistanceFunction(), k1);
    final double logPerp = perplexity > 1. ? Math.log(perplexity) : .1;
    
    scoresList = new LinkedList<ISOSScore>();

    double[] p = new double[k + 10];
    ModifiableDoubleDBIDList dists = DBIDUtil.newDistanceDBIDList(k + 10);
    DoubleDBIDListIter di = dists.iter();
    FiniteProgress prog = LOG.isVerbose() ? new FiniteProgress("ISOS scores", relation.size(), LOG) : null;
    WritableDoubleDataStore scores = DataStoreUtil.makeDoubleStorage(relation.getDBIDs(), DataStoreFactory.HINT_HOT | DataStoreFactory.HINT_DB, 1.);
    List<Double> sList = new LinkedList<Double>();
    for(DBIDIter it = relation.iterDBIDs(); it.valid(); it.advance()) {
      KNNList knns = knnq.getKNNForDBID(it, k1);
      if(p.length < knns.size() + 1) {
        p = new double[knns.size() + 10];
      }
      final DoubleDBIDListIter ki = knns.iter();
      try {
        double id = estimateID(it, ki, p);
        adjustDistances(it, ki, knns.getKNNDistance(), id, dists);
        // We now continue with the modified distances below.
        // Compute affinities
        CustomSOS.computePi(it, di, p, perplexity, logPerp);
        // Normalization factor:
        double s = CustomSOS.sumOfProbabilities(it, di, p);
        sList.add(s);
        if(s > 0.) {
          nominateNeighbors(it, di, p, 1. / s, scores);
        }
      }
      catch(ArithmeticException e) {
        // ID estimation failed, supposedly constant values because of too many
        // duplicate points, or too small k. Fall back to KNNSOS.
        // Note: this looks almost identical to the above, but uses ki instead
        // of the adjusted distances di!
        // Compute affinities
        CustomSOS.computePi(it, ki, p, perplexity, logPerp);
        // Normalization factor:
        double s = CustomSOS.sumOfProbabilities(it, ki, p);
        sList.add(s);
        if(s > 0.) {
          nominateNeighbors(it, ki, p, 1. / s, scores);
        }
      }
      LOG.incrementProcessed(prog);
    }
    LOG.ensureCompleted(prog);
    DoubleMinMax minmax = transformScores(db, scores, relation.getDBIDs(), logPerp, phi, sList);
    DoubleRelation scoreres = new MaterializedDoubleRelation("Intrinsic Stoachastic Outlier Selection", "isos-outlier", scores, relation.getDBIDs());
    OutlierScoreMeta meta = new ProbabilisticOutlierScore(minmax.getMin(), minmax.getMax(), 0.);
    return new OutlierResult(meta, scoreres);
  }

  protected static void adjustDistances(DBIDRef ignore, DoubleDBIDListIter ki, double max, double id, ModifiableDoubleDBIDList dists) {
    dists.clear();
    double scaleexp = id * .5; // Generate squared distances.
    double scalelin = 1. / max; // Linear scaling
    for(ki.seek(0); ki.valid(); ki.advance()) {
      if(DBIDUtil.equal(ignore, ki)) {
        continue;
      }
      double d = Math.pow(ki.doubleValue() * scalelin, scaleexp);
      dists.add(d, ki);
    }
    return;
  }
  
  protected static List<Double> adjustDistances(List<Double> neiDist, double max, double id) {
	    List<Double> dists = new LinkedList<Double>();
	    double scaleexp = id * .5; // Generate squared distances.
	    double scalelin = 1. / max; // Linear scaling
	    for(int i=0;i<neiDist.size();i++) {
	      double d = Math.pow(neiDist.get(i) * scalelin, scaleexp);
	      dists.add(d);
	    }
	    return dists;
	  }

  /**
   * Estimate the local intrinsic dimensionality.
   * 
   * @param ignore Object to ignore
   * @param it Iterator
   * @param p Scratch array
   * @return ID estimate
   */
  protected double estimateID(DBIDRef ignore, DoubleDBIDListIter it, double[] p) {
    int j = 0;
    for(it.seek(0); it.valid(); it.advance()) {
      if(it.doubleValue() == 0. || DBIDUtil.equal(ignore, it)) {
        continue;
      }
      p[j++] = it.doubleValue();
    }
    if(j < 2) {
      throw new ArithmeticException("Too little data to estimate ID.");
    }
    return estimator.estimate(p, j);
  }
  
  protected double estimateID(List<Double> neiDist, double[] p) {
	    int j = 0;
	    for(j=0;j<neiDist.size();j++) {
	      if(neiDist.get(j) == 0.) {
	        continue;
	      }
	      p[j] = neiDist.get(j);
	    }
	    if(j < 2) {
	      throw new ArithmeticException("Too little data to estimate ID.");
	    }
	    return estimator.estimate(p, j);
	  }

  /**
   * Vote for neighbors not being outliers. The key method of SOS.
   * 
   * @param ignore Object to ignore
   * @param di Neighbor object IDs.
   * @param p Probabilities
   * @param norm Normalization factor (1/sum)
   * @param scores Output score storage
   */
  public static void nominateNeighbors(DBIDIter ignore, DBIDArrayIter di, double[] p, double norm, WritableDoubleDataStore scores) {
    for(di.seek(0); di.valid(); di.advance()) {
      if(DBIDUtil.equal(ignore, di)) {
        continue;
      }
      double v = p[di.getOffset()] * norm; // Normalize
      if(!(v > 0)) {
        break;
      }
      scores.increment(di, Math.log1p(-v));
    }
  }
  
  public static double nominateNeighbors(List<ISOSCouple> neiDist, double[] p) {
	  double sos = 1;  
	  for(int i=0;i<neiDist.size();i++) {
	      double v = p[i] * neiDist.get(i).getScore().getNorm(); // Normalize
	      if(!(v > 0)) {
	        break;
	      }
	      sos = sos + Math.log1p(-v);
	    }
	  return sos;
	  }

  public double calculateSingleISOS(NumberVector newInstance) {
	  double partialResult;
	  if(scoresList == null || scoresList.size() == 0) 
		  return Double.MAX_VALUE;
	  else if(Double.isFinite(partialResult = hasResult(newInstance)))
		  return partialResult;
	  else {
		  DistanceQuery<NumberVector> sq = getDistanceFunction().instantiate(null);

		  // Calculating distances
		  List<ISOSCouple> distances = new LinkedList<ISOSCouple>();
		  for(ISOSScore ss : scoresList){
			  distances.add(new ISOSCouple(ss, getSimilarity(sq, ss.getVector(), newInstance)));
		  }
		  Collections.sort(distances);

		  // Extracting neighbours
		  List<ISOSCouple> neiDist = new LinkedList<ISOSCouple>();
		  for(int i=0;i<k+1;i++){
			  neiDist.add(distances.get(distances.size() - i - 1));
		  }
		  Collections.sort(neiDist);

		  // Calculating ISOS
		  double[] p = new double[k + 10];
		  double sos = Double.NaN;
		  List<Double> neiDistDouble = extractDistancesFrom(neiDist);
		  // Trying adjustment of distances
		  try {
			  double id = estimateID(neiDistDouble, p);
			  neiDistDouble = adjustDistances(neiDistDouble, Collections.max(neiDistDouble), id);			  
		  } catch(ArithmeticException e) {}
		  CustomSOS.computePi(neiDistDouble, p, perplexity, Math.log(perplexity));
		  // Normalization factor:
		  double s = CustomSOS.sumOfProbabilities(neiDistDouble, p);
		  if(s > 0.) {
			  sos = nominateNeighbors(neiDist, p);
		  }
		  return transformScore(sos, Math.log(perplexity), phi);
	  }
		   
	}
  
  private List<Double> extractDistancesFrom(List<ISOSCouple> list){
	  List<Double> distances = new LinkedList<Double>();
	  for(ISOSCouple sc : list){
		  distances.add(sc.getDistance());
	  }
	  return distances;
  }

  private double getSimilarity(DistanceQuery<NumberVector> sq, NumberVector arg0, NumberVector arg1) {
		return sq.distance(arg0, arg1);
	}

  private double hasResult(NumberVector newInstance){
		for(ISOSScore ar : scoresList){
			if(ar.getVector().equals(newInstance))
				return ar.getSOS();
		}
		return Double.NaN;
	}
  
private class ISOSCouple implements Comparable<ISOSCouple> {
	  
	  private ISOSScore score;
	  
	  private double distance; 

	public ISOSCouple(ISOSScore score, double distance) {
		this.score = score;
		this.distance = distance;
	}

	public ISOSScore getScore() {
		return score;
	}
	
	public double getDistance() {
		return distance;
	}

	@Override
	public int compareTo(ISOSCouple o) {
		return Double.compare(distance, o.getDistance());
	}  
	  
  }
  
  private class ISOSScore implements Comparable<ISOSScore> {

		private NumberVector data;

		private double sos;
		
		private double s;

		public ISOSScore(SingleObjectBundle bundle, double sos, double s) {
			this.sos = sos;
			this.s = s;
			double[] bValues = ((DoubleVector)bundle.data(1)).getValues();
			data = new Vector(bValues.length);
			for(int i=0;i<data.getDimensionality();i++){
				((Vector)data).set(i, bValues[i]);
			}
		}
	
		public ISOSScore(String vString, String sos, String s) {
			this.sos = Double.parseDouble(sos);
			this.s = Double.parseDouble(s);
			String[] splitted = vString.split(",");
			data = new Vector(splitted.length);
			for(int i=0;i<data.getDimensionality();i++){
				((Vector)data).set(i, Double.parseDouble(splitted[i].trim()));
			}
		}

		public double getSOS() {
			return sos;
		}
		
		public double getS(){
			return s;
		}

		public double getNorm(){
			return 1.0 / s;
		}
		
		public NumberVector getVector(){
			return data;
		}

		@Override
		public int compareTo(ISOSScore o) {
			return Double.compare(sos, o.getSOS());
		}

		@Override
		public String toString() {
			return "SOSScore [data=" + data.toString() + ", sos=" + sos + "]";
		}

	}

  /**
   * Transform scores
   * 
   * @param scores Scores to transform
   * @param ids DBIDs to process
   * @param logPerp log perplexity
   * @param phi Expected outlier rate
 * @param sList 
   * @return Minimum and maximum scores
   */
  public DoubleMinMax transformScores(Database db, WritableDoubleDataStore scores, DBIDs ids, double logPerp, double phi, List<Double> sList) {
    DoubleMinMax minmax = new DoubleMinMax();
    double adj = (1 - phi) / phi;
    int i = 0;
    for(DBIDIter it = ids.iter(); it.valid(); it.advance()) {
      double or = Math.exp(-scores.doubleValue(it) * logPerp) * adj;
      double s = 1. / (1 + or);
      scores.putDouble(it, s);
      scoresList.add(new ISOSScore(db.getBundle(it), s, sList.get(i++)));
      minmax.put(s);
    }
    return minmax;
  }
  
  public double transformScore(double sos, double logPerp, double phi) {
	    double adj = (1 - phi) / phi;
	    double or = Math.exp(sos * logPerp) * adj;
	    double s = 1. / (1 + or);
	    return s;
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
   * @hidden
   *
   * @param <O> Object type
   */
  public static class Parameterizer extends AbstractDistanceBasedAlgorithm.Parameterizer<NumberVector> {
    /**
     * Parameter to specify the number of neighbors
     */
    public static final OptionID KNN_ID = new OptionID("isos.k", "Number of neighbors to use. Should be about 3x the desired perplexity.");

    /**
     * Parameter for ID estimation.
     */
    public static final OptionID ESTIMATOR_ID = new OptionID("isos.estimator", "Estimator for intrinsic dimensionality.");

    /**
     * Number of neighbors
     */
    int k = 15;

    /**
     * Estimator of intrinsic dimensionality.
     */
    IntrinsicDimensionalityEstimator estimator = AggregatedHillEstimator.STATIC;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      IntParameter kP = new IntParameter(KNN_ID, 100) //
          .addConstraint(new GreaterEqualConstraint(5));
      if(config.grab(kP)) {
        k = kP.intValue();
      }
      ObjectParameter<IntrinsicDimensionalityEstimator> estimatorP = new ObjectParameter<>(ESTIMATOR_ID, IntrinsicDimensionalityEstimator.class, AggregatedHillEstimator.class);
      if(config.grab(estimatorP)) {
        estimator = estimatorP.instantiateClass(config);
      }
    }

    @Override
    protected CustomISOS makeInstance() {
      return new CustomISOS(distanceFunction, k, 0.01, estimator);
    }
  }

}
