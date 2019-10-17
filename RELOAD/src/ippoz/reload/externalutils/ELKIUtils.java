/**
 * 
 */
package ippoz.reload.externalutils;

import ippoz.reload.algorithm.support.ClusterableSnapshot;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;

import java.util.List;

import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;

/**
 * @author Tommy
 *
 */
public class ELKIUtils {
	
	/**
	 * Creates the elki database.
	 *
	 * @param data the data
	 * @return the database
	 */
	public static Database createElkiDatabase(double[][] data){
		DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
	    Database db = new StaticArrayDatabase(dbc, null);
	    db.initialize();  
	    return db;
	}
	
	/**
	 * Creates the elki database.
	 *
	 * @param data the data
	 * @return the database
	 */
	public static Database createElkiDatabase(List<ClusterableSnapshot> list){
		double[][] data = new double[list.size()][];
		for(int i=0;i<list.size();i++){
			data[i] = list.get(i).getPoint();
		}
		return createElkiDatabase(data);
	}

}
