/**
 * 
 */
package ippoz.reload.algorithm.configuration;

import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.meta.MetaLearnerType;

/**
 * The Class MetaConfiguration.
 *
 * @author Tommy
 */
public class MetaConfiguration extends BasicConfiguration {
	
	private MetaLearnerType mlType;

	public MetaConfiguration(MetaLearnerType mlType) {
		super();
		this.mlType = mlType;
	}
	
	/**
	 * Gets the algorithm type.
	 *
	 * @return the algType
	 */
	public MetaLearnerType getMetaType(){
		return mlType;
	}
	
	@Override
	public String toString() {
		return mlType + ":" + super.toString();
	}

	@Override
	public LearnerType getLearnerType() {
		return new MetaLearner(mlType, null);
	}

}
