package ippoz.reload.algorithm.elki;

import java.io.File;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.relation.Relation;

public interface ELKIAlgorithm<V extends NumberVector> {

	public void loadFile(String filename);

	public List<Double> getScoresList();
	
	public String getAlgorithmName();

	public void printFile(File file);

	public Object run(Database db, Relation<V> relation);


}
