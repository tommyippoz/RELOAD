/**
 * 
 */
package ippoz.reload.commons.failure;

import ippoz.reload.commons.loader.DatasetIndex;

/**
 * The Class InjectedElement.
 * Represents a failure injected a given time.
 *
 * @author Tommy
 */
public class InjectedElement {
	
	/** The failure timestamp. */
	private DatasetIndex index;
	
	/** The failure description. */
	private String description;

	/**
	 * Instantiates a new injected element.
	 *
	 * @param timestamp the timestamp
	 * @param description the description
	 */
	public InjectedElement(DatasetIndex index, String description) {
		this.index = index;
		this.description = description;
	}

	/**
	 * Gets the description of the injected element.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the timestamp of the injected element.
	 *
	 * @return the timestamp
	 */
	public DatasetIndex getIndex() {
		return index;
	}

	public boolean compliesWith(DatasetIndex other) {
		return index.compareTo(other) == 0;
	}
	
	public boolean compliesWith(InjectedElement el) {
		return compliesWith(el.getIndex());
	}

}
