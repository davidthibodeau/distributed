package serversrc.resImpl;

import java.rmi.RemoteException;
import serversrc.resInterface.*;

import java.util.Enumeration;


public class RMCustomerImpl implements RMCustomer{
	
	static private Integer counter = 0;
	private RMHashtable customers = new RMHashtable();
	private ResourceManager rm = null;
	
	
	@Override
	public int newCustomer(int id) throws RemoteException {
		int cid = counter.intValue();
		counter++;
		Customer customer = new Customer(cid);
		customers.put(cid, customer);
		return cid;
	}
	@Override
	public boolean newCustomer(int id, int cid) throws RemoteException {
	    {
	        Trace.info("RM::addCustomer(" + cid +") called" );
	        Customer curObj = (Customer) getCustomer( cid );
	        if ( curObj == null ) {
	            // doesn't exist...add it
	            Customer newObj = new Customer( cid);
	            customers.put(cid, newObj);
	            Trace.info("RM::addCustomer(" + cid + ") created new customer " + id);
	        } else {
	            Trace.error("RM::addCustomer(" + cid +") Customer already exists, no action taken.");
	            return false;
	        }
	        return(true);
	    }
	}

	@Override
	/**
	 * Must remove all reserved items of that customer as well. 
	 */
	public boolean deleteCustomer(int id) throws RemoteException {
        Customer curObj = (Customer) getCustomer(id);
		if ( curObj == null ) {
            
            Trace.error("RM::deleteCustomer("+ id+") Customer not found, no action taken.");
            return false;
        } else {
        	RMHashtable reservedItems = curObj.getReservations();
        	Object key = null;
        	for (Enumeration e = reservedItems.keys(); e.hasMoreElements(); ) {
				key = e.nextElement();
				ReservableItem item = (ReservableItem) reservedItems.get( key );
				//TODO: Delete all instances of the reservedItem, Suspect we need to use the toString(). 
			}
        	customers.remove(id);
        	Trace.info("RM::deleteCustomer(" + id + "), Customer deleted.");
        	return true;
        	
        }
	}


	public Object getCustomer(Integer id){
		return customers.get(id);
	}




}
