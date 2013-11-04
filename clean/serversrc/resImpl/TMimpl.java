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
	
	public TMimpl(RMCar Car, RMFlight Flight,	RMHotel Hotel, RMCustomer Customer){
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
		return 0;
	}

	@Override
	public boolean commit(int transactionID) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void abort(int transactionID) throws RemoteException, InvalidTransactionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void enlist(int transactionID, RMType rm) {
		// TODO Auto-generated method stub

	}


}
