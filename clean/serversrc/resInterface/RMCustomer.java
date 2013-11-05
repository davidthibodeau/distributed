package serversrc.resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import serversrc.resImpl.Customer;
import serversrc.resImpl.InvalidTransactionException;
import serversrc.resImpl.RMHashtable;
import serversrc.resImpl.ReservedItem;
import serversrc.resImpl.TransactionAbortedException;

/**
 *	This Interface is made to guarantee to the middleware the 
 *  Customer rm will have these methods. 
 *
 */
public interface RMCustomer extends Remote {

	/* new customer just returns a unique customer identifier */
	public int newCustomer(int id) 
			throws RemoteException; 

	/* new customer with providing id */
	public boolean newCustomer(int id, int cid)
			throws RemoteException;

	/* deleteCustomer removes the customer and associated reservations */
	public RMHashtable deleteCustomer(int id,int customer) 
			throws RemoteException; 

	/* return a bill */
	public String queryCustomerInfo(int id,int customer) 
			throws RemoteException; 

	public Customer getCustomer(int id, int customerID)
			throws RemoteException;	

	public ReservedItem reserve(int id, int cid, String key, String location, int price, ReservedItem.rType rtype)
			throws RemoteException;

	public boolean unreserve(int id, int cid, ReservedItem item)
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
