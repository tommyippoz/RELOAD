/**
 * 
 */
package ippoz.reload.externalutils;

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

}
