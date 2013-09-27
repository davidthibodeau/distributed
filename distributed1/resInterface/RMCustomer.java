package resInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import resImpl.ReservableItem;

public interface RMCustomer extends Remote {
    
	public int newCustomer(int id) 
	throws RemoteException; 
    
    /** Delete Customer with ID = id
     *
     * @return success
     */		    
    public boolean deleteCustomer(int id) 
	throws RemoteException; 
    
    public boolean reserveItem(int id, ReservableItem item) throws RemoteException;

	boolean newCustomer(int id, int cid) throws RemoteException;
}
