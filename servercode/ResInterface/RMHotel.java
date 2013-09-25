package ResInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface RMHotel extends Remote {

	/* Add rooms to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public boolean addRooms(int id, String location, int numRooms, int price) 
	throws RemoteException; 	
    
    /* Delete all Rooms from a location.
     * It may not succeed if there are reservations for this location.
     *
     * @return success
     */
    public boolean deleteRooms(int id, String location) 
	throws RemoteException; 
    
    /* return the number of rooms available at a location */
    public int queryRooms(int id, String location) 
	throws RemoteException; 
    
    /* return the price of a room at a location */
    public int queryRoomsPrice(int id, String location) 
	throws RemoteException; 
 
    /* reserve a room certain at this location */
    public boolean reserveRoom(int id, int customer, String locationd) 
	throws RemoteException; 
    
}
