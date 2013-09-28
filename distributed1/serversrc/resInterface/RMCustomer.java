package serversrc.resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import serversrc.resImpl.RMHashtable;

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
}
