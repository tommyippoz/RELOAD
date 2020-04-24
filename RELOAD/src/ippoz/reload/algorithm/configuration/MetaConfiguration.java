/**
 * 
 */
package ippoz.reload.algorithm.configuration;

import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.meta.MetaData;
import ippoz.reload.meta.MetaLearnerType;

/**
 * The Class MetaConfiguration.
 *
 * @author Tommy
 */
public class MetaConfiguration extends BasicConfiguration {
	
	private MetaLearner mlType;

	public MetaConfiguration(LearnerType learnerType) {
		super();
		if(learnerType instanceof MetaLearner)
			this.mlType = (MetaLearner)learnerType;
		else mlType = null;
	}
	
	/**
	 * Gets the algorithm type.
	 *
	 * @return the algType
	 */
	public MetaLearnerType getMetaType(){
		return mlType.getMetaType();
	}
	
	@Override
	public String toString() {
		return mlType + ":" + super.toString();
	}

	@Override
	public LearnerType getLearnerType() {
		return mlType;
	}

	public MetaData generateMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

}
