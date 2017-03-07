/**
 * 
 */
package ippoz.multilayer.detector.commons.datafetcher.database;

import ippoz.multilayer.detector.commons.support.AppLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.ibatis.common.jdbc.ScriptRunner;

/**
 * The Class DatabaseConnector.
 * Represents a generic database connector that needs to be instantiated depending on the specific database type. 
 *
 * @author Tommy
 */
public class DatabaseConnector {
	
	/** The basic connection. */
	private Connection conn;
	
	/**
	 * Instantiates a new database connector.
	 *
	 * @param dbName the database name
	 * @param username the database username
	 * @param password the database password
	 * @param create the create database flag
	 */
	public DatabaseConnector(String dbName, String username, String password, boolean create){
		this("jdbc:mysql://localhost:3306/", dbName, "com.mysql.jdbc.Driver", username, password, create);
	}
	
	/**
	 * Instantiates a new database connector.
	 *
	 * @param url the database url
	 * @param dbName the database name
	 * @param driver the database driver
	 * @param username the database username
	 * @param password the database password
	 * @param create the create database flag
	 */
	public DatabaseConnector(String url, String dbName, String driver, String username, String password, boolean create){
		try {
			if(create){
				dbName = "experiment";
				createDatabase(url, dbName, username, password);
			}
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url + dbName, username, password);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			AppLogger.logException(getClass(), e, "Impossible to estalish DB connection");
		} 
	}
	
	/**
	 * Gets the basic connection.
	 *
	 * @return the connection
	 */
	public Connection getConnection(){
		return conn;
	}
	
	/**
	 * Performs an SQL update (update, delete, alter).
	 *
	 * @param query the update query
	 * @return true, if update is successful
	 */
	public boolean update(String query){
		Statement stmt;
		try {
			stmt = conn.createStatement();
			//AppLogger.logInfo(getClass(), "QUERY: " + query);
			stmt.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			AppLogger.logException(getClass(), e, "Query not executed: '" + query + "'");
		}
		return false;
	}
	
	/**
	 * Executes query.
	 *
	 * @param query the SQL query
	 * @return the array list containing the results of the query
	 */
	protected ArrayList<HashMap<String, String>> executeQuery(String query){
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			//AppLogger.logInfo(getClass(), "QUERY: " + query);
			rs = stmt.executeQuery(query);
			return parseResultSet(rs);
		} catch (SQLException e) {
			AppLogger.logException(getClass(), e, "Query not executed: '" + query + "'");
		}
		return null;
	}
	
	/**
	 * Closes basic connection.
	 *
	 * @throws SQLException the SQL exception
	 */
	public void closeConnection() throws SQLException{
		conn.close();
	}
	
	/**
	 * Execute a given query passed by string.
	 *
	 * @param params the parameters
	 * @param query the query
	 * @return the resulting arraylist
	 */
	public ArrayList<HashMap<String, String>> executeCustomQuery(String[] params, String query){
		int i = 0;
		while(query.indexOf("?") != -1){
			if(params.length <= i){
				AppLogger.logError(DatabaseConnector.class, "SQLError", "Malformed parameters list");
			} else query = query.substring(0, query.indexOf("?")) + params[i] + query.substring(query.indexOf("?")+1);
			i++;
		}
		return executeQuery(query);
	}
	
	/**
	 * Execute builded query.
	 *
	 * @param params the parameters
	 * @param tables the list of the tables
	 * @param filter the filter
	 * @param orders the order clauses
	 * @return the resulting arraylist
	 */
	public ArrayList<HashMap<String, String>> executeBuildedQuery(String[] params, String[] tables, String filter, String[] orders){
		return executeQuery(buildQuery(params, tables, filter, orders));
	}
	
	/**
	 * Gets the first value of an arraylist by tag.
	 *
	 * @param list the arraylist
	 * @param tag the tag
	 * @return the first value by tag
	 */
	public static String getFirstValueByTag(ArrayList<HashMap<String, String>> list, String tag){
		if(list.size() > 0)
			return list.get(0).get(tag);
		else return null;
	}
	
	/**
	 * Filters the output.
	 *
	 * @param result the raw result
	 * @param field the field tag
	 * @return the filtered linkedlist
	 */
	public static LinkedList<String> filterOutput(ArrayList<HashMap<String, String>> result, String field){
		LinkedList<String> list = new LinkedList<String>();
		if(result.get(0).keySet().contains(field)) {
			for(HashMap<String, String> current : result){
				list.add(current.get(field));
			}
		}
		return list;
	}
	
	/**
	 * Parses the result set.
	 *
	 * @param rs the result set
	 * @return the array list
	 */
	protected static ArrayList<HashMap<String, String>> parseResultSet(ResultSet rs){
		HashMap<String, String> partial;
		ArrayList<HashMap<String, String>> list = null;
		try {
			list = new ArrayList<HashMap<String, String>>();
			rs.beforeFirst();
			while(rs.next()){
				partial = new HashMap<String, String>();
				for(int i=1;i<=rs.getMetaData().getColumnCount();i++){
					partial.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
				}
				list.add(partial);
			}
		} catch (SQLException e) {
			AppLogger.logException(DatabaseConnector.class, e, "Unable to read ResultSet");
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * Builds the query.
	 *
	 * @param params the params
	 * @param tables the tables
	 * @param filter the filter
	 * @param orders the orders
	 * @return the string
	 */
	public static String buildQuery(String[] params, String[] tables, String filter, String[] orders){
		String finalQuery = "";
		if(params == null || params.length == 0) {
			finalQuery = "select *";
		} else {
			finalQuery = "select " + params[0];
			for (int i=1;i<params.length;i++){
				finalQuery = finalQuery + ", " + params[i]; 
			}
		}
		finalQuery = finalQuery + " from ";
		if(tables == null || tables.length == 0){
			AppLogger.logError(DatabaseConnector.class, "SQLException", "Malformed Query: no tables specified");
			return null;
		} else {
			finalQuery = finalQuery + tables[0];
			for (int i=1;i<tables.length;i++){
				finalQuery = finalQuery + " natural join " + tables[i]; 
			}
		}
		if(filter != null && filter.length() > 0){
			finalQuery = finalQuery + " where " + filter;
		} 
		if(orders != null && orders.length > 0){
			finalQuery = finalQuery + " order by " + orders[0];
			for (int i=1;i<orders.length;i++){
				finalQuery = finalQuery + ", " + orders[i]; 
			}
		}
		return finalQuery;
	}
	
	/**
	 * Creates the database.
	 *
	 * @param url the database url
	 * @param dbName the database name
	 * @param user the database username
	 * @param psw the database password
	 */
	public static void createDatabase(String url, String dbName, String user, String psw){
		ScriptRunner sr;
		Reader reader = null;
		try {
			sr = new ScriptRunner(DriverManager.getConnection(url, user, psw), false, false);
			reader = new BufferedReader(new FileReader(new File("sql/CreateExpDatabase.sql")));
			sr.runScript(reader);
		} catch(Exception ex){
			AppLogger.logException(DatabaseConnector.class, ex, "Unable to create database");
		} 
	}

}
