package serversrc.resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import serversrc.resImpl.Customer;
import serversrc.resImpl.RMHashtable;
import serversrc.resImpl.ReservedItem;

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

	public boolean reserve(int id, int cid, String key, String location, int price, ReservedItem.rType rtype)
			throws RemoteException;

	public boolean unreserve(int id, int cid, String key)
			throws RemoteException;
}
