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
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDListIter;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDListMIter;
import de.lmu.ifi.dbs.elki.database.ids.ModifiableDoubleDBIDList;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.relation.DoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.MaterializedDoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.bundle.SingleObjectBundle;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.logging.progress.FiniteProgress;
import de.lmu.ifi.dbs.elki.math.DoubleMinMax;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierScoreMeta;
import de.lmu.ifi.dbs.elki.result.outlier.ProbabilisticOutlierScore;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.DoubleParameter;


/**
 * Stochastic Outlier Selection.
 * <p>
 * Reference:
 * <p>
 * J. Janssens, F. Huszár, E. Postma, J. van den Herik<br>
 * Stochastic Outlier Selection<br>
 * TiCC TR 2012–001
 * 
 * @author Erich Schubert
 * @since 0.7.5
 *
 * @param <O> Object type
 */
public class CustomSOS extends AbstractDistanceBasedAlgorithm<NumberVector, OutlierResult> implements OutlierAlgorithm, ELKIAlgorithm<NumberVector> {
  /**
   * Class logger.
   */
  private static final Logging LOG = Logging.getLogger(CustomSOS.class);

  /**
   * Threshold for optimizing perplexity.
   */
  final static protected double PERPLEXITY_ERROR = 1e-5;

  /**
   * Maximum number of iterations when optimizing perplexity.
   */
  final static protected int PERPLEXITY_MAXITER = 50;

  /**
   * Perplexity
   */
  protected double perplexity;
  
  private List<SOSScore> scoresList;

  /**
   * Constructor.
   *
   * @param distance Distance function
   * @param h Perplexity
   */
  public CustomSOS(DistanceFunction<? super NumberVector> distance, double h) {
    super(distance);
    this.perplexity = h;
  }

  @Override
  public TypeInformation[] getInputTypeRestriction() {
    return TypeUtil.array(getDistanceFunction().getInputTypeRestriction());
  }
  
  public void loadFile(String item) {
		BufferedReader reader;
		String readed;
		try {
			scoresList = new LinkedList<SOSScore>();
			if(new File(item).exists()){
				reader = new BufferedReader(new FileReader(new File(item)));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						scoresList.add(new SOSScore(readed.split(";")[0].replace("{", "").replace("}",  ""), readed.split(";")[3], readed.split(";")[2]));
					}
				}
				reader.close();
				Collections.sort(scoresList);
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read SOS file");
		} 
	}

	public void printFile(File file) {
		BufferedWriter writer;
		try {
			if(scoresList != null && scoresList.size() > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("data (enclosed in {});h;s;sos\n");
				for(SOSScore ar : scoresList){
					writer.write("{" + ar.getVector().toString() + "};" + perplexity + ";" + ar.getS() + ";" + ar.getSOS() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write SOS file");
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
		for(SOSScore os : scoresList){
			list.add(os.getSOS());
		}
		Collections.sort(list);
		return list;
	}
	
	@Override
	public String getAlgorithmName() {
		return "SOS";
	}

  /**
   * Run the algorithm.
   * 
   * @param relation data relation
   * @return outlier detection result
   */
  public OutlierResult run(Database db, Relation<NumberVector> relation) {
    DistanceQuery<NumberVector> dq = relation.getDistanceQuery(getDistanceFunction());
    final double logPerp = Math.log(perplexity);
    
    scoresList = new LinkedList<SOSScore>();
    
    ModifiableDoubleDBIDList dlist = DBIDUtil.newDistanceDBIDList(relation.size() - 1);
    DoubleDBIDListMIter di = dlist.iter();
    double[] p = new double[relation.size() - 1];
    FiniteProgress prog = LOG.isVerbose() ? new FiniteProgress("SOS scores", relation.size(), LOG) : null;
    WritableDoubleDataStore scores = DataStoreUtil.makeDoubleStorage(relation.getDBIDs(), DataStoreFactory.HINT_HOT | DataStoreFactory.HINT_DB, 1.);
    
    List<Double> sList = new LinkedList<Double>();
    for(DBIDIter it = relation.iterDBIDs(); it.valid(); it.advance()) {
      dlist.clear();
      for(DBIDIter i2 = relation.iterDBIDs(); i2.valid(); i2.advance()) {
        if(DBIDUtil.equal(it, i2)) {
          continue;
        }
        dlist.add(dq.distance(it, i2), i2);
      }
      dlist.sort();
      
      // Compute affinities
      computePi(it, di, p, perplexity, logPerp);
      // Normalization factor:
      double s = sumOfProbabilities(it, di, p);
      sList.add(s);
      if(s > 0) {
        nominateNeighbors(it, di, p, 1. / s, scores);
      }
      
      LOG.incrementProcessed(prog);
    }
    LOG.ensureCompleted(prog);
    // Find minimum and maximum.
    DoubleMinMax minmax = new DoubleMinMax();
    int i=0;
    for(DBIDIter it2 = relation.iterDBIDs(); it2.valid(); it2.advance()) {
      minmax.put(scores.doubleValue(it2));
      scoresList.add(new SOSScore(db.getBundle(it2), scores.doubleValue(it2), sList.get(i++)));
      //System.out.println(db.getBundle(it2).data(1) + " - " + scores.doubleValue(it2));
    }
    DoubleRelation scoreres = new MaterializedDoubleRelation("Stoachastic Outlier Selection", "sos-outlier", scores, relation.getDBIDs());
    OutlierScoreMeta meta = new ProbabilisticOutlierScore(minmax.getMin(), minmax.getMax(), 0.);
    return new OutlierResult(meta, scoreres);
  }
  
  public double calculateSingleSOS(NumberVector newInstance) {
		double partialResult;
		if(scoresList == null || scoresList.size() == 0) 
			return Double.MAX_VALUE;
		else if(Double.isFinite(partialResult = hasResult(newInstance)))
			return partialResult;
		else {
			DistanceQuery<NumberVector> sq = getDistanceFunction().instantiate(null);
			List<SOSCouple> distances = new LinkedList<SOSCouple>();
			for(SOSScore ss : scoresList){
				distances.add(new SOSCouple(ss, getSimilarity(sq, ss.getVector(), newInstance)));
			}
			Collections.sort(distances);
			double[] p = new double[distances.size()];
			computePi(extractDistancesFrom(distances), p, perplexity, Math.log(perplexity));
		    // Normalization factor
		    return nominateNeighbors(distances, p);
		}
	}
  
  private List<Double> extractDistancesFrom(List<SOSCouple> list){
	  List<Double> distances = new LinkedList<Double>();
	  for(SOSCouple sc : list){
		  distances.add(sc.getDistance());
	  }
	  return distances;
  }
  
  private double getSimilarity(DistanceQuery<NumberVector> sq, NumberVector arg0, NumberVector arg1) {
		return sq.distance(arg0, arg1);
	}
  
  private double hasResult(NumberVector newInstance){
		for(SOSScore ar : scoresList){
			if(ar.getVector().equals(newInstance))
				return ar.getSOS();
		}
		return Double.NaN;
	}
  
  private class SOSCouple implements Comparable<SOSCouple> {
	  
	  private SOSScore score;
	  
	  private double distance; 

	public SOSCouple(SOSScore score, double distance) {
		this.score = score;
		this.distance = distance;
	}

	public SOSScore getScore() {
		return score;
	}
	
	public double getDistance() {
		return distance;
	}

	@Override
	public int compareTo(SOSCouple o) {
		return Double.compare(distance, o.getDistance());
	}  
	  
  }
  
  private class SOSScore implements Comparable<SOSScore> {

		private NumberVector data;

		private double sos;
		
		private double s;

		public SOSScore(SingleObjectBundle bundle, double sos, double s) {
			this.sos = sos;
			this.s = s;
			double[] bValues = ((DoubleVector)bundle.data(1)).getValues();
			data = new Vector(bValues.length);
			for(int i=0;i<data.getDimensionality();i++){
				((Vector)data).set(i, bValues[i]);
			}
		}
	
		public double getS() {
			return s;
		}

		public SOSScore(String vString, String sos, String s) {
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
		
		public double getNorm(){
			return 1. / s;
		}

		public NumberVector getVector(){
			return data;
		}

		@Override
		public int compareTo(SOSScore o) {
			return Double.compare(sos, o.getSOS());
		}

		@Override
		public String toString() {
			return "SOSScore [data=" + data.toString() + ", sos=" + sos + "]";
		}

	}

  /**
   * Compute the sum of probabilities, stop at first 0, ignore query object.
   * 
   * Note: while SOS ensures the 'ignore' object is not added in the first
   * place, KNNSOS cannot do so efficiently (yet).
   * 
   * @param ignore Object to ignore.
   * @param di Object list
   * @param p Probabilities
   * @return Sum.
   */
  public static double sumOfProbabilities(DBIDIter ignore, DBIDArrayIter di, double[] p) {
    double s = 0;
    for(di.seek(0); di.valid(); di.advance()) {
      if(DBIDUtil.equal(ignore, di)) {
        continue;
      }
      final double v = p[di.getOffset()];
      if(!(v > 0)) {
        break;
      }
      s += v;
    }
    return s;
  }
  
  public static double sumOfProbabilities(List<Double> distances, double[] p) {
	    double s = 0;
	    for(int i=0;i<distances.size();i++) {
	      final double v = p[i];
	      if(!(v > 0)) {
	        break;
	      }
	      s += v;
	    }
	    return s;
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
      scores.putDouble(di, scores.doubleValue(di) * (1 - v));
    }
  }
  
  private double nominateNeighbors(List<SOSCouple> distances, double[] p) {
	  double sos = 1;  
	  for(int i=0;i<distances.size();i++) {
	      double v = p[i] * distances.get(i).getScore().getNorm(); // Normalize
	      if(!(v > 0)) {
	        break;
	      }
	      sos = sos*(1 - v);
	  }
	  return sos;
}
  

  /**
   * Compute row p[i], using binary search on the kernel bandwidth sigma to
   * obtain the desired perplexity.
   *
   * @param ignore Object to skip
   * @param it Distances iterator
   * @param p Output row
   * @param perplexity Desired perplexity
   * @param logPerp Log of desired perplexity
   * @return Beta
   */
  public static double computePi(DBIDRef ignore, DoubleDBIDListIter it, double[] p, double perplexity, double logPerp) {
    // Relation to paper: beta == 1. / (2*sigma*sigma)
    double beta = estimateInitialBeta(ignore, it, perplexity);
    double diff = computeH(ignore, it, p, -beta) - logPerp;
    double betaMin = 0.;
    double betaMax = Double.POSITIVE_INFINITY;
    for(int tries = 0; tries < PERPLEXITY_MAXITER && Math.abs(diff) > PERPLEXITY_ERROR; ++tries) {
      if(diff > 0) {
        betaMin = beta;
        beta += (betaMax == Double.POSITIVE_INFINITY) ? beta : ((betaMax - beta) * .5);
      }
      else {
        betaMax = beta;
        beta -= (beta - betaMin) * .5;
      }
      diff = computeH(ignore, it, p, -beta) - logPerp;
    }
    return beta;
  }
  
  public static double computePi(List<Double> distances, double[] p, double perplexity, double logPerp) {
	    // Relation to paper: beta == 1. / (2*sigma*sigma)
	    double beta = estimateInitialBeta(distances, perplexity);
	    double diff = computeH(distances, p, -beta) - logPerp;
	    double betaMin = 0.;
	    double betaMax = Double.POSITIVE_INFINITY;
	    for(int tries = 0; tries < PERPLEXITY_MAXITER && Math.abs(diff) > PERPLEXITY_ERROR; ++tries) {
	      if(diff > 0) {
	        betaMin = beta;
	        beta += (betaMax == Double.POSITIVE_INFINITY) ? beta : ((betaMax - beta) * .5);
	      }
	      else {
	        betaMax = beta;
	        beta -= (beta - betaMin) * .5;
	      }
	      diff = computeH(distances, p, -beta) - logPerp;
	    }
	    return beta;
	  }

  /**
   * Estimate beta from the distances in a row.
   * <p>
   * This lacks a thorough mathematical argument, but is a handcrafted heuristic
   * to avoid numerical problems. The average distance is usually too large, so
   * we scale the average distance by 2*N/perplexity. Then estimate beta as 1/x.
   *
   * @param ignore Object to skip
   * @param it Distance iterator
   * @param perplexity Desired perplexity
   * @return Estimated beta.
   */
  /*@Reference(authors = "Erich Schubert, Michael Gertz", //
      title = "Intrinsic t-Stochastic Neighbor Embedding for Visualization and Outlier Detection: A Remedy Against the Curse of Dimensionality?", //
      booktitle = "Proc. Int. Conf. Similarity Search and Applications, SISAP'2017", //
      url = "https://doi.org/10.1007/978-3-319-68474-1_13", //
      bibkey = "DBLP:conf/sisap/SchubertG17")*/
  protected static double estimateInitialBeta(DBIDRef ignore, DoubleDBIDListIter it, double perplexity) {
    double sum = 0.;
    int size = 0;
    for(it.seek(0); it.valid(); it.advance()) {
      if(DBIDUtil.equal(ignore, it)) {
        continue;
      }
      sum += it.doubleValue() < Double.POSITIVE_INFINITY ? it.doubleValue() : 0.;
      ++size;
    }
    // In degenerate cases, simply return 1.
    return (sum > 0. && sum < Double.POSITIVE_INFINITY) ? (.5 / sum * perplexity * (size - 1.)) : 1.;
  }
  
  protected static double estimateInitialBeta(List<Double> distances, double perplexity) {
	    double sum = 0.;
	    int size = 0;
	    for(int i=0;i<distances.size();i++) {
	      sum += distances.get(i) < Double.POSITIVE_INFINITY ? distances.get(i) : 0.;
	      ++size;
	    }
	    // In degenerate cases, simply return 1.
	    return (sum > 0. && sum < Double.POSITIVE_INFINITY) ? (.5 / sum * perplexity * (size - 1.)) : 1.;
	  }

  /**
   * Compute H (observed perplexity) for row i, and the row pij_i.
   * 
   * @param ignore Object to skip
   * @param it Distances list
   * @param p Output probabilities
   * @param mbeta {@code -1. / (2 * sigma * sigma)}
   * @return Observed perplexity
   */
  protected static double computeH(DBIDRef ignore, DoubleDBIDListIter it, double[] p, double mbeta) {
    double sumP = 0.;
    // Skip point "i", break loop in two:
    it.seek(0);
    for(int j = 0; it.valid(); j++, it.advance()) {
      if(DBIDUtil.equal(ignore, it)) {
        p[j] = 0;
        continue;
      }
      sumP += (p[j] = Math.exp(it.doubleValue() * mbeta));
    }
    if(!(sumP > 0)) {
      // All pij are zero. Bad news.
      return Double.NEGATIVE_INFINITY;
    }
    final double s = 1. / sumP; // Scaling factor
    double sum = 0.;
    // While we could skip pi[i], it should be 0 anyway.
    it.seek(0);
    for(int j = 0; it.valid(); j++, it.advance()) {
      sum += it.doubleValue() * (p[j] *= s);
    }
    return Math.log(sumP) - mbeta * sum;
  }
  
  /**
   * Compute H (observed perplexity) for row i, and the row pij_i.
   * 
   * @param ignore Object to skip
   * @param it Distances list
   * @param p Output probabilities
   * @param mbeta {@code -1. / (2 * sigma * sigma)}
   * @return Observed perplexity
   */
  private static double computeH(List<Double> distances, double[] p, double mbeta) {
    double sumP = 0.;
    for(int j = 0; j < distances.size(); j++) {
      sumP += (p[j] = Math.exp(distances.get(j) * mbeta));
    }
    if(!(sumP > 0)) {
      // All pij are zero. Bad news.
      return Double.NEGATIVE_INFINITY;
    }
    final double s = 1. / sumP; // Scaling factor
    double sum = 0.;
    for(int j = 0; j < distances.size(); j++) {
      sum += distances.get(j) * (p[j] *= s);
    }
    return Math.log(sumP) - mbeta * sum;
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
     * Parameter to specify perplexity
     */
    public static final OptionID PERPLEXITY_ID = new OptionID("sos.perplexity", "Perplexity to use.");

    /**
     * Perplexity.
     */
    double perplexity = 4.5;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      DoubleParameter perplexityP = new DoubleParameter(PERPLEXITY_ID, 4.5) //
          .addConstraint(CommonConstraints.GREATER_THAN_ZERO_DOUBLE);
      if(config.grab(perplexityP)) {
        perplexity = perplexityP.doubleValue();
      }
    }

    @Override
    protected CustomSOS makeInstance() {
      return new CustomSOS(distanceFunction, perplexity);
    }
  }

}

