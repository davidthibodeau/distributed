package serversrc.resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import serversrc.resImpl.Customer;
import serversrc.resImpl.InvalidTransactionException;
import serversrc.resImpl.TransactionAbortedException;
import serversrc.resImpl.RMHashtable;
import serversrc.resImpl.ReservedItem;

/**
 *	This Interface is made to guarantee to the middleware the 
 *  Customer rm will have these methods. 
 *
 */
public interface RMCustomer extends Remote, RMBase {

	/* new customer just returns a unique customer identifier */
	public int newCustomer(int id) 
			throws RemoteException; 

	/* new customer with providing id */
	public boolean newCustomer(int id, int cid)
			throws RemoteException, TransactionAbortedException;

	/* deleteCustomer removes the customer and associated reservations */
	public RMHashtable deleteCustomer(int id,int customer) 
			throws RemoteException, TransactionAbortedException; 

	/* return a bill */
	public String queryCustomerInfo(int id,int customer) 
			throws RemoteException, TransactionAbortedException; 

	public Customer getCustomer(int id, int customerID)
			throws RemoteException;	

	public ReservedItem reserve(int id, int cid, String key, String location, int price, ReservedItem.rType rtype)
			throws RemoteException, TransactionAbortedException;

	public boolean unreserve(int id, int cid, ReservedItem item)
			throws RemoteException, TransactionAbortedException;
	
}
