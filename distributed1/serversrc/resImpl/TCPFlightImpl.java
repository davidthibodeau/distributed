package serversrc.resImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPFlightImpl extends RMBaseImpl {

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
			BufferedReader in = new BufferedReader(new InputStreamReader(
					middlewareSocket.getInputStream()));
			PrintWriter out = new PrintWriter(
					middlewareSocket.getOutputStream());
			String method;
			while ((method = in.readLine()) != null) {
				out.println(obj.methodSelect(method));
			}
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	private String methodSelect(String input) throws NumberFormatException {
		String output = "";
		String[] args = input.split("[,()]"); // may cause trouble with location
		if (input.startsWith("addCars")) {
			output = this.addFlight(args);
		}
		if (input.startsWith("deleteCars")) {
			output = this.deleteFlight(args);
		}
		if (input.startsWith("queryCars")) {
			output = this.queryFlight(args);
		}
		if (input.startsWith("queryCarsPrice")) {
			output = this.queryFlightPrice(args);
		}

		return output;
	}

	private String addFlight(String[] args) throws NumberFormatException {
		return String.valueOf(this.addFlight(Integer.parseInt(args[1]),
				Integer.parseInt(args[2]), Integer.parseInt(args[3]),
				Integer.parseInt(args[4])));

	}

	private String queryFlightPrice(String[] args) throws NumberFormatException {
		return String.valueOf(this.queryFlightPrice(Integer.parseInt(args[1]),
				Integer.parseInt(args[2])));

	}

	private String queryFlight(String[] args) throws NumberFormatException {
		return String.valueOf(this.queryFlight(Integer.parseInt(args[1]),
				Integer.parseInt(args[2])));
	}

	private String deleteFlight(String[] args) throws NumberFormatException {

		return String.valueOf(this.deleteFlight(Integer.parseInt(args[1]),
				Integer.parseInt(args[2])));
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

}
