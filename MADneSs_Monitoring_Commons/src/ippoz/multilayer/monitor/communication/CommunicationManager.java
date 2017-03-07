/**
 * 
 */
package ippoz.multilayer.monitor.communication;

import ippoz.multilayer.commons.support.AppLogger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * The Class CommunicationManager.
 * Manages communication between Master and Slave of the experiments management.
 *
 * @author Tommy
 */
public class CommunicationManager {

	/** The server socket. */
	private ServerSocket ssocket;
	
	/** The destination IP address. */
	private String ipAddress;
	
	/** The IP port. */
	private int ipPort;
	
	/**
	 * Instantiates a new communication manager.
	 *
	 * @param ipAddress the IP address
	 * @param inPort the input port
	 * @param outPort the output port
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public CommunicationManager(String ipAddress, int inPort, int outPort) throws IOException {
		this.ipAddress = ipAddress;
		ipPort = outPort;
		ssocket = new ServerSocket(inPort);
	}
	
	/**
	 * Sends data through the communication channel.
	 *
	 * @param toSend the Object to send
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void send(Object toSend) throws IOException {
		LinkedList<Object> list = new LinkedList<Object>();
		list.add(toSend);
		send(list);
	}
	
	/**
	 * Sends a list of data through the communication channel.
	 *
	 * @param toSend the list to send
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void send(Object[] toSend) throws IOException {
		send(new LinkedList<Object>(Arrays.asList(toSend)));
	}
	
	/**
	 * Sends a collection of data through the communication channel.
	 *
	 * @param toSend the to send
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void send(Collection<Object> toSend) throws IOException {
		Socket socket = null;
		ObjectOutputStream objStream = null;
		try {
			while(socket == null) {
				try {
					socket = new Socket(ipAddress, ipPort);
				} catch (IOException e) {}
			}
			objStream = new ObjectOutputStream(socket.getOutputStream());
			for(Object obj : toSend){
				objStream.writeObject(obj);
				//AppLogger.logInfo(getClass(), "Sent: " + obj.toString());
			}
		} catch (UnknownHostException ex) {
			AppLogger.logException(getClass(), ex, "Unable to find host");
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to communicate");
		} finally {
			if(!socket.isClosed())
				socket.close();
		}
	}
	
	/**
	 * Receives data from the communication channel.
	 *
	 * @return the list of read objects
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public LinkedList<Object> receive() throws IOException {
		LinkedList<Object> objList = null;
		Socket newSocket = null;
		ObjectInputStream oiStream = null;
		Object readed;
		try {
			newSocket = ssocket.accept();
			objList = new LinkedList<Object>();
			oiStream = new ObjectInputStream(newSocket.getInputStream());
			while((readed = oiStream.readObject()) != null){
				objList.add(readed);
				//AppLogger.logInfo(getClass(), "Received: " + readed.toString());
			}
		} catch (EOFException ex){
			//AppLogger.logInfo(getClass(), "No such elements in stream");
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to get messages");
		} catch (ClassNotFoundException ex) {
			AppLogger.logException(getClass(), ex, "Wrong object on the flow");
		} finally {
			if(oiStream != null)
				oiStream.close();
			if(newSocket != null && !newSocket.isClosed())
				newSocket.close();
		}
		return objList;
	}
	
	/**
	 * Waits for a predefined message sent through the channel.
	 *
	 * @param message the message
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void waitFor(MessageType message) throws IOException {
		boolean found = false;
		while(!found) {
			for(Object obj : receive()){
				if(obj instanceof MessageType && ((MessageType)obj).equals(message)){
					found = true;
					break;
				}
			}
		}
	}
	
	/**
	 * Waits for an OK message.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void waitForConfirm() throws IOException {
		waitFor(MessageType.OK);
	}
	
	/**
	 * Flushes the channel.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void flush() throws IOException{
		ssocket.close();
	}

	/**
	 * Checks if the channel is alive.
	 *
	 * @return true, if is alive
	 */
	public boolean isAlive() {
		return !ssocket.isClosed();
	}

	/**
	 * Gets the destination IP address.
	 *
	 * @return the ip address
	 */
	public String getIpAddress() {
		return ipAddress;
	}
	
}
