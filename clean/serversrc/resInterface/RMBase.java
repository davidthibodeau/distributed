package serversrc.resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import serversrc.resImpl.Crash;
import serversrc.resImpl.InvalidTransactionException;
import serversrc.resImpl.TransactionAbortedException;

public interface RMBase extends Remote {
	

	public boolean prepare(int id) throws RemoteException;
	
	/**
	 * Commits the transaction with transactionID
	 * @param transactionID is the transaction to be committed
	 * @return true if commit is a success. 
	 * @throws RemoteException -rmi
	 * @throws TransactionAbortedException 
	 * @throws InvalidTransactionException 
	 */
	public boolean commit(int transactionID) throws RemoteException, InvalidTransactionException;
	
	
	/**
	 * Aborts the transaction with transactionID
	 * @param transactionID
	 * @throws RemoteException -rmi
	 * @throws InvalidTransactionException 
	 */
	public void abort(int transactionID) throws RemoteException, InvalidTransactionException;
	
	public boolean shutdown() throws RemoteException;
	
	public boolean enlist(int id) throws RemoteException;
	
	public void selfdestruct() throws RemoteException;
	
	/**
	 * Used to test whether a connection to the RM can be made or if the RM crashed.
	 * @return true
	 * @throws RemoteException
	 */
	public boolean heartbeat() throws RemoteException;

	public void setCrashType(Crash crashType) throws RemoteException;

}
