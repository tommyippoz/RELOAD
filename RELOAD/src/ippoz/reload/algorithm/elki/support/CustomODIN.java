/**
 * 
 */
package ippoz.reload.algorithm.elki.support;

import ippoz.reload.algorithm.elki.ELKIAlgorithm;
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
import java.util.List;

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
import de.lmu.ifi.dbs.elki.utilities.Alias;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;
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
 * Added on: Fall 2018
 */

/**
 * Outlier detection based on the in-degree of the kNN graph.
 *
 * This is a curried version: instead of using a threshold T to obtain a binary
 * decision, we use the computed value as outlier score; normalized by k to make
 * the numbers more comparable across different parameterizations.
 *
 * Reference:
 * <p>
 * V. Hautamäki and I. Kärkkäinen and P. Fränti<br />
 * Outlier detection using k-nearest neighbour graph<br />
 * Proc. 17th Int. Conf. Pattern Recognition, ICPR 2004
 * </p>
 *
 * @author Erich Schubert
 * @since 0.6.0
 *
 * @param <O> Object type
 */
@Reference(authors = "V. Hautamäki and I. Kärkkäinen and P. Fränti", //
title = "Outlier detection using k-nearest neighbour graph", //
booktitle = "Proc. 17th Int. Conf. Pattern Recognition, ICPR 2004", //
url = "http://dx.doi.org/10.1109/ICPR.2004.1334558")
@Alias({ "de.lmu.ifi.dbs.elki.algorithm.outlier.ODIN" })

public class CustomODIN extends AbstractDistanceBasedAlgorithm<NumberVector, OutlierResult> implements OutlierAlgorithm, ELKIAlgorithm<NumberVector> {
	/**
	 * Class logger.
	 */
	private static final Logging LOG = Logging.getLogger(ODIN.class);

	/**
	 * Number of neighbors for kNN graph.
	 */
	int k;

	//private List<ODINScore> resList;
	
	private ELKIEuclid<ODINScore> treeList;

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
		WritableDoubleDataStore distances = DataStoreUtil.makeDoubleStorage(ids, DataStoreFactory.HINT_DB, 0.);
		
		double inc = 1. / (k - 1);
		double min = Double.POSITIVE_INFINITY, max = 0.0;
		// Process all objects
		for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
			// Find the nearest neighbors (using an index, if available!)
			DBIDs neighbors = knnq.getKNNForDBID(iter, k);
			double maxDist = -1;
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
				
				double instDist = dq.distance(iter, nei);
				if(instDist > maxDist)
					maxDist = instDist;
				
			}
			distances.put(iter, maxDist);
		}

		treeList = new ELKIEuclid<CustomODIN.ODINScore>();
		ids = relation.getDBIDs();
		for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
			ODINScore os = new ODINScore(database.getBundle(iter), scores.doubleValue(iter), distances.doubleValue(iter));
			treeList.addPoint(os.getPoint(), os);
		}

		// Wrap the result and add metadata.
		OutlierScoreMeta meta = new InvertedOutlierScoreMeta(min, max, 0., inc * (ids.size() - 1), 1);
		DoubleRelation rel = new MaterializedDoubleRelation("ODIN In-Degree", "odin", scores, ids);
		return new OutlierResult(meta, rel);
	}

	public double calculateSingleODIN(NumberVector newInstance) {
		double partialResult;
		if(treeList == null || treeList.size() == 0) 
			return Double.MAX_VALUE;
		else if(Double.isFinite(partialResult = hasResult(newInstance)))
			return partialResult;
		else {
			double odin = 0;
			double inc = 1. / (k - 1);
			List<ODINScore> extList = new ArrayList<ODINScore>(treeList.listItems());
			extList.add(new ODINScore(newInstance, 0.0));
			for(ODINScore os : treeList.listItems()){
				if(!os.getVector().equals(newInstance) && isKNN(newInstance, os))
					odin = odin + inc;
			}
			return odin;			
		}
	}
	
	private boolean isKNN(NumberVector toCheck, ODINScore os){
		DistanceQuery<NumberVector> sq = getDistanceFunction().instantiate(null);
		double refDist = getSimilarity(sq, toCheck, os.getVector());
		return refDist <= os.getDistanceToKthNeighbour();
	} 

	private double getSimilarity(DistanceQuery<NumberVector> sq, NumberVector arg0, NumberVector arg1) {
		return sq.distance(arg0, arg1);
	}

	private double hasResult(NumberVector newInstance){
		for(ODINScore ar : treeList.listItems()){
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
	
	public static Vector extractVector(SingleObjectBundle bundle){
		double[] bValues = ((DoubleVector)bundle.data(1)).getValues();
		Vector data = new Vector(bValues.length);
		for(int i=0;i<data.getDimensionality();i++){
			((Vector)data).set(i, bValues[i]);
		}
		return data;
	}

	private class ODINScore implements Comparable<ODINScore> {

		private NumberVector data;

		private double odin;
		
		private double distanceToKthNeighbour;

		public ODINScore(SingleObjectBundle bundle, double odin, double distanceToKthNeighbour) {
			this.odin = odin;
			this.distanceToKthNeighbour = distanceToKthNeighbour;
			double[] bValues = ((DoubleVector)bundle.data(1)).getValues();
			data = new Vector(bValues.length);
			for(int i=0;i<data.getDimensionality();i++){
				((Vector)data).set(i, bValues[i]);
			}
		}
	
		public double[] getPoint(){
			double[] point = new double[data.getDimensionality()];
			for(int i=0;i<point.length;i++){
				point[i] = data.doubleValue(i);
			}
			return point;
		}

		public ODINScore(String vString, String odin, String distK) {
			this.odin = Double.parseDouble(odin);
			this.distanceToKthNeighbour = Double.parseDouble(distK);
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
		
		public double getDistanceToKthNeighbour(){
			return distanceToKthNeighbour;
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

	public double getDbSize() {
		if(treeList != null)
			return treeList.size();
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
			treeList = new ELKIEuclid<CustomODIN.ODINScore>();
			if(new File(item).exists()){
				reader = new BufferedReader(new FileReader(new File(item)));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						ODINScore os = new ODINScore(readed.split(";")[0].replace("{", "").replace("}",  ""), readed.split(";")[2], readed.split(";")[3]);
						treeList.addPoint(os.getPoint(), os);
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
				writer.write("data (enclosed in {});k;odin;distanceToKthNeighbour\n");
				for(ODINScore ar : treeList.listItems()){
					writer.write("{" + ar.getVector().toString() + "};" + (k-1) + ";" + ar.getODIN() + ";" + ar.getDistanceToKthNeighbour() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write ODIB file");
		} 
	}

	public int size() {
		return treeList.size();
	}
	
	@Override
	public List<Double> getScoresList() {
		ArrayList<Double> list = new ArrayList<Double>(size());
		for(ODINScore os : treeList.listItems()){
			list.add(os.getODIN());
		}
		Collections.sort(list);
		return list;
	}

	@Override
	public String getAlgorithmName() {
		return "odin";
	}
}
