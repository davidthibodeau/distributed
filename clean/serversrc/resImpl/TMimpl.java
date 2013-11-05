package serversrc.resImpl;

import java.rmi.RemoteException;
import java.util.Calendar;

import serversrc.resInterface.RMCar;
import serversrc.resInterface.RMCustomer;
import serversrc.resInterface.RMFlight;
import serversrc.resInterface.RMHotel;
import serversrc.resInterface.RMType;
import serversrc.resInterface.TransactionManager;

public class TMimpl implements TransactionManager {
	
	private RMCar rmCar;
	private RMFlight rmFlight;
	private RMHotel rmHotel;
	private RMCustomer rmCustomer;
	private RMHashtable transactionHT;
	
    // Reads a data item
    private Transaction readData(int id)
    {
        synchronized(transactionHT) {
            return (Transaction) transactionHT.get(id);
        }
    }

    // Writes a data item
    private void writeData( int id, Transaction value )
    {
        synchronized(transactionHT) {
            transactionHT.put(id, value);
        }
    }
    
    // Remove the item out of storage
    protected Transaction removeData(int id) {
        synchronized(transactionHT) {
            return (Transaction)transactionHT.remove(id);
        }
    }
	
	public TMimpl(RMCar Car, RMFlight Flight, RMHotel Hotel, RMCustomer Customer){
		rmCar = Car;
		rmFlight = Flight;
		rmHotel = Hotel;
		rmCustomer = Customer;
		transactionHT = new RMHashtable();
	}

	@Override
	public int start() throws RemoteException {
		// Generate a globally unique ID for the new transaction
        int tid = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                   String.valueOf( Math.round( Math.random() * 100 + 1 )));
        writeData(tid, new Transaction(tid));
		return tid;
	}

	@Override
	public boolean commit(int transactionID) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		Transaction t = removeData(transactionID);
		if(t.isCarEnlisted())
			rmCar.commit(transactionID);
		if(t.isFlightEnlisted())
			rmFlight.commit(transactionID);
		if(t.isHotelEnlisted())
			rmHotel.commit(transactionID);
		if(t.isCustomerEnlisted())
			rmCustomer.commit(transactionID);
		return true;
	}

	@Override
	public void abort(int transactionID) throws RemoteException, InvalidTransactionException {
		Transaction t = removeData(transactionID);
		if(t.isCarEnlisted())
			rmCar.abort(transactionID);
		if(t.isFlightEnlisted())
			rmFlight.abort(transactionID);
		if(t.isHotelEnlisted())
			rmHotel.abort(transactionID);
		if(t.isCustomerEnlisted())
			rmCustomer.abort(transactionID);
		
	}

	@Override
	public void enlist(int transactionID, RMType rm) {
		Transaction t = readData(transactionID);
		switch(rm){
		case CAR:
			t.enlistCar();
			break;
		case FLIGHT:
			t.enlistFlight();
			break;
		case HOTEL:
			t.enlistHotel();
			break;
		case CUSTOMER:
			t.enlistCustomer();
			break;
		}

	}


}
