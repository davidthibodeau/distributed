package serversrc.resImpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Vector;

import serversrc.resInterface.*;

@SuppressWarnings("rawtypes")
public class TCPHotelImpl extends RMBaseImpl implements RMHotel, Runnable {
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
				TCPHotelImpl obj;
				middlewareSocket = connection.accept();
				obj = new TCPHotelImpl(middlewareSocket);
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
			in = new ObjectInputStream(middlewareSocket.getInputStream());
			out = new ObjectOutputStream(middlewareSocket.getOutputStream());
			Vector method;

			while ((method = (Vector) in.readObject()) != null) {
				methodSelect(method);
			}
		} catch (Exception e) {
			Trace.error("Cannot Connect");
		}

	}

	public void methodSelect(Vector input) throws Exception {

		if (((String) input.elementAt(0)).equalsIgnoreCase("addRooms")) {
			boolean added = addRooms(getInt(input.elementAt(1)),
					getString(input.elementAt(2)), getInt(input.elementAt(3)),
					getInt(input.elementAt(4)));
			out.writeBoolean(added);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("deleteRooms")) {
			boolean deleted = deleteRooms(getInt(input.elementAt(1)),
					getString(input.elementAt(2)));
			out.writeBoolean(deleted);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("queryRooms")) {
			int emptySeats = queryRooms(getInt(input.elementAt(1)),
					getString(input.elementAt(2)));
			out.writeInt(emptySeats);

		}
		if (((String) input.elementAt(0)).equalsIgnoreCase("queryRoomsPrice")) {
			int price = queryRoomsPrice(getInt(input.elementAt(1)),
					getString(input.elementAt(2)));
			out.writeInt(price);

		}

		return;
	}

	
	public TCPHotelImpl() throws RemoteException {
	
	}
	
	private TCPHotelImpl(Socket middlewareSocket) {
		this.middlewareSocket = middlewareSocket;
	}

	// Reads a data item
    private RMItem readData( int id, String key )
    {
        synchronized(m_itemHT) {
            return (RMItem) m_itemHT.get(key);
        }
    }

    // Writes a data item
    @SuppressWarnings("unchecked")
	private void writeData( int id, String key, RMItem value )
    {
        synchronized(m_itemHT) {
            m_itemHT.put(key, value);
        }
    }
	
    // Create a new room location or add rooms to an existing location
    //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int id, String location, int count, int price)
        throws RemoteException
    {
        Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
        Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
        if ( curObj == null ) {
            // doesn't exist...add it
            Hotel newObj = new Hotel( location, count, price );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
        } else {
            // add count to existing object and update price...
            curObj.setCount( curObj.getCount() + count );
            if ( price > 0 ) {
                curObj.setPrice( price );
            } // if
            writeData( id, curObj.getKey(), curObj );
            Trace.info("RM::addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
        } // else
        return(true);
    }
    
    // Delete rooms from a location
    public boolean deleteRooms(int id, String location)
        throws RemoteException
    {
        return deleteItem(id, Hotel.getKey(location));
        
    }

    // Returns the number of rooms available at a location
    public int queryRooms(int id, String location)
        throws RemoteException
    {
        return queryNum(id, Hotel.getKey(location));
    }
    
    
    // Returns room price at this location
    public int queryRoomsPrice(int id, String location)
        throws RemoteException
    {
        return queryPrice(id, Hotel.getKey(location));
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
