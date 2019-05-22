package ippoz.reload.algorithm.elki;

import java.io.File;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.relation.Relation;

/**
 * The Interface ELKIAlgorithm, which wraps code from detection algorithms coming from ELKI framework.
 *
 * @param <V> the value type, extends an ELKI object
 */
public interface ELKIAlgorithm<V extends NumberVector> {

	/**
	 * Loads the file in which the object obtained during training is stored (or serialized).
	 *
	 * @param filename the filename
	 */
	public void loadFile(String filename);

	/**
	 * Gets the list of scores obtained during training.
	 *
	 * @return the scores list
	 */
	public List<Double> getScoresList();
	
	/**
	 * Gets the algorithm name.
	 *
	 * @return the algorithm name
	 */
	public String getAlgorithmName();

	/**
	 * Prints the file in which the object obtained during training is stored (or serialized).
	 *
	 * @param file the file
	 */
	public void printFile(File file);

	/**
	 * Runs the training of the algorithm. It outputs an ELKI object, or null if training fails.
	 *
	 * @param db the database
	 * @param relation the relation
	 * @return the object
	 */
	public Object run(Database db, Relation<V> relation);


}
