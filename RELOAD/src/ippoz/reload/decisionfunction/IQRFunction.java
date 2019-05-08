/**
 * 
 */
package ippoz.reload.decisionfunction;

import java.text.DecimalFormat;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class IQRFunction extends DecisionFunction {
	
	protected double q1;
	
	protected double q3;
	
	protected double ratio;

	protected IQRFunction(double ratio, double q1, double q3) {
		super("IQR", DecisionFunctionType.IQR);
		this.ratio = ratio;
		if(q1 <= q3) {
			this.q1 = q1;
			this.q3 = q3;
		} else {
			this.q1 = q3;
			this.q3 = q1;
		}
	}

	@Override
	protected AnomalyResult classify(AlgorithmResult value) {
		double iqr = q3 - q1;
		boolean outleft = value.getScore() < q1 - ratio*iqr;
		boolean outright = value.getScore() > q3 + ratio*iqr;
		if(outleft || outright)
			return AnomalyResult.ANOMALY;
		else return AnomalyResult.NORMAL;
	}

	@Override
	public String toCompactString() {
		double iqr = q3 - q1;
		DecimalFormat df = new DecimalFormat("#.000"); 
		return "IQR(Q1:" + df.format(q1) + " Q3:" + df.format(q3) + " ratio:" + ratio + ") - {ANOMALY: value < " + df.format(q1 - ratio*iqr) + " or value > " + df.format(q3 + ratio*iqr) + "}";
	}

	@Override
	public String getClassifierTag() {
		return "IQR(" + ratio + ")";
	}

}
