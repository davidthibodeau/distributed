package serversrc.resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import serversrc.resImpl.InvalidTransactionException;
import serversrc.resImpl.TransactionAbortedException;
import serversrc.resImpl.RMInteger;

public interface RMBase extends Remote {
	
	
	public boolean prepare(int id) throws RemoteException, InvalidTransactionException, TransactionAbortedException;
	
	/**
	 * Commits the transaction with transactionID
	 * @param transactionID is the transaction to be committed
	 * @return true if commit is a success. 
	 * @throws RemoteException -rmi
	 * @throws TransactionAbortedException 
	 * @throws InvalidTransactionException 
	 */
	public boolean commit(int transactionID) throws RemoteException, InvalidTransactionException, TransactionAbortedException;
	
	
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

}
