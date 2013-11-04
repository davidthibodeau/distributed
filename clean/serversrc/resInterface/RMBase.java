package serversrc.resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import serversrc.resImpl.InvalidTransactionException;
import serversrc.resImpl.RMInteger;
import serversrc.resImpl.ReservedItem;
import serversrc.resImpl.TransactionAbortedException;

public interface RMBase extends Remote {

	public boolean unreserveItem(int id, ReservedItem reserveditem)
		throws RemoteException;
	
	public boolean unreserveItem(int id, String key)
			throws RemoteException;
	
	public RMInteger reserveItem(int id, int customerID, String key, String location)
	    	throws RemoteException;

	
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

}
