package serversrc.resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TransactionManager extends Remote {
	
	/**
	 * Informs transaction manager that a transaction is starting
	 * @return The id associated with this transaction. 
	 *   
	 */
	public int start() throws RemoteException;
	
	/**
	 * Commits the transaction with transactionID
	 * @param transactionID is the transaction to be committed
	 * @return true if commit is a success. 
	 * @throws RemoteException -rmi
	 */
	public boolean commit(int transactionID) throws RemoteException;
	
	
	/**
	 * Aborts the transaction with transactionID
	 * @param transactionID
	 * @throws RemoteException -rmi
	 */
	public void abort(int transactionID) throws RemoteException;
	
	/**
	 * Adds RM into transactionID list of RMs. 
	 * @param transactionID the id of the transaction
	 * @param rm the resource manager being used. 
	 */
	public void enlist(int transactionID, RMType rm);
}
