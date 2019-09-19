/**
 * 
 */
package ippoz.reload.commons.layers;

/**
 * The Enum LayerType.
 * Defines all the types of probes that exist in the testbed.
 *
 * @author Tommy
 */
public enum LayerType {
	
	/** The CentOS OS layer. */
	CENTOS, 
	
	/** The Ubuntu OS layer. */
	UBUNTU, 
	
	/** The Windows OS layer. */
	WINDOWS, 
	
	/** The JVM AS layer. */
	JVM, 
	
	/** The Unix network layer. */
	UNIX_NETWORK,
	
	/** The MySQL database layer. */
	MYSQL, 
	
	/** The SQLserver database layer. */
	SQLSERVER, 
	
	/** The Oracle database layer. */
	ORACLE, 
	
	/** The MongoDB database layer. */
	MONGODB, 
		
	/** The Tomcat AS layer. */
	TOMCAT, 
	
	/** The JBoss AS layer. */
	JBOSS,
	
	/** The no layer placeholder. */
	NO_LAYER, 
	
	/** The composition placeholder. */
	COMPOSITION
}
