package serversrc.resImpl;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Vector;

import serversrc.resInterface.*;

public class Middleware implements ResourceManager {
	
	RMCar rmCar;
	RMFlight rmFlight;
	RMHotel rmHotel;
	RMCustomer rmCustomer;
	
	public static void main(String args[]) {
        // Figure out where server is running
        String server = "localhost";
        int port = 1099;
        Registry registry;
        if (args.length == 5) {
            server = server + ":" + args[4];
            port = Integer.parseInt(args[4]); 
        } else {
            System.err.println ("Wrong usage");
            System.out.println("Usage: java ResImpl.Middleware rmCar rmFlight rmHotel [port]");
            System.exit(1);
        }

        try {
            // create a new Server object
            // dynamically generate the stub (client proxy)
        	Middleware obj = new Middleware();
            ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
            // get a reference to the rmiregistry
            registry = LocateRegistry.getRegistry(port);
            // get the proxy and the remote reference by rmiregistry lookup
            obj.rmCar = (RMCar) registry.lookup(args[0]);
            obj.rmFlight = (RMFlight) registry.lookup(args[1]);
            obj.rmHotel = (RMHotel) registry.lookup(args[2]);
            obj.rmCustomer = (RMCustomer) registry.lookup(args[3]);
            if(obj.rmCar!=null && obj.rmFlight != null && obj.rmHotel!=null)
            {
            	System.out.println("Successful");
            	System.out.println("Connected to RMs");
            }
            else
            {
            	System.out.println("Unsuccessful");
            }

            // Bind the remote object's stub in the registry
            registry.rebind("Group2Middleware", rm);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
    }

	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats,
			int flightPrice) throws RemoteException {

		return rmFlight.addFlight(id, flightNum, flightSeats, flightPrice);
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price)
			throws RemoteException {

		return rmCar.addCars(id, location, numCars, price);
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price)
			throws RemoteException {

		return rmHotel.addRooms(id, location, numRooms, price);
	}

	@Override
	public int newCustomer(int id) throws RemoteException {
		
		return rmCustomer.newCustomer(id);
	}

	@Override
	public boolean newCustomer(int id, int cid) throws RemoteException {
		
		return rmCustomer.newCustomer(id, cid);
	}

	@Override
	public boolean deleteFlight(int id, int flightNum) throws RemoteException {
		
		return rmFlight.deleteFlight(id, flightNum);
	}

	@Override
	public boolean deleteCars(int id, String location) throws RemoteException {
		
		return rmCar.deleteCars(id, location);
	}

	@Override
	public boolean deleteRooms(int id, String location) throws RemoteException {
		
		return rmHotel.deleteRooms(id, location);
	}

	@Override
	public boolean deleteCustomer(int id, int customer) throws RemoteException {
		
		RMHashtable reservationHT = rmCustomer.deleteCustomer(id, customer);
		 for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {        
             String reservedkey = (String) (e.nextElement());
             ReservedItem reserveditem = (ReservedItem) reservationHT.get( reservedkey );
             Trace.info("RM::deleteCustomer(" + id + ", " + customer + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
             
             if (reserveditem.getrType() == ReservedItem.rType.FLIGHT)
            	 rmFlight.unreserveItem(id, reserveditem);
             else if (reserveditem.getrType() == ReservedItem.rType.CAR)
            	 rmCar.unreserveItem(id, reserveditem);
             else if (reserveditem.getrType() == ReservedItem.rType.ROOM)
            	 rmHotel.unreserveItem(id, reserveditem);
             
         }
		
		return true;
	}

	@Override
	public int queryFlight(int id, int flightNumber) throws RemoteException {

		return rmFlight.queryFlight(id, flightNumber);
	}

	@Override
	public int queryCars(int id, String location) throws RemoteException {

		return rmCar.queryCars(id, location);
	}

	@Override
	public int queryRooms(int id, String location) throws RemoteException {

		return rmHotel.queryRooms(id, location);
	}

	@Override
	public String queryCustomerInfo(int id, int customer)
			throws RemoteException {
		return rmCustomer.queryCustomerInfo(id, customer);
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber)
			throws RemoteException {
		
		return rmFlight.queryFlightPrice(id, flightNumber);
	}

	@Override
	public int queryCarsPrice(int id, String location) throws RemoteException {

		return rmCar.queryCarsPrice(id, location);
	}

	@Override
	public int queryRoomsPrice(int id, String location) throws RemoteException {

		return rmHotel.queryRoomsPrice(id, location);
	}

	@Override
	public boolean reserveFlight(int id, int customer, int flightNumber)
			throws RemoteException {

		return rmFlight.reserveFlight(id, customer, flightNumber);
	}

	@Override
	public boolean reserveCar(int id, int customer, String location)
			throws RemoteException {

		return rmCar.reserveCar(id, customer, location);
	}

	@Override
	public boolean reserveRoom(int id, int customer, String locationd)
			throws RemoteException {

		return rmHotel.reserveRoom(id, customer, locationd);
	}

	@Override
	public boolean itinerary(int id, int customer, Vector flightNumbers,
			String location, boolean Car, boolean Room) throws RemoteException {
		return false;
	}

	@Override
	public boolean unreserveItem(int id, ReservedItem reserveditem)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

}
