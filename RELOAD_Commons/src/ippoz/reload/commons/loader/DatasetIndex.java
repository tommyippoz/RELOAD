/**
 * 
 */
package ippoz.reload.commons.loader;

/**
 * @author Tommy
 *
 */
public class DatasetIndex implements Comparable<DatasetIndex>{
	
	private int index;

	public DatasetIndex(int index) {
		super();
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public int compareTo(DatasetIndex o) {
		return Integer.compare(index, getIndex());
	}
	
	

}
