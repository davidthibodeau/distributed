package serversrc.resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import serversrc.resImpl.InvalidTransactionException;
import serversrc.resImpl.TransactionAbortedException;

public interface TransactionManager extends Remote {
	
	/**
	 * Informs transaction manager that a transaction is starting
	 * @param autocommit tells whether it starts a normal transaction or a series of autocommited operations
	 * @return The id associated with this transaction. 
	 *   
	 */
	public int start(boolean autocommit);
	
	/**
	 * Informs transaction manager that a transaction is starting
	 * @return The id associated with this transaction. 
	 *   
	 */
	int start();
	
	/**
	 * Commits the transaction with transactionID
	 * @param transactionID is the transaction to be committed
	 * @return true if commit is a success. 
	 * @throws TransactionAbortedException 
	 * @throws InvalidTransactionException 
	 */
	public boolean commit(int transactionID) throws InvalidTransactionException, TransactionAbortedException;
	
	
	/**
	 * Aborts the transaction with transactionID
	 * @param transactionID
	 * @throws InvalidTransactionException 
	 */
	public void abort(int transactionID) throws InvalidTransactionException;
	
	/**
	 * Adds RM into transactionID list of RMs. 
	 * @param transactionID the id of the transaction
	 * @param rm the resource manager being used. 
	 * @throws InvalidTransactionException 
	 */
	public void enlist(int transactionID, RMType rm) throws InvalidTransactionException;
	
	
	public boolean shutdown();

	/**
	 * Refreshes the TTL timer.
	 * @param id: the transaction id whose timer gets refreshed
	 * @return true if the timer has been refreshed. false if the transaction is on autocommit (and so does not have a TTL timer)
	 * @throws InvalidTransactionException
	 */
	public boolean lives(int id) throws InvalidTransactionException;

	public boolean boot();
	
	public void updateRMCar(RMCar rm);
	
	public void updateRMFlight(RMFlight rm);
	
	public void updateRMHotel(RMHotel rm);
	
	public void updateRMCustomer(RMCustomer rm);

	
}
