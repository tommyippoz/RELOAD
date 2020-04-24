/**
 * 
 */
package ippoz.reload.algorithm.type;

import java.util.Map;

import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.meta.MetaLearnerType;

/**
 * @author Tommy
 *
 */
public class BaseLearner extends LearnerType {
	
	private AlgorithmType algType;

	public BaseLearner(AlgorithmType algType) {
		super();
		this.algType = algType;
	}
	
	public BaseLearner(AlgorithmType algType, Map<String, String> learnerPreferences) {
		super(learnerPreferences);
		this.algType = algType;
	}

	public AlgorithmType getAlgType() {
		return algType;
	}

	@Override
	public String toString() {
		if(algType != null)
			return algType.toString();
		else return "";
	}

	@Override
	public int compareTo(LearnerType other) {
		if(other != null && other instanceof BaseLearner &&((BaseLearner)other).getAlgType() == algType)
			return 0;
		else return -1;
	}	
	
	public MetaLearner toMeta(MetaLearnerType mlt){
		return new MetaLearner(mlt, new AlgorithmType[]{getAlgType()});
	}

	@Override
	public LearnerType clone() {
		return new BaseLearner(getAlgType(), learnerPreferences);
	}

}
