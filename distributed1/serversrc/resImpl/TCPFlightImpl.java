package serversrc.resImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class TCPFlightImpl extends RMBaseImpl {

	ObjectInputStream in;
	ObjectOutputStream out;

	public static void main(String args[]) {
		// Figure out where server is running
		ServerSocket flightSocket = null;
		Socket middlewareSocket = null;

		String server = "localhost";
		int port = 1099;

		if (args.length == 1) {
			server = server + ":" + args[0];
			port = Integer.parseInt(args[0]);
		} else if (args.length != 0 && args.length != 1) {
			System.err.println("Wrong usage");
			System.out
					.println("Usage: java ResImpl.ResourceManagerImpl [port]");
			System.exit(1);
		}

		try {
			// create a new Server object
			TCPFlightImpl obj = new TCPFlightImpl();
			flightSocket = new ServerSocket(port);
			middlewareSocket = flightSocket.accept();
			System.err.println("Server ready");
			obj.in = new ObjectInputStream(middlewareSocket.getInputStream());
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

		if (((String) input.elementAt(0)).equalsIgnoreCase("addFlight")) {
			boolean added = addFlight(getInt(input.elementAt(1)),
					getInt(input.elementAt(2)), getInt(input.elementAt(3)),
					getInt(input.elementAt(4)));
			out.writeBoolean(added);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("deleteFlight")) {
			boolean deleted = deleteFlight(getInt(input.elementAt(1)),
					getInt(input.elementAt(2)));
			out.writeBoolean(deleted);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("queryFlight")) {
			int emptySeats = queryFlight(getInt(input.elementAt(1)),
					getInt(input.elementAt(2)));
			out.writeInt(emptySeats);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("queryFlightPrice")) {
			int price = queryFlightPrice(getInt(input.elementAt(1)),
					getInt(input.elementAt(2)));
			out.writeInt(price);

		}

		return;
	}

	public TCPFlightImpl() {

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

	// Create a new flight, or add seats to existing flight
	// NOTE: if flightPrice <= 0 and the flight already exists, it maintains its
	// current price
	public boolean addFlight(int id, int flightNum, int flightSeats,
			int flightPrice) {
		Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $"
				+ flightPrice + ", " + flightSeats + ") called");
		Flight curObj = (Flight) readData(id, Flight.getKey(flightNum));
		if (curObj == null) {
			// doesn't exist...add it
			Flight newObj = new Flight(flightNum, flightSeats, flightPrice);
			writeData(id, newObj.getKey(), newObj);
			Trace.info("RM::addFlight(" + id + ") created new flight "
					+ flightNum + ", seats=" + flightSeats + ", price=$"
					+ flightPrice);
		} else {
			// add seats to existing flight and update the price...
			curObj.setCount(curObj.getCount() + flightSeats);
			if (flightPrice > 0) {
				curObj.setPrice(flightPrice);
			} // if
			writeData(id, curObj.getKey(), curObj);
			Trace.info("RM::addFlight(" + id + ") modified existing flight "
					+ flightNum + ", seats=" + curObj.getCount() + ", price=$"
					+ flightPrice);
		} // else
		return (true);
	}

	public boolean deleteFlight(int id, int flightNum) {
		return deleteItem(id, Flight.getKey(flightNum));
	}

	// Returns the number of empty seats on this flight
	public int queryFlight(int id, int flightNum) {
		return queryNum(id, Flight.getKey(flightNum));
	}

	// Returns price of this flight
	public int queryFlightPrice(int id, int flightNum) {
		return queryPrice(id, Flight.getKey(flightNum));
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
