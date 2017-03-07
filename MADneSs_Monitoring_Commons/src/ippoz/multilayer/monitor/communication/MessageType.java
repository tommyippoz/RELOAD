/**
 * 
 */
package ippoz.multilayer.monitor.communication;

/**
 * The Enum MessageType.
 * Defines all the message that can be sent through the communication channel.
 *
 * @author Tommy
 */
public enum MessageType {
	
	/** The ping flag. */
	PING,
	
	/** The OK flag. */
	OK,
	
	/** The add probe flag. */
	ADD_PROBE,
	
	/** The check probe flag. */
	CHECK_PROBE,
	
	/** The start probe flag. */
	START_PROBE,
	
	/** The setup SUT flag. */
	SETUP_SUT,
	
	/** The start experiment flag. */
	START_EXPERIMENT,
	
	/** The end experiment flag. */
	END_EXPERIMENT,
	
	/** The start SUT flag. */
	START_SUT,
	
	/** The shutdown SUT flag. */
	SHUTDOWN_SUT,
	
	/** The restart SUT flag. */
	RESTART_SUT, 
	
	/** The start campaign flag. */
	START_CAMPAIGN,
	
	/** The end campaign flag. */
	END_CAMPAIGN
}
