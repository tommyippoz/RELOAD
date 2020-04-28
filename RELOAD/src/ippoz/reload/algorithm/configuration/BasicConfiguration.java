/**
 * 
 */
package ippoz.reload.algorithm.configuration;

import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.metric.Metric;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The Class AlgorithmConfiguration. Basic Configuration for the involved
 * Algorithms.
 *
 * @author Tommy
 */
public abstract class BasicConfiguration implements Cloneable {

	/** The Constant WEIGHT. */
	public static final String WEIGHT = "weight";

	/** The Constant AVG_SCORE. */
	public static final String AVG_SCORE = "metric_avg_score";

	/** The Constant STD_SCORE. */
	public static final String STD_SCORE = "metric_std_score";

	public static final String AUC_SCORE = "auc_score";

	public static final String INVARIANT = "invariant";

	public static final String DETAIL = "detail";

	public static final String PEARSON_TOLERANCE = "pi_tolerance";

	public static final String PEARSON_WINDOW = "pi_window";

	public static final String SLIDING_WINDOW_SIZE = "sliding_window_size";

	public static final String TRAIN_Q0 = "train_q0";

	public static final String TRAIN_Q1 = "train_q1";

	public static final String TRAIN_Q2 = "train_q2";

	public static final String TRAIN_Q3 = "train_q3";

	public static final String TRAIN_Q4 = "train_q4";

	public static final String TRAIN_STD = "train_std";

	public static final String TRAIN_AVG = "train_avg";

	public static final String SLIDING_POLICY = "sliding_policy";

	public static final String THRESHOLD = "threshold";

	public static final String DATASET_NAME = "dataset";

	public static final String ANOMALY_AVG = "anomaly_avg";

	public static final String ANOMALY_STD = "anomaly_std";

	public static final String ANOMALY_MED = "anomaly_med";

	public static final String DATASERIES = "data_series";

	public static final String K_FOLD = "k_fold";

	public static final String METRIC = "metric";

	public static final String REPUTATION = "reputation";

	/** The configuration map. */
	private Map<String, Object> confMap;

	/**
	 * Instantiates a new algorithm configuration.
	 */
	public BasicConfiguration() {
		confMap = new HashMap<String, Object>();
	}

	public BasicConfiguration(Map<String, Object> confMap) {
		this.confMap = confMap;
	}

	private void setMap(Map<String, Object> newMap) {
		confMap = newMap;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Map<String, Object> newMap = new HashMap<String, Object>();
		BasicConfiguration newConf = null;
		try {
			if (this instanceof AlgorithmConfiguration){
				newConf = new AlgorithmConfiguration(((AlgorithmConfiguration) this).getAlgorithmType(), confMap);
			} else {
				MetaConfiguration mConf = (MetaConfiguration)this;
				newConf = new MetaConfiguration(mConf.getLearnerType(), confMap, mConf.getConfigurations());
			}
			for(String key : confMap.keySet()){
				newMap.put(key, confMap.get(key));
			}
			newConf.setMap(newMap);
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex,
					"Unable to clone configuration");
		}
		return newConf;
	}

	public void removeItem(String tag) {
		if (hasItem(tag))
			confMap.remove(tag);
	}

	/**
	 * Adds an item.
	 *
	 * @param item
	 *            the item tag
	 * @param value
	 *            the itam value
	 */
	public void addItem(String item, String value) {
		confMap.put(item.trim(), value.trim());
	}

	/**
	 * Adds a raw item.
	 *
	 * @param item
	 *            the item tag
	 * @param value
	 *            the item value
	 */
	public void addRawItem(String item, Object value) {
		confMap.put(item.trim(), value);
	}

	/**
	 * Gets the item.
	 *
	 * @param tag
	 *            the item tag
	 * @return the item
	 */
	public String getItem(String tag) {
		return getItem(tag.trim(), true);
	}

	/**
	 * Gets the item.
	 *
	 * @param tag
	 *            the item tag
	 * @return the item
	 */
	public String getItem(String tag, boolean flag) {
		if (!confMap.containsKey(tag.trim())) {
			if (flag && !tag.equals("weight"))
				AppLogger.logInfo(getClass(), "Unable to find tag '" + tag
						+ "'");
			return null;
		} else
			return confMap.get(tag).toString();
	}

	/**
	 * Gets the raw item.
	 *
	 * @param tag
	 *            the item tag
	 * @return the item
	 */
	public Object getRawItem(String tag) {
		return getRawItem(tag.trim(), true);
	}

	/**
	 * Gets the raw item.
	 *
	 * @param tag
	 *            the item tag
	 * @return the item
	 */
	public Object getRawItem(String tag, boolean flag) {
		if (!confMap.containsKey(tag.trim())) {
			if (flag)
				AppLogger.logInfo(getClass(), "Unable to find tag '" + tag
						+ "'");
			return null;
		} else
			return confMap.get(tag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + getSpecificItems() + "]";
	}

	/**
	 * Converts to a file row.
	 *
	 * @param complete
	 *            the complete flag, defines if the description is extended or
	 *            not.
	 * @return the file row string
	 */
	public String toFileRow(boolean complete) {
		if (complete)
			return getItem(WEIGHT, false) + ", " + getItem(AVG_SCORE, false)
					+ ", " + getItem(STD_SCORE, false) + ", "
					+ getItem(DATASET_NAME, false) + ", " + getSpecificItems();
		else
			return getSpecificItems();
	}

	private String getSpecificItems() {
		String all = "";
		for (String itemTag : confMap.keySet()) {
			if (!itemTag.equals(AVG_SCORE) && !itemTag.equals(STD_SCORE)
					&& !itemTag.equals(WEIGHT) && !itemTag.equals(DATASET_NAME)) {
				all = all + itemTag + "="
						+ getRawItem(itemTag, false).toString() + "&";
			}
		}
		return all;
	}

	public static BasicConfiguration buildConfiguration(LearnerType lType,
			String descRow) {
		String tag, value;
		BasicConfiguration conf = null;
		if (lType instanceof MetaLearner)
			conf = new MetaConfiguration((MetaLearner) lType);
		else
			conf = new AlgorithmConfiguration(
					((BaseLearner) lType).getAlgType());
		if (descRow != null) {
			for (String splitted : descRow.split("&")) {
				if (splitted.contains("=")) {
					tag = splitted.split("=")[0].trim();
					value = splitted.split("=")[1].trim();
					conf.addItem(tag, value);
				}
			}
		}
		return conf;
	}

	public boolean hasItem(String tag) {
		return confMap != null && confMap.containsKey(tag.trim());
	}

	public int getSlidingWindowSize() {
		if (confMap.containsKey(SLIDING_WINDOW_SIZE)) {
			return Integer.parseInt(getItem(SLIDING_WINDOW_SIZE));
		} else
			return -1;
	}

	public void addItem(String tag, double doubleValue) {
		addItem(tag, String.valueOf(doubleValue));
	}

	public void addItem(String tag, Integer intValue) {
		addItem(tag, String.valueOf(intValue));
	}

	public Set<String> listLabels() {
		return confMap.keySet();
	}

	public abstract LearnerType getLearnerType();

	public static BasicConfiguration buildConfiguration(LearnerType algType) {
		if (algType != null) {
			if (algType instanceof BaseLearner)
				return new AlgorithmConfiguration(
						((BaseLearner) algType).getAlgType());
			else
				return new MetaConfiguration(algType);
		} else
			return null;
	}

	public Map<String, Object> getConfMap() {
		return confMap;
	}

}
