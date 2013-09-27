package resInterface;


import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;
/** 
 * Simplified version from CSE 593 Univ. of Washington
 *
 * Distributed  System in Java.
 * 
 * failure reporting is done using two pieces, exceptions and boolean 
 * return values.  Exceptions are used for systemy things. Return
 * values are used for operations that would affect the consistency
 * 
 * If there is a boolean return value and you're not sure how it 
 * would be used in your implementation, ignore it.  I used boolean
 * return values in the interface generously to allow flexibility in 
 * implementation.  But don't forget to return true when the operation
 * has succeeded.
 */

public interface ResourceManager extends RMCar, RMFlight, RMHotel, Remote
{
			    			    
    /* new customer just returns a unique customer identifier */
    public int newCustomer(int id) 
	throws RemoteException; 
    
    /* new customer with providing id */
    public boolean newCustomer(int id, int cid)
	throws RemoteException;
    
    /* deleteCustomer removes the customer and associated reservations */
    public boolean deleteCustomer(int id,int customer) 
	throws RemoteException; 

    /* return a bill */
    public String queryCustomerInfo(int id,int customer) 
	throws RemoteException; 

    /* reserve an itinerary */
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location, boolean Car, boolean Room)
	throws RemoteException; 
    			
}
