/**
 * 
 */
package ippoz.multilayer.detector.commons.invariants;

import ippoz.multilayer.detector.commons.data.MultipleSnapshot;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;

/**
 * @author Tommy
 *
 */
public class DataSeriesMember extends InvariantMember {

	private DataSeries dataSeries;
	
	public DataSeriesMember(DataSeries dataSeries) {
		super(Double.class, dataSeries.getName());
		this.dataSeries = dataSeries;
	}
	
	@Override
	public String getStringValue(MultipleSnapshot snapshot) {
		return String.valueOf(snapshot.getSnapshot(dataSeries).getSnapValue());
	}

	@Override
	public String toString() {
		return dataSeries.toString();
	}

	@Override
	public boolean equals(Object other) {
		DataSeriesMember oMember = (DataSeriesMember)other;
		return oMember.getDataSeries().compareTo(dataSeries) == 0;
	}

	public DataSeries getDataSeries() {
		return dataSeries;
	}

	@Override
	public boolean contains(DataSeries serie) {
		return dataSeries.contains(serie);
	}	

}
