package serversrc.resImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.Registry;
import serversrc.resInterface.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RMISecurityManager;

public class TCPCarImpl extends RMBaseImpl implements RMCar {

	// protected RMHashtable m_itemHT = new RMHashtable();

	public static void main(String args[]) {
		// Figure out where server is running
		ServerSocket carSocket = null;
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
			TCPCarImpl obj = new TCPCarImpl();
			carSocket = new ServerSocket(port);
			middlewareSocket = carSocket.accept();
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

	private String methodSelect(String input) throws NumberFormatException,
			RemoteException {
		String output = "";
		String[] args = input.split("[,()]"); // may cause trouble with location
		if (input.startsWith("addCars")) {
			output = this.addCars(args);
		}
		if (input.startsWith("deleteCars")) {
			output = this.deleteCars(args);
		}
		if (input.startsWith("queryCars")) {
			output = this.queryCars(args);
		}
		if (input.startsWith("queryCarsPrice")) {
			output = this.queryCarsPrice(args);
		}

		return output;
	}

	private String queryCarsPrice(String[] args) throws NumberFormatException,
			RemoteException {
		return String.valueOf(this.queryCarsPrice(Integer.parseInt(args[1]),
				args[2]));

	}

	private String queryCars(String[] args) throws NumberFormatException,
			RemoteException {
		return String
				.valueOf(this.queryCars(Integer.parseInt(args[1]), args[2]));
	}

	private String deleteCars(String[] args) throws NumberFormatException,
			RemoteException {

		return String.valueOf(this.deleteCars(Integer.parseInt(args[1]),
				args[2]));
	}

	// Reads a data item
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

	public TCPCarImpl() throws RemoteException {

	}

	@Override
	public boolean addCars(int id, String location, int count, int price)
			throws RemoteException {

		Trace.info("RM::addCars(" + id + ", " + location + ", " + count + ", $"
				+ price + ") called");
		Car curObj = (Car) readData(id, Car.getKey(location));
		if (curObj == null) {
			// car location doesn't exist...add it
			Car newObj = new Car(location, count, price);
			writeData(id, newObj.getKey(), newObj);
			Trace.info("RM::addCars(" + id + ") created new location "
					+ location + ", count=" + count + ", price=$" + price);
		} else {
			// add count to existing car location and update price...
			curObj.setCount(curObj.getCount() + count);
			if (price > 0) {
				curObj.setPrice(price);
			} // if
			writeData(id, curObj.getKey(), curObj);
			Trace.info("RM::addCars(" + id + ") modified existing location "
					+ location + ", count=" + curObj.getCount() + ", price=$"
					+ price);
		} // else
		return (true);
	}

	private String addCars(String[] args) throws NumberFormatException,
			RemoteException {
		return String.valueOf(this.addCars(Integer.parseInt(args[1]), args[2],
				Integer.parseInt(args[3]), Integer.parseInt(args[4])));

	}

	@Override
	public boolean deleteCars(int id, String location) throws RemoteException {

		return deleteItem(id, Car.getKey(location));
	}

	@Override
	public int queryCars(int id, String location) throws RemoteException {

		return queryNum(id, Car.getKey(location));
	}

	@Override
	public int queryCarsPrice(int id, String location) throws RemoteException {

		return queryPrice(id, Car.getKey(location));
	}

}
