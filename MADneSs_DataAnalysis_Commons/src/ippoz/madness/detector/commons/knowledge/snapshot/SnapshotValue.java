/**
 * 
 */
package ippoz.madness.detector.commons.knowledge.snapshot;

/**
 * @author Tommy
 *
 */
public class SnapshotValue {
	
	private double first;
	private double last;
	
	public SnapshotValue(double first) {
		this.first = first;
		this.last = Double.NaN;
	}
	
	public SnapshotValue(double first, double last) {
		this.first = first;
		this.last = last;
	}
	
	public double getFirst() {
		return first;
	}
	
	public double getLast() {
		return last;
	}	

}
