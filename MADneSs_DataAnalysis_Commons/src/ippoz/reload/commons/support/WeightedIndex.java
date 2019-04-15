/**
 * 
 */
package ippoz.reload.commons.support;

/**
 * @author Tommy
 *
 */
public class WeightedIndex {
	
	private int index;
	private double weigth;
	
	public WeightedIndex(int index, double weigth) {
		this.index = index;
		this.weigth = weigth;
	}
	
	public int getIndex() {
		return index;
	}
	
	public double getWeigth() {
		return weigth;
	}
	
}
