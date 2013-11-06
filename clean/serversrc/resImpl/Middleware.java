package serversrc.resImpl;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Vector;
import LockManager.*;

import serversrc.resInterface.*;

public class Middleware implements ResourceManager {
	
	RMCar rmCar;
	RMFlight rmFlight;
	RMHotel rmHotel;
	RMCustomer rmCustomer;
	LockManager lock;
	TransactionManager tm;

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
			System.out.println("Usage: java ResImpl.Middleware rmCar rmFlight rmHotel rmCustomer [port]");
			System.exit(1);
		}

		try {
			// create a new Server object
			// dynamically generate the stub (client proxy)
			Middleware obj = new Middleware();
			ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
			// get a reference to the rmiregistry
			registry = LocateRegistry.getRegistry(args[0], port);
			// get the proxy and the remote reference by rmiregistry lookup
			obj.rmCar = (RMCar) registry.lookup("Group2RMCar");
			registry = LocateRegistry.getRegistry(args[1], port);
			obj.rmFlight = (RMFlight) registry.lookup("Group2RMFlight");
			registry = LocateRegistry.getRegistry(args[2], port);
			obj.rmHotel = (RMHotel) registry.lookup("Group2RMHotel");
			registry = LocateRegistry.getRegistry(args[3], port);
			obj.rmCustomer = (RMCustomer) registry.lookup("Group2RMCustomer");
			if(obj.rmCar!=null && obj.rmFlight != null && obj.rmHotel!=null)
			{
				System.out.println("Successful");
				System.out.println("Connected to RMs");
				obj.lock = new LockManager();
				obj.tm = new TMimpl(obj.rmCar, obj.rmFlight, obj.rmHotel, obj.rmCustomer);
			}
			else
			{
				System.out.println("Unsuccessful");
				System.exit(1);
			}

			// Bind the remote object's stub in the registry
			registry = LocateRegistry.getRegistry(port);
			registry = LocateRegistry.getRegistry(port);
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
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) 
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
		acquireLock(id, RMType.FLIGHT, Flight.getKey(flightNum), LockManager.WRITE);
		return rmFlight.addFlight(id, flightNum, flightSeats, flightPrice);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price)
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.CAR, Car.getKey(location), LockManager.WRITE);
			return rmCar.addCars(id, location, numCars, price);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}

	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price)
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.HOTEL, Hotel.getKey(location), LockManager.WRITE);
			return rmHotel.addRooms(id, location, numRooms, price);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	/*
	 * This function will make the request then acquire a Write lock for it. 
	 * Since newCustomer generates a new unique cid, there is not any lock for it yet.
	 */
	public int newCustomer(int id) 
			throws RemoteException, InvalidTransactionException, TransactionAbortedException{
		try{
			int cid = rmCustomer.newCustomer(id);
			acquireLock(id, RMType.CUSTOMER, Customer.getKey(cid), LockManager.WRITE);
			return cid;
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	public boolean newCustomer(int id, int cid)
			throws RemoteException, InvalidTransactionException, TransactionAbortedException{
		try{
			acquireLock(id, RMType.CUSTOMER, Customer.getKey(cid), LockManager.WRITE);
			return rmCustomer.newCustomer(id, cid);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	public boolean deleteFlight(int id, int flightNum) 
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.FLIGHT, Flight.getKey(flightNum), LockManager.WRITE);
			return rmFlight.deleteFlight(id, flightNum);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	public boolean deleteCars(int id, String location)
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.CAR, Car.getKey(location), LockManager.WRITE);
			return rmCar.deleteCars(id, location);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	public boolean deleteRooms(int id, String location)
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.HOTEL, Hotel.getKey(location), LockManager.WRITE);
			return rmHotel.deleteRooms(id, location);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}		
	}

	@Override
	public boolean deleteCustomer(int id, int customer)
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.CUSTOMER, Customer.getKey(customer), LockManager.WRITE);
			RMHashtable reservationHT = rmCustomer.deleteCustomer(id, customer);
			for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {        
				String reservedkey = (String) (e.nextElement());
				ReservedItem reserveditem = (ReservedItem) reservationHT.get( reservedkey );
				Trace.info("RM::deleteCustomer(" + id + ", " + customer + ") has reserved " 
						+ reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );

				String key = reserveditem.getKey();
				if (reserveditem.getrType() == ReservedItem.rType.FLIGHT){
					acquireLock(id, RMType.FLIGHT, key, LockManager.WRITE);
					rmFlight.unreserveItem(id, reserveditem.getKey());
				} else if (reserveditem.getrType() == ReservedItem.rType.CAR){
					acquireLock(id, RMType.CAR, key, LockManager.WRITE);
					rmCar.unreserveItem(id, reserveditem.getKey());
				}else if (reserveditem.getrType() == ReservedItem.rType.ROOM){
					acquireLock(id, RMType.HOTEL, key, LockManager.WRITE);
					rmHotel.unreserveItem(id, reserveditem.getKey());
				}
			}
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}		

		return true;
	}

	@Override
	public int queryFlight(int id, int flightNumber) 
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.FLIGHT, Flight.getKey(flightNumber), LockManager.READ);
			return rmFlight.queryFlight(id, flightNumber);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}		
	}

	@Override
	public int queryCars(int id, String location) 
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.CAR, Car.getKey(location), LockManager.READ);
			return rmCar.queryCars(id, location);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	public int queryRooms(int id, String location)
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.HOTEL, Hotel.getKey(location), LockManager.READ);
			return rmHotel.queryRooms(id, location);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
		
	}

	@Override
	public String queryCustomerInfo(int id, int customer)
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.CUSTOMER, Customer.getKey(customer), LockManager.READ);
			return rmCustomer.queryCustomerInfo(id, customer);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber)
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.FLIGHT, Flight.getKey(flightNumber), LockManager.READ);
			return rmFlight.queryFlightPrice(id, flightNumber);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}	
	}

	@Override
	public int queryCarsPrice(int id, String location)
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.CAR, Car.getKey(location), LockManager.READ);
			return rmCar.queryCarsPrice(id, location);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	public int queryRoomsPrice(int id, String location) 
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		try{
			acquireLock(id, RMType.HOTEL, Hotel.getKey(location), LockManager.READ);
			return rmHotel.queryRoomsPrice(id, location);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	public boolean reserveFlight(int id, int customer, int flightNum)
			throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		try{
			ReservedItem item = reserveItem(id, customer, Flight.getKey(flightNum), String.valueOf(flightNum), ReservedItem.rType.FLIGHT);
			return (item != null);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	public boolean reserveCar(int id, int customer, String location)
			throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		try{
			ReservedItem item = reserveItem(id, customer, Car.getKey(location), location, ReservedItem.rType.CAR);
			return (item != null);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	public boolean reserveRoom(int id, int customer, String location)
			throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		try{
			ReservedItem item = reserveItem(id, customer, Hotel.getKey(location), location, ReservedItem.rType.ROOM);
			return (item != null);
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
	}

	@Override
	public boolean itinerary(int id, int customer, Vector flightNumbers,String location, boolean car, boolean room) 
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightNumbers+ ", "+location+
				", " + car + ", " + room + " ) called" );        
		// Read customer object if it exists (and read lock it)
		try{
			if (car){
				reserveItem(id, customer, Car.getKey(location) ,location, ReservedItem.rType.CAR);
			}
			if (room){
				reserveItem(id, customer, Hotel.getKey(location) ,location, ReservedItem.rType.ROOM);
			}
			Vector flightsDone = new Vector();
			for (Enumeration e = flightNumbers.elements(); e.hasMoreElements();) {
				int flightnum = 0;
				try {
					flightnum = getInt(e.nextElement());
				} catch(Exception ex) {
					Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightNumbers+ ", "+location+
							", " + car + ", " + room + " ) -- Expected FlightNumber was not a valid integer. Exception "
							+ ex + " cached");
					return false;
				}
				ReservedItem reservedFlight = reserveItem(id, customer, Flight.getKey(flightnum), String.valueOf(flightnum), ReservedItem.rType.FLIGHT);
				if (reservedFlight == null){
					Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightnum+ ", "+location+
							", " + car + ", " + room + " ) -- flight could not have been reserved." );
					return false;
				}
				flightsDone.add(reservedFlight);	
			}
		} catch (TransactionAbortedException i) {
			abort(id);
			throw new TransactionAbortedException(id);
		}
        	
		return true;
	}
	
	/*Since the client sends a Vector of objects, we need this 
	 * unsafe function that retrieves the int from the vector.
	 * 
	 */ 
	public int getInt(Object temp) throws Exception {
		try {
			return (new Integer((String)temp)).intValue();
		} catch(Exception e) {
			throw e;
		}
	}
	
	/*
	 * Call RMCust to obtain customer, if it exists.
	 * Verify if item exists and is available. (Call RM*obj*)
	 * Reserve with RMCustomer
	 * Tell RM*obj* to reduce the number of available

	 */
	protected ReservedItem reserveItem(int id, int customerID, String key, String location, ReservedItem.rType rtype)
			throws RemoteException, TransactionAbortedException, InvalidTransactionException  {
		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );        
		// Verifies if customer exists
		acquireLock(id, RMType.CUSTOMER, Customer.getKey(customerID), LockManager.WRITE);
		Customer cust = rmCustomer.getCustomer(id, customerID);
		if ( cust == null ) {
			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
			return null;
		} 

		RMInteger price = null;
		// check if the item is available
		if (rtype == ReservedItem.rType.CAR){ 
			acquireLock(id, RMType.CAR, key, LockManager.WRITE);
			price = rmCar.reserveItem(id, customerID, key, location);
		} else if (rtype == ReservedItem.rType.FLIGHT) { 
			acquireLock(id, RMType.FLIGHT, key, LockManager.WRITE);
			price = rmFlight.reserveItem(id, customerID, key, location);
		} else if (rtype == ReservedItem.rType.ROOM) {
			acquireLock(id, RMType.HOTEL, key, LockManager.WRITE);
			price = rmHotel.reserveItem(id, customerID, key, location);
		}
		if ( price == null ) {
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed-- Object RM returned false." );
			return null;
		} else {  
			// We do the following check in case the customer has been
			// deleted between the first verification and now.
			ReservedItem item = rmCustomer.reserve(id, customerID, key, location, price.getValue(), rtype);
			if(item != null){
				Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
				return item;
			} else {

			}
			return null;

		}        
	}

	@Override
	public int start() throws RemoteException {
		return tm.start();
	}

	@Override
	public boolean commit(int id) 
			throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		return tm.commit(id);
	}

	@Override
	public void abort(int id) 
			throws RemoteException, InvalidTransactionException {
		tm.abort(id);
	}
	
	/*
	 * lock.Lock will return false only if informations are incorrect.
	 * The only information that can fail is the id that is provided by the user.
	 * Hence InvalidTransactionException is raised.
	 */
	private boolean acquireLock(int id, RMType type, String key, int lockType) 
			throws TransactionAbortedException, InvalidTransactionException {
		try {

			if (lock.Lock(id, key, lockType)) {
				tm.lives(id);
				if(lockType == LockManager.WRITE)
					tm.enlist(id, type);
				return true;
			} else{
				throw new InvalidTransactionException();
			}
		} catch (DeadlockException e) {
			throw new TransactionAbortedException(id);
		}
	}
}
