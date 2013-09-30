package serversrc.resImpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;

import serversrc.resInterface.*;

@SuppressWarnings("rawtypes")
public class TCPMiddleware {

	RMCar rmCar;
	RMFlight rmFlight;
	RMHotel rmHotel;
	RMCustomer rmCustomer;
	static Socket flightSocket;
	static Socket carsSocket;
	static Socket hotelSocket;
	static Socket customerSocket;
	
	ObjectInputStream clientIn;
	ObjectOutputStream clientOut;
	
	ObjectInputStream flightIn;
	ObjectOutputStream flightOut;

	ObjectInputStream carsIn;
	ObjectOutputStream carsOut;

	ObjectInputStream hotelIn;
	ObjectOutputStream hotelOut;

	ObjectInputStream customersIn;
	ObjectOutputStream customersOut;

	public static void main(String args[]) {
		// Figure out where server is running
		String server = "localhost";
		int port = 1099;
		ServerSocket serverSocket;

		Socket clientSocket;
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
			Vector methodInvocation;
			
			TCPMiddleware obj = new TCPMiddleware();
			
			obj.clientIn = new ObjectInputStream(
					clientSocket.getInputStream());
			obj.clientOut = new ObjectOutputStream(
					clientSocket.getOutputStream());
			

			obj.flightIn = new ObjectInputStream(flightSocket.getInputStream());
			obj.flightOut = new ObjectOutputStream(
					flightSocket.getOutputStream());

			obj.carsIn = new ObjectInputStream(flightSocket.getInputStream());
			obj.carsOut = new ObjectOutputStream(flightSocket.getOutputStream());

			obj.hotelIn = new ObjectInputStream(flightSocket.getInputStream());
			obj.hotelOut = new ObjectOutputStream(
					flightSocket.getOutputStream());

			obj.customersIn = new ObjectInputStream(
					flightSocket.getInputStream());
			obj.customersOut = new ObjectOutputStream(
					flightSocket.getOutputStream());

			while ((methodInvocation = (Vector) obj.clientIn.readObject()) != null) {
				obj.methodSelector(methodInvocation);
			}
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * parses a string into a method invocation
	 * 
	 * @param methodInvocation
	 *            the string to be parsed
	 * @throws RemoteException
	 * @throws NumberFormatException
	 */
	private void methodSelector(Vector methodInvocation) throws Exception {
		Vector args = methodInvocation;
		String method = getString(args.elementAt(0));
		if (method.contains("Flight")) {
			// send method to flight manager and get response
			try {
				flightOut.writeObject(methodInvocation);
				clientOut.writeObject(flightIn.readObject());
			} catch (IOException e) {
				Trace.error("IOException in method invocation: "
						+ getString(method));
				return;
			}

			// return flight manager's response return;
		}
		if (!method.contains("reserve")) {
			if (method.contains("Cars")) {
				try {
					carsOut.writeObject(methodInvocation);
					clientOut.writeObject(carsIn.readObject());
				} catch (IOException e) {
					Trace.error("IOException in method invocation: "
							+ getString(method));
				}
			}
			if (method.contains("Rooms")) {
				try {
					hotelOut.writeObject(methodInvocation);
					clientOut.writeObject(hotelIn.readObject());
				} catch (IOException e) {
					Trace.error("IOException in method invocation: "
							+ getString(method));
				}
			}
			if (method.contains("Customer")) {

				try {
					customersOut.writeObject(methodInvocation);

					// TODO: deleteCustomer
					clientOut.writeObject(customersIn.readObject());
				} catch (IOException e) {
					Trace.error("IOException in method invocation: "
							+ getString(method));
				}
			}
		} else {
			if (method.equalsIgnoreCase("reserveFlight")) {
				clientOut.writeObject(reserveFlight(getInt(args.elementAt(1)),
						getInt(args.elementAt(2)), getInt(args.elementAt(3))));
			}
			if (method.equalsIgnoreCase("reserveCar")) {
				clientOut.writeObject(reserveCar(getInt(args.elementAt(1)),
						getInt(args.elementAt(2)), getString(args.elementAt(3))));
			}
			if (method.equalsIgnoreCase("reserveRoom")) {
				clientOut.writeObject(reserveRoom(getInt(args.elementAt(1)),
						getInt(args.elementAt(2)), getString(args.elementAt(3))));
			}
		}
	}

	public boolean reserveFlight(int id, int customer, int flightNum)
			throws IOException {

		return reserveItem(id, customer, Flight.getKey(flightNum),
				String.valueOf(flightNum), ReservedItem.rType.FLIGHT);
	}

	public boolean reserveCar(int id, int customer, String location)
			throws IOException {

		return reserveItem(id, customer, Car.getKey(location), location,
				ReservedItem.rType.CAR);
	}

	public boolean reserveRoom(int id, int customer, String location)
			throws IOException {

		return reserveItem(id, customer, Hotel.getKey(location), location,
				ReservedItem.rType.ROOM);
	}

	public boolean itinerary(int id, int customer, Vector flightNumbers,
			String location, boolean Car, boolean Room) throws IOException {
		//TODO: Convert To TCP
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
	 * Call RMCust to obtain customer, if it exists. Verify if item exists and
	 * is available. (Call RM*obj*) Reserve with RMCustomer Tell RM*obj* to
	 * reduce the number of available
	 */
	protected boolean reserveItem(int id, int customerID, String key,
			String location, ReservedItem.rType rtype) throws IOException {
		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", "
				+ key + ", " + location + " ) called");
		Boolean result = false;
		Vector<Object> args = new Vector<Object>();
		args.add("getCustomer");
		args.add(id);
		args.add(customerID);
		args.add(key);
		args.add(location);
		args.add(rtype);
		customersOut.writeObject(args);
		Customer cust;
		try {
			cust = (Customer) customersIn.readObject();
		} catch (ClassNotFoundException e) {
			Trace.error("Object returned was not a Customer ");
			return false;
		}
		if (cust == null) {
			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key
					+ ", " + location + ")  failed--customer doesn't exist");
			return false;
		}

		RMInteger price = null;
		// check if the item is available
		args.set(0, "reserve");
		try {
			if (rtype == ReservedItem.rType.CAR) {
				carsOut.writeObject(args);
				price = (RMInteger) carsIn.readObject();
			} else if (rtype == ReservedItem.rType.FLIGHT) {
				flightOut.writeObject(args);
				price = (RMInteger) flightIn.readObject();
			} else if (rtype == ReservedItem.rType.ROOM) {
				hotelOut.writeObject(args);
				price = (RMInteger) hotelIn.readObject();
			}
		} catch (ClassNotFoundException e) {
			Trace.error("Expected an RMInteger, In TCPMiddleware reserveItem");

		}
		if (price == null) {
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", "
					+ key + ", " + location
					+ ") failed-- Object RM returned false.");
			return false;
		} else {
			args.set(0, "reserve");
			args.add(5, price.getValue());
			customersOut.writeObject(args);
			try {
				result = getBoolean(customersIn.readObject());
				Trace.info("RM::reserveItem( " + id + ", " + customerID + ", "
						+ key + ", " + location + ") succeeded");
			} catch (Exception e) {
				Trace.error("Something wrong happened in reserve");
				return false;
			}
			// rmCustomer.reserve(id, cust);

			return result;
		}
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

	public boolean getBoolean(Object temp) throws Exception {
		try {
			return (new Boolean((String) temp)).booleanValue();
		} catch (Exception e) {
			throw e;
		}
	}

	public String getString(Object temp) throws Exception {
		try {
			return (String) temp;
		} catch (Exception e) {
			throw e;
		}
	}

}
