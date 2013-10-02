package serversrc.resImpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import serversrc.resInterface.*;

import java.rmi.RemoteException;
import java.util.Vector;

@SuppressWarnings("rawtypes")
public class TCPCarImpl extends RMBaseImpl implements RMCar, Runnable {
	ObjectInputStream in;
	ObjectOutputStream out;
	private Socket middlewareSocket;

	public static void main(String args[]) {
		// Figure out where server is running
		ServerSocket connection = null;
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
			connection = new ServerSocket(port);
			while (true) {
				TCPCarImpl obj;
				System.out.println("Waiting for connection");
				middlewareSocket = connection.accept();
				obj = new TCPCarImpl(middlewareSocket);
				Thread t = new Thread(obj);
				t.run();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void run() {
		try {
		    System.out.println("Thread started.");
			in = new ObjectInputStream(middlewareSocket.getInputStream());
			out = new ObjectOutputStream(middlewareSocket.getOutputStream());
			Vector method;
			while (true){
				System.out.println("Waiting for query.");

			    method = (Vector) in.readObject();
			    if (method != null) {
				methodSelect(method);
			    }
			}
		} catch (Exception e) {
			Trace.error("Cannot Connect");
		}

	}

	public void methodSelect(Vector input) throws Exception {

		if (((String) input.elementAt(0)).equalsIgnoreCase("newCar")) {
			Boolean added = addCars(getInt(input.elementAt(1)),
					getString(input.elementAt(2)), getInt(input.elementAt(3)),
					getInt(input.elementAt(4)));
			
			System.out.println("newCar returned " + added);
			out.writeObject(added);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("deleteCar")) {
			Boolean deleted = deleteCars(getInt(input.elementAt(1)),
					getString(input.elementAt(2)));
			out.writeObject(deleted);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("queryCar")) {
			Integer emptySeats = queryCars(getInt(input.elementAt(1)),
					getString(input.elementAt(2)));
			out.writeObject(emptySeats);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("queryCarPrice")) {
			Integer price = queryCarsPrice(getInt(input.elementAt(1)),
					getString(input.elementAt(2)));
			out.writeObject(price);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("reserveItem")) {
			RMInteger price = reserveItem(getInt(input.elementAt(1)),
					getInt(input.elementAt(2)),getString(input.elementAt(3)),
					getString(input.elementAt(4)));
			out.writeObject(price);
		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("unreserveItem")) {
			Boolean answer = unreserveItem(getInt(input.elementAt(1)),
					(ReservedItem)input.elementAt(2));
			out.writeObject(answer);
		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("unreserveKey")) {
			Boolean answer = unreserveItem(getInt(input.elementAt(1)),
					getString(input.elementAt(2)));
			out.writeObject(answer);
		}
		System.out.println("methodselect returned");
	}
	// Reads a data item
	private RMItem readData(int id, String key) {
		synchronized (m_itemHT) {
			return (RMItem) m_itemHT.get(key);
		}
	}

	// Writes a data item
	@SuppressWarnings("unchecked")
	private void writeData(int id, String key, RMItem value) {
		synchronized (m_itemHT) {
			m_itemHT.put(key, value);
		}
	}

	public TCPCarImpl() throws RemoteException {

	}

	public TCPCarImpl(Socket middlewareSocket) {
		this.middlewareSocket = middlewareSocket;
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
