package serversrc.resImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.Registry;
import serversrc.resInterface.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RMISecurityManager;
import java.util.Vector;

public class TCPCarImpl extends RMBaseImpl implements RMCar {
	ObjectInputStream in;
	ObjectOutputStream out;

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

		if (((String) input.elementAt(0)).equalsIgnoreCase("addCars")) {
			boolean added = addCars(getInt(input.elementAt(1)),
					getString(input.elementAt(2)), getInt(input.elementAt(3)),
					getInt(input.elementAt(4)));
			out.writeBoolean(added);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("deleteCars")) {
			boolean deleted = deleteCars(getInt(input.elementAt(1)),
					getString(input.elementAt(2)));
			out.writeBoolean(deleted);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("queryCars")) {
			int emptySeats = queryCars(getInt(input.elementAt(1)),
					getString(input.elementAt(2)));
			out.writeInt(emptySeats);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("queryCarsPrice")) {
			int price = queryCarsPrice(getInt(input.elementAt(1)),
					getString(input.elementAt(2)));
			out.writeInt(price);

		}

		return;
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
