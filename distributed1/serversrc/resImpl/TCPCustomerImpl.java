package serversrc.resImpl;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Vector;

public class TCPCustomerImpl extends RMBaseImpl {

	ObjectInputStream in;
	ObjectOutputStream out;
	public static void main(String args[]) {
		ServerSocket customerSocket;
		Socket middlewareSocket;

		// Figure out where server is running
		String server = "localhost";
		int port = 1099;

		if (args.length == 1) {
			server = server + ":" + args[0];
			port = Integer.parseInt(args[0]);
		} else if (args.length != 0 && args.length != 1) {
			System.err.println("Wrong usage");
			System.out.println("Usage: java ResImpl.ResourceManagerImpl [port]");
			System.exit(1);
		}

		try {
			// create a new Server object
			TCPCustomerImpl obj = new TCPCustomerImpl();
			customerSocket = new ServerSocket(port);
			middlewareSocket = customerSocket.accept();
			System.err.println("Server ready");
			obj.in = new ObjectInputStream( middlewareSocket.getInputStream());
			obj.out = new ObjectOutputStream(middlewareSocket.getOutputStream());
			Vector method;
			
			while ((method = (Vector) obj.in.readObject()) != null) {
				obj.methodSelect(method);
			}
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	public void methodSelect(Vector input) throws Exception {
		String output = "";
		if (((String) input.elementAt(0)).equalsIgnoreCase("newCustomer")) {
			boolean added;
			if (input.size() == 3){ 
				added = newCustomer(getInt(input.elementAt(1)),
						getInt(input.elementAt(2)));
				out.writeBoolean(added);
				
			}
			
			else if (input.size() == 2) {
				int cid = newCustomer(getInt(input.elementAt(1)));
				out.writeInt(cid);
			}
		}

		if (((String) input.elementAt(0)).equalsIgnoreCase("getCustomer")) {
			Customer cust = getCustomer(getInt(input.elementAt(1)),
					getInt(input.elementAt(2)));
			out.writeObject(cust);
			

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("deleteCustomer")) {
			RMHashtable reservations = deleteCustomer(
					getInt(input.elementAt(1)), getInt(input.elementAt(2)));
			out.writeObject(reservations);
		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("queryCustomerInfo")) {
			String info = queryCustomerInfo(getInt(input.elementAt(1)),
					getInt(input.elementAt(2)));
			out.writeBytes(info);
		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("reserve")) {
			Boolean reserved = reserve(getInt(input.elementAt(1)),
					getInt(input.elementAt(2)), getString(input.elementAt(3)),
					getString(input.elementAt(4)), getInt(input.elementAt(5)),
					(ReservedItem.rType) input.elementAt(6));
			out.writeObject(reserved);
		}

		return;
	}

	public TCPCustomerImpl() throws RemoteException {

	}

	private RMItem readData(int id, String key) {
		synchronized (m_itemHT) {
			return (RMItem) m_itemHT.get(key);
		}
	}

	// Writes a data item
	private void writeData(int id, String key, RMItem value) {
		synchronized (m_itemHT) {
			m_itemHT.put(key, value);
		}
	}

	public int newCustomer(int id) throws RemoteException {

		Trace.info("INFO: RM::newCustomer(" + id + ") called");
		// Generate a globally unique ID for the new customer
		int cid = Integer.parseInt(String.valueOf(id)
				+ String.valueOf(Calendar.getInstance().get(
						Calendar.MILLISECOND))
				+ String.valueOf(Math.round(Math.random() * 100 + 1)));
		Customer cust = new Customer(cid);
		writeData(id, cust.getKey(), cust);
		Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
		return cid;
	}

	public boolean newCustomer(int id, int cid) throws RemoteException {
		{
			Trace.info("INFO: RM::newCustomer(" + id + ", " + cid + ") called");
			Customer cust = (Customer) readData(id, Customer.getKey(cid));
			if (cust == null) {
				cust = new Customer(cid);
				writeData(id, cust.getKey(), cust);
				Trace.info("INFO: RM::newCustomer(" + id + ", " + cid
						+ ") created a new customer");
				return true;
			} else {
				Trace.info("INFO: RM::newCustomer(" + id + ", " + cid
						+ ") failed--customer already exists");
				return false;
			} // else
		}
	}

	/**
	 * Must remove all reserved items of that customer as well.
	 */
	public RMHashtable deleteCustomer(int id, int customerID)
			throws RemoteException {

		Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called");
		Customer cust = (Customer) readData(id, Customer.getKey(customerID));
		if (cust == null) {
			Trace.warn("RM::deleteCustomer(" + id + ", " + customerID
					+ ") failed--customer doesn't exist");
			return null;
		} else {
			// Increase the reserved numbers of all reservable items which the
			// customer reserved.
			RMHashtable reservationHT = cust.getReservations();

			// remove the customer from the storage
			removeData(id, cust.getKey());

			Trace.info("RM::deleteCustomer(" + id + ", " + customerID
					+ ") succeeded");
			return reservationHT;
		} // if

	}

	public Customer getCustomer(int id, int customerID) throws RemoteException {
		return (Customer) readData(id, Customer.getKey(customerID));
	}

	// return a bill
	public String queryCustomerInfo(int id, int customerID)
			throws RemoteException {
		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID
				+ ") called");
		Customer cust = (Customer) readData(id, Customer.getKey(customerID));
		if (cust == null) {
			Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID
					+ ") failed--customer doesn't exist");
			return ""; // NOTE: don't change this--WC counts on this value
						// indicating a customer does not exist...
		} else {
			String s = cust.printBill();
			Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID
					+ "), bill follows...");
			System.out.println(s);
			return s;
		} // if
	}

	public synchronized boolean reserve(int id, int cid, String key,
			String location, int price, ReservedItem.rType rtype)
			throws RemoteException {
		Customer cust = (Customer) readData(id, Customer.getKey(cid));
		if (cust == null)
			return false;
		cust.reserve(key, location, price, rtype);
		writeData(id, cust.getKey(), cust);
		return true;
	}

	public int getInt(Object temp) throws Exception {
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
