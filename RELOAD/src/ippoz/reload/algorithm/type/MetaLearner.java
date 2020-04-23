/**
 * 
 */
package ippoz.reload.algorithm.type;

import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.meta.MetaLearnerType;

/**
 * @author Tommy
 *
 */
public class MetaLearner extends LearnerType {
	
	private MetaLearnerType mlType;
	
	private AlgorithmType[] atList;

	public MetaLearner(MetaLearnerType mlType, AlgorithmType[] atList) {
		this.mlType = mlType;
		this.atList = atList;
	}

	public MetaLearnerType getMetaType() {
		return mlType;
	}

	public AlgorithmType[] getBaseLearners() {
		return atList;
	}	

}
