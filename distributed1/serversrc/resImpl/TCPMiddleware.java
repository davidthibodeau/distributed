package serversrc.resImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Vector;

import serversrc.resInterface.*;

public class TCPMiddleware implements TCPResourceManager {

	RMCar rmCar;
	RMFlight rmFlight;
	RMHotel rmHotel;
	RMCustomer rmCustomer;
	static Socket flightSocket;
	static Socket carsSocket;
	static Socket hotelSocket;
	static Socket customerSocket;
	
	public static void main(String args[]) {
		// Figure out where server is running
		String server = "localhost";
		int port = 1099;
		ServerSocket serverSocket;

		Socket clientSocket;
		Registry registry;
		if (args.length == 5) {
			server = server + ":" + args[4];
			port = Integer.parseInt(args[4]);
		} else {
			System.err.println("Wrong usage");
			System.out
					.println("Usage: java ResImpl.Middleware rmCar rmFlight rmHotel [port]");
			System.exit(1);
		}

		try {
			// Listens for clients messages.
			serverSocket = new ServerSocket(port);
			// Where to write to the client
			clientSocket = serverSocket.accept();
			flightSocket = new Socket(server, port);
			carsSocket = new Socket(server, port);
			hotelSocket = new Socket(server, port);
			customerSocket = new Socket(server, port);
			String methodInvocation;
			BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			TCPMiddleware obj = new TCPMiddleware();
			
			while((methodInvocation = in.readLine()) != null){
				obj.methodSelector(methodInvocation);
				//TODO: send response to client (may need to change methodSelector)
			}
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * parses a string into a method invocation
	 * @param methodInvocation the string to be parsed
	 * @throws RemoteException 
	 * @throws NumberFormatException 
	 */
	private void methodSelector(String methodInvocation) throws Exception {
		String params[] = methodInvocation.split("[,()]");
		if(params[0].compareTo("addFlight") == 0){
			this.addFlight(Integer.parseInt(params[1]),Integer.parseInt(params[2]),Integer.parseInt(params[3]),Integer.parseInt(params[4]));
			return;
		}
		if(params[0].contains("addCars")){
			this.addCars(Integer.parseInt(params[1]),params[2],Integer.parseInt(params[3]),Integer.parseInt(params[4]));
			return;
		}
		if(params[0].contains("addRooms")){
			this.addRooms(Integer.parseInt(params[1]),params[2],Integer.parseInt(params[3]),Integer.parseInt(params[4]));
			return;
		}
		if(params[0].contains("newCustomer")){
			this.newCustomer(Integer.parseInt(params[1]),Integer.parseInt(params[2]));
			return;
		}
		if(params[0].contains("deleteFlight")){
			this.deleteFlight(Integer.parseInt(params[1]),Integer.parseInt(params[2]));
			return;
		}
		if(params[0].contains("deleteCars")){
			this.deleteCars(Integer.parseInt(params[1]),params[2]);
			return;
		}
		if(params[0].contains("deleteRooms")){
			this.deleteRooms(Integer.parseInt(params[1]),params[2]);
			return;
		}
		if(params[0].contains("deleteCustomer")){
			this.deleteCustomer(Integer.parseInt(params[1]),Integer.parseInt(params[2]));
			return;
		}
		if(params[0].contains("queryFlight")){
			this.queryFlight(Integer.parseInt(params[1]),Integer.parseInt(params[2]));
			return;
		}
		if(params[0].contains("queryCars")){
			this.queryCars(Integer.parseInt(params[1]),params[2]);
			return;
		}
		if(params[0].contains("queryRooms")){
			this.queryRooms(Integer.parseInt(params[1]),params[2]);
			return;
		}
		if(params[0].contains("queryCustomerInfo")){
			this.queryCustomerInfo(Integer.parseInt(params[1]),Integer.parseInt(params[2]));
			return;
		}
		if(params[0].contains("queryFlightPrice")){
			this.queryFlightPrice(Integer.parseInt(params[1]),Integer.parseInt(params[2]));
			return;
		}
		if(params[0].contains("queryCarsPrice")){
			this.queryCarsPrice(Integer.parseInt(params[1]),params[2]);
			return;
		}
		if(params[0].contains("queryRoomsPrice")){
			this.queryRoomsPrice(Integer.parseInt(params[1]),params[2]);
			return;
		}
		if(params[0].contains("reserveFlight")){
			this.reserveFlight(Integer.parseInt(params[1]),Integer.parseInt(params[2]),Integer.parseInt(params[3]));
			return;
		}
		if(params[0].contains("reserveCar")){
			this.reserveCar(Integer.parseInt(params[1]),Integer.parseInt(params[2]),params[3]);
			return;
		}
		if(params[0].contains("reserveRoom")){
			this.reserveRoom(Integer.parseInt(params[1]),Integer.parseInt(params[2]),params[3]);
			return;
		}		
		
	}

	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats,
			int flightPrice) throws IOException{
		//send method to flight manager
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			out = new PrintWriter(flightSocket.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(flightSocket.getInputStream()));
			out.println("addFlight(" + id +","+flightNum + "," + flightSeats + "," + flightPrice);
			return in.readLine().contentEquals("true");
		} catch (IOException e) {
			return false;
		}
		finally{
			in.close();
			out.close();
		}
		//return flight manager's response
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
			ReservedItem reserveditem = (ReservedItem) reservationHT
					.get(reservedkey);
			Trace.info("RM::deleteCustomer(" + id + ", " + customer
					+ ") has reserved " + reserveditem.getKey() + " "
					+ reserveditem.getCount() + " times");

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
	public boolean reserveFlight(int id, int customer, int flightNum)
			throws RemoteException {

		return reserveItem(id, customer, Flight.getKey(flightNum),
				String.valueOf(flightNum), ReservedItem.rType.FLIGHT);
	}

	@Override
	public boolean reserveCar(int id, int customer, String location)
			throws RemoteException {

		return reserveItem(id, customer, Car.getKey(location), location,
				ReservedItem.rType.CAR);
	}

	@Override
	public boolean reserveRoom(int id, int customer, String location)
			throws RemoteException {

		return reserveItem(id, customer, Hotel.getKey(location), location,
				ReservedItem.rType.ROOM);
	}

	@Override
	public boolean itinerary(int id, int customer, Vector flightNumbers,
			String location, boolean Car, boolean Room) throws RemoteException {
		Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", "
				+ flightNumbers + ", " + location + ", " + Car + ", " + Room
				+ " ) called");
		// Read customer object if it exists (and read lock it)
		Customer cust = rmCustomer.getCustomer(id, customer);
		if (cust == null) {
			Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", "
					+ flightNumbers + ", " + location + ", " + Car + ", "
					+ Room + " ) -- Customer non existent, adding it.");
			rmCustomer.newCustomer(id, customer);
			cust = rmCustomer.getCustomer(id, customer);
		}

		if (Car) {
			if (reserveCar(id, customer, location)) {
				Trace.info("RM::itinerary( " + id + ", customer=" + customer
						+ ", " + flightNumbers + ", " + location + ", " + Car
						+ ", " + Room
						+ " ) -- Car could not have been reserved.");
				return false;
			}
		}
		if (Room) {
			if (reserveRoom(id, customer, location)) {
				Trace.info("RM::itinerary( " + id + ", customer=" + customer
						+ ", " + flightNumbers + ", " + location + ", " + Car
						+ ", " + Room
						+ " ) -- Room could not have been reserved.");
				return false;
			}
		}
		for (Enumeration e = flightNumbers.elements(); e.hasMoreElements();) {
			int flightnum = 0;
			try {
				flightnum = getInt(e.nextElement());
			} catch (Exception ex) {
				Trace.info("RM::itinerary( "
						+ id
						+ ", customer="
						+ customer
						+ ", "
						+ flightNumbers
						+ ", "
						+ location
						+ ", "
						+ Car
						+ ", "
						+ Room
						+ " ) -- Expected FlightNumber was not a valid integer. Exception "
						+ ex + " cached");
				return false;
			}
			if (reserveFlight(id, customer, flightnum)) {
				Trace.info("RM::itinerary( " + id + ", customer=" + customer
						+ ", " + flightnum + ", " + location + ", " + Car
						+ ", " + Room
						+ " ) -- flight could not have been reserved.");
				return false;
			}

		}

		return true;
	}

	/*
	 * Since the client sends a Vector of objects, we need this unsafe function
	 * that retrieves the int from the vector.
	 */
	public static int getInt(Object temp) throws Exception {
		try {
			return (new Integer((String) temp)).intValue();
		} catch (Exception e) {
			throw e;
		}
	}

	/*
	 * Call RMCust to obtain customer, if it exists. Verify if item exists and
	 * is available. (Call RM*obj*) Reserve with RMCustomer Tell RM*obj* to
	 * reduce the number of available
	 */
	protected boolean reserveItem(int id, int customerID, String key,
			String location, ReservedItem.rType rtype) throws RemoteException {
		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", "
				+ key + ", " + location + " ) called");
		// Read customer object if it exists (and read lock it)
		Customer cust = rmCustomer.getCustomer(id, customerID);
		if (cust == null) {
			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key
					+ ", " + location + ")  failed--customer doesn't exist");
			return false;
		}

		RMInteger price = null;
		// check if the item is available
		if (rtype == ReservedItem.rType.CAR)
			price = rmCar.reserveItem(id, customerID, key, location);
		else if (rtype == ReservedItem.rType.FLIGHT)
			price = rmFlight.reserveItem(id, customerID, key, location);
		else if (rtype == ReservedItem.rType.ROOM)
			price = rmHotel.reserveItem(id, customerID, key, location);

		if (price == null) {
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", "
					+ key + ", " + location
					+ ") failed-- Object RM returned false.");
			return false;
		} else {
			cust.reserve(key, location, price.getValue(), rtype);
			rmCustomer.reserve(id, cust);

			Trace.info("RM::reserveItem( " + id + ", " + customerID + ", "
					+ key + ", " + location + ") succeeded");
			return true;
		}
	}

}
