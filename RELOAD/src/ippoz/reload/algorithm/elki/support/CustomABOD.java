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
import java.util.LinkedList;
import java.util.List;

import de.lmu.ifi.dbs.elki.algorithm.AbstractAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.outlier.OutlierAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.outlier.anglebased.ABOD;
import de.lmu.ifi.dbs.elki.algorithm.outlier.anglebased.FastABOD;
import de.lmu.ifi.dbs.elki.algorithm.outlier.anglebased.LBABOD;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDoubleDataStore;
import de.lmu.ifi.dbs.elki.database.ids.ArrayDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBIDArrayIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.query.similarity.SimilarityQuery;
import de.lmu.ifi.dbs.elki.database.relation.DoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.MaterializedDoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.bundle.SingleObjectBundle;
import de.lmu.ifi.dbs.elki.distance.similarityfunction.SimilarityFunction;
import de.lmu.ifi.dbs.elki.distance.similarityfunction.kernel.KernelMatrix;
import de.lmu.ifi.dbs.elki.distance.similarityfunction.kernel.PolynomialKernelFunction;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.math.DoubleMinMax;
import de.lmu.ifi.dbs.elki.math.MeanVariance;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.result.outlier.InvertedOutlierScoreMeta;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierScoreMeta;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.ObjectParameter;

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
 * Added on: Fall 2018
 * 
 */

/**
* Angle-Based Outlier Detection / Angle-Based Outlier Factor.
*
* Outlier detection using variance analysis on angles, especially for high
* dimensional data sets. Exact version, which has cubic runtime (see also
* {@link FastABOD} and {@link LBABOD} for faster versions).
*
* Reference:
* <p>
* H.-P. Kriegel, M. Schubert, and A. Zimek:<br />
* Angle-Based Outlier Detection in High-dimensional Data.<br />
* In: Proc. 14th ACM SIGKDD Int. Conf. on Knowledge Discovery and Data Mining
* (KDD '08), Las Vegas, NV, 2008.
* </p>
*
* @author Matthias Schubert (Original Code)
* @author Erich Schubert (ELKIfication)
* @since 0.2
*
* @param <V> Vector type
*/
public class CustomABOD<V extends NumberVector> extends AbstractAlgorithm<OutlierResult> implements OutlierAlgorithm, ELKIAlgorithm<V> {
	
	/**
	   * The logger for this class.
	   */
	  private static final Logging LOG = Logging.getLogger(ABOD.class);

	  /**
	   * Store the configured Kernel version.
	   */
	  protected SimilarityFunction<? super V> kernelFunction;
	  
	  private List<ABODResult> resList;

	  /**
	   * Constructor for Angle-Based Outlier Detection (ABOD).
	   *
	   * @param kernelFunction kernel function to use
	   */
	  public CustomABOD(SimilarityFunction<? super V> kernelFunction) {
	    super();
	    this.kernelFunction = kernelFunction;
	  }

	  /**
	   * Run ABOD on the data set.
	   *
	   * @param relation Relation to process
	   * @return Outlier detection result
	   */
	  public OutlierResult run(Database db, Relation<V> relation) {
	    ArrayDBIDs ids = DBIDUtil.ensureArray(relation.getDBIDs());
	    // Build a kernel matrix, to make O(n^3) slightly less bad.
	    SimilarityQuery<V> sq = db.getSimilarityQuery(relation, kernelFunction);
	    KernelMatrix kernelMatrix = new KernelMatrix(sq, relation, ids);

	    WritableDoubleDataStore abodvalues = DataStoreUtil.makeDoubleStorage(ids, DataStoreFactory.HINT_STATIC);
	    DoubleMinMax minmaxabod = new DoubleMinMax();
	    
	    resList = new ArrayList<ABODResult>(ids.size());

	    MeanVariance s = new MeanVariance();
	    DBIDArrayIter pA = ids.iter(), pB = ids.iter(), pC = ids.iter();
	    for(; pA.valid(); pA.advance()) {
	      final double abof = computeABOF(kernelMatrix, pA, pB, pC, s);
	      if(Double.isFinite(abof)){
		      resList.add(new ABODResult(db.getBundle(pA), abof));
		      minmaxabod.put(abof);
		      abodvalues.putDouble(pA, abof);
	      }
	    }

	    Collections.sort(resList);
	    
	    // Build result representation.
	    DoubleRelation scoreResult = new MaterializedDoubleRelation("Angle-Based Outlier Degree", "abod-outlier", abodvalues, relation.getDBIDs());
	    OutlierScoreMeta scoreMeta = new InvertedOutlierScoreMeta(minmaxabod.getMin(), minmaxabod.getMax(), 0.0, Double.POSITIVE_INFINITY);
	    return new OutlierResult(scoreMeta, scoreResult);
	  }

	  /**
	   * Compute the exact ABOF value.
	   *
	   * @param kernelMatrix Kernel matrix
	   * @param pA Object A to compute ABOF for
	   * @param pB Iterator over objects B
	   * @param pC Iterator over objects C
	   * @param s Statistics tracker
	   * @return ABOF value
	   */
	  protected double computeABOF(KernelMatrix kernelMatrix, DBIDRef pA, DBIDArrayIter pB, DBIDArrayIter pC, MeanVariance s) {
	    s.reset(); // Reused
	    double simAA = kernelMatrix.getSimilarity(pA, pA);

	    for(pB.seek(0); pB.valid(); pB.advance()) {
	      if(DBIDUtil.equal(pB, pA)) {
	        continue;
	      }
	      double simBB = kernelMatrix.getSimilarity(pB, pB);
	      double simAB = kernelMatrix.getSimilarity(pA, pB);
	      double sqdAB = simAA + simBB - simAB - simAB;
	      if(!(sqdAB > 0.)) {
	        continue;
	      }
	      for(pC.seek(pB.getOffset() + 1); pC.valid(); pC.advance()) {
	        if(DBIDUtil.equal(pC, pA)) {
	          continue;
	        }
	        double simCC = kernelMatrix.getSimilarity(pC, pC);
	        double simAC = kernelMatrix.getSimilarity(pA, pC);
	        double sqdAC = simAA + simCC - simAC - simAC;
	        if(!(sqdAC > 0.)) {
	          continue;
	        }
	        // Exploit bilinearity of scalar product:
	        // <B-A, C-A> = <B,C-A> - <A,C-A>
	        // = <B,C> - <B,A> - <A,C> + <A,A>
	        double simBC = kernelMatrix.getSimilarity(pB, pC);
	        double numerator = simBC - simAB - simAC + simAA;
	        double div = 1. / (sqdAB * sqdAC);
	        //System.out.println(numerator + " - " + div);
	        if(Double.isFinite(numerator) && Double.isFinite(div))
	        	s.put(numerator * div, Math.sqrt(div));
	      }
	    }
	    // Sample variance probably would be better here, but the ABOD publication
	    // uses the naive variance.
	    return s.getNaiveVariance();
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
		    double simAA = getSimilarity(sq, newInstance, newInstance);

		    for(int i=0;i<resList.size();i++) {
		      
		      double simBB = getSimilarity(sq, resList.get(i).getVector(), resList.get(i).getVector());
		      //System.out.println("simBB:" + simBB);
			  double simAB = getSimilarity(sq, newInstance, resList.get(i).getVector());
			  //System.out.println("simAB:" + simBB);
			  double sqdAB = simAA + simBB - simAB - simAB;
		      if(!(sqdAB > 0.)) {
		    	  sqdAB = -sqdAB;
		      }
		      
		      for(int j=i+1;j<resList.size();j++) {
		        
		        double simCC = getSimilarity(sq, resList.get(j).getVector(), resList.get(j).getVector());
				double simAC = getSimilarity(sq, newInstance, resList.get(j).getVector());
				double sqdAC = simAA + simCC - simAC - simAC;
		        
		        if(!(sqdAC > 0.)) {
		          sqdAC = -sqdAC;
		        }
		        // Exploit bilinearity of scalar product:
		        // <B-A, C-A> = <B,C-A> - <A,C-A>
		        // = <B,C> - <B,A> - <A,C> + <A,A>
		        //System.out.println(resList.get(i).getVector());
		        //System.out.println(resList.get(j).getVector());
		        double simBC = getSimilarity(sq, resList.get(i).getVector(), resList.get(j).getVector());
		        double numerator = simBC - simAB - simAC + simAA;
		        //System.out.println("sqdAB " + sqdAB + " sqdAC " + sqdAC);
		        double div = 1. / (sqdAB * sqdAC);
		        //System.out.println("numerator " + numerator + " div " + div);
		        if(Double.isFinite(numerator) && Double.isFinite(div))
		        	s.put(numerator * div, Math.sqrt(div));
		      }
		    }
		    // Sample variance probably would be better here, but the ABOD publication
		    // uses the naive variance.
		    return s.getNaiveVariance();
		}
	  }
	  
	  private double getSimilarity(SimilarityQuery<V> sq, V o1, V o2){
				double res = 0;
				if(o1 == null || o2 == null)
					return Double.MAX_VALUE;
				if(o1.getDimensionality() == o2.getDimensionality()){
					for(int i=0;i<o1.getDimensionality();i++){
						res = res + Math.pow(o1.doubleValue(i) - o2.doubleValue(i), 2);
					}
				}
				return Math.sqrt(res);
		  //return HellingerDistanceFunction.STATIC.distance(o1, o2);
		  //return sq.similarity(o1, o2);
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
	  public static class Parameterizer<V extends NumberVector> extends AbstractParameterizer {
	    /**
	     * Parameter for the kernel function.
	     */
	    public static final OptionID KERNEL_FUNCTION_ID = new OptionID("abod.kernelfunction", "Kernel function to use.");

	    /**
	     * Distance function.
	     */
	    protected SimilarityFunction<V> kernelFunction = null;

	    @Override
	    protected void makeOptions(Parameterization config) {
	      super.makeOptions(config);
	      final ObjectParameter<SimilarityFunction<V>> param = new ObjectParameter<>(KERNEL_FUNCTION_ID, SimilarityFunction.class, PolynomialKernelFunction.class);
	      if(config.grab(param)) {
	        kernelFunction = param.instantiateClass(config);
	      }
	    }

	    @Override
	    protected ABOD<V> makeInstance() {
	      return new ABOD<>(kernelFunction);
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
		return "abod";
	}
	
	
	
}
