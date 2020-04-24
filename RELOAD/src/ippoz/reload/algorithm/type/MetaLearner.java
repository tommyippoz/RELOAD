/**
 * 
 */
package ippoz.reload.algorithm.type;

import java.util.Arrays;

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
	
	@Override
	public String toString() {
		String toString = "";
		if(mlType != null){
			toString = toString + mlType.toString();
			if(atList != null && atList.length > 0)
				toString = toString + Arrays.toString(atList);
		}
		return toString;
	}	

}
