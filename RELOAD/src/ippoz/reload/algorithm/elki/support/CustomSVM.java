/**
 * 
 */
package ippoz.reload.algorithm.elki.support;

import ippoz.reload.algorithm.elki.ELKIAlgorithm;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;

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

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_print_interface;
import libsvm.svm_problem;
import de.lmu.ifi.dbs.elki.algorithm.AbstractAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.outlier.OutlierAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.outlier.svm.LibSVMOneClassOutlierDetection;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDoubleDataStore;
import de.lmu.ifi.dbs.elki.database.ids.ArrayDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.relation.DoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.MaterializedDoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.database.relation.RelationUtil;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.math.DoubleMinMax;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.result.outlier.BasicOutlierScoreMeta;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierScoreMeta;
import de.lmu.ifi.dbs.elki.utilities.exceptions.AbortException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.EnumParameter;

/**
 * @author Tommy
 *
 */
public class CustomSVM extends AbstractAlgorithm<OutlierResult> implements OutlierAlgorithm, ELKIAlgorithm<NumberVector> {
	  /**
	   * Class logger.
	   */
	  private static final Logging LOG = Logging.getLogger(LibSVMOneClassOutlierDetection.class);

	  /**
	   * Kernel functions. Expose as enum for convenience.
	   * 
	   * @apiviz.exclude
	   */
	  public static enum SVMKernel { //
	    LINEAR, // Linear
	    QUADRATIC, // Quadratic
	    CUBIC, // Cubic
	    RBF, // Radial basis functions
	    SIGMOID, // Sigmoid
	  }

	  /**
	   * Kernel function in use.
	   */
	  protected SVMKernel kernel = SVMKernel.RBF;
	  
	  private double nu;
	  
	  private svm_model model;
	  
	  private double minScore;
	  
	  private double maxScore;
	  
	  private List<SVMScore> scoresList;
	  
	  /**
	   * Constructor.
	   * 
	   * @param kernel Kernel to use with SVM.
	   */
	  public CustomSVM(SVMKernel kernel, double nu) {
	    super();
	    this.kernel = kernel;
	    this.nu = nu;
	  }
	  
	  private svm_parameter getParameter(int dim){
		  svm_parameter param = new svm_parameter();
		    param.svm_type = svm_parameter.ONE_CLASS;
		    param.kernel_type = svm_parameter.LINEAR;
		    param.degree = 3;
		    switch(kernel){
		    case LINEAR:
		      param.kernel_type = svm_parameter.LINEAR;
		      break;
		    case QUADRATIC:
		      param.kernel_type = svm_parameter.POLY;
		      param.degree = 2;
		      break;
		    case CUBIC:
		      param.kernel_type = svm_parameter.POLY;
		      param.degree = 3;
		      break;
		    case RBF:
		      param.kernel_type = svm_parameter.RBF;
		      break;
		    case SIGMOID:
		      param.kernel_type = svm_parameter.SIGMOID;
		      break;
		    default:
		      throw new AbortException("Invalid kernel parameter: " + kernel);
		    }
		    param.nu = nu;
		    param.coef0 = 0.;
		    param.cache_size = 100;
		    param.C = 1e2;
		    param.eps = 1e-4; // not used by one-class?
		    param.p = 0.1; // not used by one-class?
		    param.shrinking = 0;
		    param.probability = 0;
		    param.nr_weight = 0;
		    param.weight_label = new int[0];
		    param.weight = new double[0];
		    param.gamma = 1e-4 / dim;
		    return param;
	  }

	  /**
	   * Run one-class SVM.
	   * 
	   * @param relation Data relation
	   * @return Outlier result.
	   */
	  public OutlierResult run(Relation<NumberVector> relation) {
	    final int dim = RelationUtil.dimensionality(relation);
	    final ArrayDBIDs ids = DBIDUtil.ensureArray(relation.getDBIDs());
	    scoresList = new LinkedList<SVMScore>();

	    svm.svm_set_print_string_function(LOG_HELPER);

	    svm_parameter param = getParameter(dim);
	    
	    // Transform data:
	    svm_problem prob = new svm_problem();
	    prob.l = relation.size();
	    prob.x = new svm_node[prob.l][];
	    prob.y = new double[prob.l];
	    {
	      DBIDIter iter = ids.iter();
	      for(int i = 0; i < prob.l && iter.valid(); iter.advance(), i++) {
	    	  NumberVector vec = relation.get(iter);
	        // TODO: support compact sparse vectors, too!
	        svm_node[] x = new svm_node[dim];
	        for(int d = 0; d < dim; d++) {
	          x[d] = new svm_node();
	          x[d].index = d + 1;
	          x[d].value = vec.doubleValue(d);
	        }
	        prob.x[i] = x;
	        prob.y[i] = +1;
	      }
	    }

	    svm.svm_check_parameter(prob, param);
	    model = svm.svm_train(prob, param);

	    WritableDoubleDataStore scores = DataStoreUtil.makeDoubleStorage(relation.getDBIDs(), DataStoreFactory.HINT_DB);
	    DoubleMinMax mm = new DoubleMinMax();
	      
	    DBIDIter iter = ids.iter();
	      double[] buf = new double[svm.svm_get_nr_class(model)];
	      for(int i = 0; i < prob.l && iter.valid(); iter.advance(), i++) {
	    	NumberVector vec = relation.get(iter);
	        svm_node[] x = new svm_node[dim];
	        for(int d = 0; d < dim; d++) {
	          x[d] = new svm_node();
	          x[d].index = d + 1;
	          x[d].value = vec.doubleValue(d);
	        }
	        svm.svm_predict_values(model, x, buf);
	        double score = -buf[0] / param.gamma; // Heuristic rescaling, sorry.
	        // Unfortunately, libsvm one-class currently yields a binary decision.
	        scores.putDouble(iter, score);
	        mm.put(score);
	        scoresList.add(new SVMScore(vec, score));
	      }
	      minScore = mm.getMin();
	      maxScore = mm.getMax();
	      
	    DoubleRelation scoreResult = new MaterializedDoubleRelation("One-Class SVM Decision", "svm-outlier", scores, ids);
	    OutlierScoreMeta scoreMeta = new BasicOutlierScoreMeta(mm.getMin(), mm.getMax(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.);
	    return new OutlierResult(scoreMeta, scoreResult);
	  }
	  
	  public double calculateSVM(NumberVector newInstance){
		  	int dim = newInstance.getDimensionality(); 
			double[] buf = new double[svm.svm_get_nr_class(model)];
			svm_node[] x = new svm_node[dim];
			for(int d = 0; d < dim; d++) {
			  x[d] = new svm_node();
			  x[d].index = d + 1;
			  x[d].value = newInstance.doubleValue(d);
			}
			svm.svm_predict_values(model, x, buf);
			// Heuristic rescaling, sorry.
			return -buf[0] / model.param.gamma; 
	  }
	  
	  public boolean evaluateSVM(NumberVector newInstance){
		  double score = calculateSVM(newInstance);
		  return score < minScore || score > maxScore;
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
	   * Setup logging helper for SVM.
	   */
	  static final svm_print_interface LOG_HELPER = new svm_print_interface() {
	    @Override
	    public void print(String arg0) {
	      if(LOG.isVerbose()) {
	        LOG.verbose(arg0);
	      }
	    }
	  };

	  /**
	   * Parameterization class.
	   * 
	   * @author Erich Schubert
	   * 
	   * @apiviz.exclude
	   * 
	   * @param <V> Vector type
	   */
	  public static class Parameterizer extends AbstractParameterizer {
	    /**
	     * Parameter for kernel function.
	     */
	    private static final OptionID KERNEL_ID = new OptionID("svm.kernel", "Kernel to use with SVM.");

	    /**
	     * Kernel in use.
	     */
	    protected SVMKernel kernel = SVMKernel.RBF;
	    
	    protected double mynu = 0.05;

	    @Override
	    protected void makeOptions(Parameterization config) {
	      super.makeOptions(config);

	      EnumParameter<SVMKernel> kernelP = new EnumParameter<>(KERNEL_ID, SVMKernel.class, SVMKernel.RBF);
	      if(config.grab(kernelP)) {
	        kernel = kernelP.getValue();
	      }
	    }

	    @Override
	    protected CustomSVM makeInstance() {
	      return new CustomSVM(kernel, mynu);
	    }
	  }
	  
	  private static final String SVMM_NR_CLASS = "SVMM_NR_CLASS"; 
	  
	  private static final String SVMM_L = "SVMM_L"; 
	  
	  private static final String SVMM_SV = "SVMM_SV"; 
	  
	  private static final String SVMM_SV_COEF = "SVMM_SV_COEF"; 
	  
	  private static final String SVMM_RHO = "SVMM_RHO"; 
	  
	  private static final String SVMM_PROBA = "SVMM_PROBA";
	  
	  private static final String SVMM_PROBB = "SVMM_PROBB";
	  
	  private static final String SVMM_SV_INDICES = "SVMM_SV_INDICES";
	  
	  private static final String SVMM_LABEL = "SVMM_LABEL";
	  
	  private static final String SVMM_NSV = "SVMM_NSV";
	  
	  private static final String SVMM_DIM = "SVMM_DIM";
	  
	  private static final String MINSCORE = "MINSCORE";
	  
	  private static final String MAXSCORE = "MAXSCORE";
	  
	  private void loadParametersFile(File file){
		  PreferencesManager pManager;
			try {
				if(file.exists()){
					pManager = new PreferencesManager(file);
					if(pManager.isValidFile()){
						
						model = new svm_model();

						if(pManager.hasPreference(SVMM_DIM) && AppUtility.isNumber(pManager.getPreference(SVMM_NR_CLASS)))
							model.param = getParameter(Integer.parseInt(pManager.getPreference(SVMM_DIM)));
						else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing or wrong preference: " + SVMM_DIM);
						
						
						if(pManager.hasPreference(SVMM_NR_CLASS) && AppUtility.isNumber(pManager.getPreference(SVMM_NR_CLASS)))
							model.nr_class = Integer.parseInt(pManager.getPreference(SVMM_NR_CLASS));
						else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing or wrong preference: " + SVMM_NR_CLASS);
						
						if(pManager.hasPreference(SVMM_L) && AppUtility.isNumber(pManager.getPreference(SVMM_L)))
							model.l = Integer.parseInt(pManager.getPreference(SVMM_L));
						else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing or wrong preference: " + SVMM_L);
						
						if(pManager.hasPreference(SVMM_SV)){
							String pref = pManager.getPreference(SVMM_SV).trim();
							String[] pSplit = pref.split(";");
							model.SV = new svm_node[pSplit.length][];
							for(int i=0;i<pSplit.length;i++){
								String row = pSplit[i].trim();
								String[] ppSplit = row.split(",");
								model.SV[i] = new svm_node[ppSplit.length];
								for(int j=0;j<ppSplit.length;j++){
									model.SV[i][j] = new svm_node();
									model.SV[i][j].index = Integer.parseInt(ppSplit[j].trim().split("&")[0]);
									model.SV[i][j].value = Double.parseDouble(ppSplit[j].trim().split("&")[1]);
								}
							}
						} else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing preference: " + SVMM_SV);
						
						if(pManager.hasPreference(SVMM_SV_COEF)){
							String pref = pManager.getPreference(SVMM_SV_COEF).trim();
							String[] pSplit = pref.split(";");
							model.sv_coef = new double[pSplit.length][];
							for(int i=0;i<pSplit.length;i++){
								model.sv_coef[i] = stringToDoubleArray(pSplit[i], ",");
							}
						} else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing preference: " + SVMM_SV_COEF);
						
						
						if(pManager.hasPreference(SVMM_RHO))
							model.rho = stringToDoubleArray(pManager.getPreference(SVMM_RHO), ",");
						else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing preference: " + SVMM_RHO);
						
						if(pManager.hasPreference(SVMM_PROBA))
							model.probA = stringToDoubleArray(pManager.getPreference(SVMM_PROBA), ",");
						else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing preference: " + SVMM_PROBA);
						
						if(pManager.hasPreference(SVMM_PROBB))
							model.probB = stringToDoubleArray(pManager.getPreference(SVMM_PROBB), ",");
						else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing preference: " + SVMM_PROBB);
						
						if(pManager.hasPreference(SVMM_SV_INDICES))
							model.sv_indices = stringToIntArray(pManager.getPreference(SVMM_SV_INDICES), ",");
						else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing or wrong preference: " + SVMM_SV_INDICES);
						
						if(pManager.hasPreference(SVMM_LABEL))
							model.label = stringToIntArray(pManager.getPreference(SVMM_LABEL), ",");
						else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing or wrong preference: " + SVMM_LABEL);
						
						if(pManager.hasPreference(SVMM_NSV))
							model.nSV = stringToIntArray(pManager.getPreference(SVMM_NSV), ",");
						else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing or wrong preference: " + SVMM_NSV);
						
						if(pManager.hasPreference(MINSCORE) && AppUtility.isNumber(pManager.getPreference(MINSCORE)))
							minScore = Double.valueOf(pManager.getPreference(MINSCORE));
						else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing or wrong preference: " + MINSCORE);
						
						if(pManager.hasPreference(MAXSCORE) && AppUtility.isNumber(pManager.getPreference(MAXSCORE)))
							maxScore = Double.valueOf(pManager.getPreference(MAXSCORE));
						else AppLogger.logError(getClass(), "MissingPreferenceError", "Missing or wrong preference: " + MAXSCORE);
												
					} else AppLogger.logError(getClass(), "SVMFileError", "Unable to find the '" + file.getName() + "' SVM preference file");
				}
			} catch (Exception ex) {
				AppLogger.logException(getClass(), ex, "Unable to read SVM file");
			} 
	  }
	  
	  private void loadScoresFile(File file){
		  BufferedReader reader;
			String readed;
			try {
				if(file.exists()){
					scoresList = new LinkedList<SVMScore>();
					reader = new BufferedReader(new FileReader(file));
					reader.readLine();
					while(reader.ready()){
						readed = reader.readLine();
						if(readed != null){
							readed = readed.trim();
							if(readed.length() > 0 && readed.split(";").length >= 2 && AppUtility.isNumber(readed.split(";")[1]))
								scoresList.add(new SVMScore(stringToVector(readed.split(";")[0]), Double.parseDouble(readed.split(";")[1])));
						}
					}
					reader.close();
				}
			} catch (IOException ex) {
				AppLogger.logException(getClass(), ex, "Unable to read SVM Scores file");
			} 
	
	  }
	  
	  private Vector stringToVector(String string) {
		  string = string.replace("{", "").replace("}", "");
		  String[] sSplitted;
		  if(string.contains(","))
			  sSplitted = string.trim().split(",");
		  else sSplitted = string.trim().split(" ");
		  Vector vec = new Vector(sSplitted.length);
		  for(int i=0; i<sSplitted.length;i++){
			  vec.set(i, Double.parseDouble(sSplitted[i].trim()));
		  }
		  return vec;
	}

	public void loadFile(String item) {
			loadParametersFile(new File(item));
			loadScoresFile(new File(item + "scores"));
		}
	  
	  private int[] stringToIntArray(String s, String sep){
	  		int i = 0;
	  		int[] arr;
	  		if(s != null && s.contains(sep)){
	  			arr = new int[s.split(sep).length];
	  			for(String substr : s.split(sep)){
	  				if(AppUtility.isNumber(substr.trim())){
	  					arr[i] = Integer.valueOf(substr.trim());
	  				}
	  				i++;
	  			}
	  			return arr;
	  		} else if(s != null && AppUtility.isNumber(s.trim())){
	  			return new int[]{Integer.valueOf(s.trim())};
	  		} else return null;
	  	}
	  
	  	private double[] stringToDoubleArray(String s, String sep){
	  		int i = 0;
	  		double[] arr;
	  		if(s != null && s.contains(sep)){
	  			arr = new double[s.split(sep).length];
	  			for(String substr : s.split(sep)){
	  				if(AppUtility.isNumber(substr.trim())){
	  					arr[i] = Double.valueOf(substr.trim());
	  				}
	  				i++;
	  			}
	  			return arr;
	  		} else if(s != null && AppUtility.isNumber(s.trim())){
	  			return new double[]{Double.valueOf(s.trim())};
	  		} else return null;
	  	}

		private String doubleArrayToString(double[] array, char sep){
			String res = "";
			if(array == null)
				return res;
			for(double d : array){
				res = res + d + sep;
			}
			return res.substring(0,  res.length()-1);
		}
		
		private String intArrayToString(int[] array, char sep){
			String res = "";
			if(array == null)
				return res;
			for(int d : array){
				res = res + d + sep;
			}
			return res.substring(0,  res.length()-1);
		}
		
		private void printScoresFile(File file){
			BufferedWriter writer;
			try {
				if(file.exists())
					file.delete();
				
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("* Preferences for SVM Model.");
				
				writer.write("data (enclosed in {});svm score");
				
				for(SVMScore score : scoresList){
					writer.write("{" + score.getVector().toString() + "};" + score.getScore() + "\n");
				}
				writer.write("\n");
				
				writer.close();
			} catch (IOException ex) {
				AppLogger.logException(getClass(), ex, "Unable to write SVM file");
			} 
		}
		
		private void printParametersFile(File file){
			BufferedWriter writer;
			String partial;
			try {
				if(file.exists())
					file.delete();
				
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("* Preferences for SVM Model.");
				
				writer.write("\n" + SVMM_DIM + " = " + model.SV[0].length + "\n");
				writer.write("\n" + SVMM_NR_CLASS + " = " + model.nr_class + "\n");
				writer.write("\n" + SVMM_L + " = " + model.l + "\n");
				
				partial = "";
				for(svm_node[] arr : model.SV){
					for(svm_node node : arr){
						partial = partial + node.index + "&" + node.value + ',';
					}
					partial = partial.substring(0,  partial.length()-1) + ";";
				}
				writer.write("\n" + SVMM_SV + " = " + partial.substring(0,  partial.length()-1) + "\n");
				
				partial = "";
				for(double[] arr : model.sv_coef){
					partial = partial + doubleArrayToString(arr, ',') + ";";
				}
				writer.write("\n" + SVMM_SV_COEF + " = " + partial.substring(0,  partial.length()-1) + "\n");
				
				writer.write("\n" + SVMM_RHO + " = " + doubleArrayToString(model.rho, ',') + "\n");
				writer.write("\n" + SVMM_PROBA + " = " + doubleArrayToString(model.probA, ',') + "\n");
				writer.write("\n" + SVMM_PROBB + " = " + doubleArrayToString(model.probB, ',') + "\n");
				writer.write("\n" + SVMM_SV_INDICES + " = " + intArrayToString(model.sv_indices, ',') + "\n");
				writer.write("\n" + SVMM_LABEL + " = " + intArrayToString(model.label, ',') + "\n");
				writer.write("\n" + SVMM_NSV + " = " + intArrayToString(model.nSV, ',') + "\n");
				
				writer.write("\n" + MINSCORE + " = " + minScore + "\n");
				writer.write("\n" + MAXSCORE + " = " + maxScore + "\n");
				
				writer.close();
			} catch (IOException ex) {
				AppLogger.logException(getClass(), ex, "Unable to write SVM file");
			} 
		}
		
		public void printFile(File file) {
			printParametersFile(file);
			printScoresFile(new File(file.getPath() + "scores"));
		}

		@Override
		public List<Double> getScoresList() {
			List<Double> list = new ArrayList<Double>(scoresList.size());
			for(SVMScore score : scoresList){
				list.add(score.getScore());
			}
			Collections.sort(list);
			return list;
		}

		@Override
		public String getAlgorithmName() {
			return "svm";
		}

		@Override
		public Object run(Database db, Relation<NumberVector> relation) {
			return run(relation);
		}
		
		public class SVMScore {
			
			private NumberVector vec;
			
			private double score;

			public SVMScore(NumberVector vec, double score) {
				this.vec = vec;
				this.score = score;
			}

			public NumberVector getVector() {
				return vec;
			}

			public double getScore() {
				return score;
			}		
			
		}
}